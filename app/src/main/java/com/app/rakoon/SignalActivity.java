package com.app.rakoon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.SignalEntry;
import com.app.rakoon.Fragments.Settings;
import com.app.rakoon.Helpers.SignalHelper;
import com.app.rakoon.Helpers.VerticesHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.grid.features.Point;
import mil.nga.mgrs.MGRS;


public class SignalActivity extends MapActivity {

	private DatabaseHelper databaseHelper;
	private static int accuracy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		databaseHelper = new DatabaseHelper(SignalActivity.this);

		// button to record sound decibel
		ImageButton getDecibel = findViewById(R.id.getDecibel);

		getDecibel.setOnClickListener(v -> {
			try {
				getSignal();
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		});
	}


	private void getSignal() throws ParseException {
		SignalHelper signalHelper = SignalHelper.getInstance(this);
		int signalLevel = signalHelper.getSignal();
		saveInDatabase(signalLevel);
		Toast.makeText(this, "signalLevel: " + signalLevel, Toast.LENGTH_SHORT).show();
		saveInDatabase(signalLevel);
	}

	// method to wait for map to be loaded
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		super.onMapReady(googleMap);
		try {
			this.fetchData();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void fetchData() throws ParseException {
		accuracy = super.getAccuracy();

		List<SignalEntry> signals = databaseHelper.getSignals();
		Map<String, Double> averageSignals;
		averageSignals = calculateSignalAverages(signals);

		for (Map.Entry<String, Double> s : averageSignals.entrySet()) {
			SignalEntry se = new SignalEntry(s.getKey(), s.getValue());
			colorMap(se);
		}
	}

	public Map<String, Double> calculateSignalAverages(List<SignalEntry> signals) throws ParseException {
		Map<String, List<Integer>> signalMap = new HashMap<>();
		VerticesHelper verticesHelper = new VerticesHelper(accuracy);

		for (SignalEntry entry : signals) {
			String MGRS = entry.getMGRS();

			verticesHelper.setBottom_left(MGRS);
			String sw = verticesHelper.getBottom_left();

			int signal = entry.getSignal();

			if (!signalMap.containsKey(sw)) {
				signalMap.put(sw, new ArrayList<>());
			}
			signalMap.get(sw).add(signal);
		}

		int userLimit = Settings.getNumber(getApplicationContext());

		// Calculating the average signal for each MGRS area
		Map<String, Double> averageSignals = new HashMap<>();
		for (Map.Entry<String, List<Integer>> entry : signalMap.entrySet()) {
			List<Integer> signalList = entry.getValue();

			if (signalList.size() > userLimit) {
				signalList = signalList.subList(0, userLimit);
			}

			double sum = 0;

			for(int d: signalList){
				sum += d;
			}

			double average = sum / signalList.size();

			averageSignals.put(entry.getKey(), average);
		}

		return averageSignals;
	}

	private void colorMap(@NonNull SignalEntry s) throws ParseException {
		String sw = s.getMGRS();

		// this substring make the marker go on the bottom-left corner of the 10m x 10m square i'm currently in
		//Log.d("sw: ", sw.toString());
		accuracy = super.getAccuracy();

		// now mgrs is the location point in MGRS coord, i have to find the corresponding square
		VerticesHelper verticesHelper = new VerticesHelper(accuracy);
		verticesHelper.setBottom_left(sw);

		sw = verticesHelper.getBottom_left();

		// bottom right corner
		String se = verticesHelper.getBottom_right();

		// top left corner
		String nw = verticesHelper.getTop_left();

		// top right corner
		String ne = verticesHelper.getTop_right();

		try {
			Point sw_point = MGRS.parse(sw).toPoint();

			Point se_point = MGRS.parse(se).toPoint();

			Point nw_point = MGRS.parse(nw).toPoint();

			Point ne_point = MGRS.parse(ne).toPoint();

			List<LatLng> vertices = new ArrayList<>();

			// polygons vertices latlong
			vertices.add(new LatLng(nw_point.getLatitude(), nw_point.getLongitude()));
			vertices.add(new LatLng(sw_point.getLatitude(), sw_point.getLongitude()));
			vertices.add(new LatLng(se_point.getLatitude(), se_point.getLongitude()));
			vertices.add(new LatLng(ne_point.getLatitude(), ne_point.getLongitude()));

			/**
			 * square with wide border
			 * PolygonOptions rectOptions = new PolygonOptions().addAll(vertices).strokeColor(Color.RED) // border color
			 * 					.fillColor(Color.argb(100, 0, 255, 155));
			 */

			PolygonOptions poly = new PolygonOptions().addAll(vertices).strokeWidth(0);

			// check the mean value to color the square
			if (s.getSignalAVG() >= 3.5) {
				poly.fillColor(Color.rgb(144, 238, 144));
			} else if (s.getSignalAVG() < 3.5 && s.getSignalAVG() >= 1) {
				poly.fillColor(Color.rgb(255, 215, 0));
			} else {
				poly.fillColor(Color.rgb(255, 0, 0));
			}
			mMap.addPolygon(poly);

		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void saveInDatabase(int signal) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
		String time = sdf.format(new Date());

		MGRS mgrs = MGRS.from(currentLocation.longitude, currentLocation.latitude);

		String mgrs_1 = mgrs.toString();

		SignalEntry signalEntry = new SignalEntry(mgrs_1, signal, time);

		DatabaseHelper databaseHelper = new DatabaseHelper(SignalActivity.this);

		boolean success = databaseHelper.addSignalEntry(signalEntry);
		Toast.makeText(this, "Saved: " + success, Toast.LENGTH_SHORT).show();
		if (success) {
			fetchData();
		}
	}

	private boolean isPhonePermissionGranted() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
			return false;
		}
		return true;
	}
}


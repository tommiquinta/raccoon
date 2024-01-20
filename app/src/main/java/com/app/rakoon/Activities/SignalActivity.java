package com.app.rakoon.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.SignalEntry;
import com.app.rakoon.Fragments.AllDataFragment;
import com.app.rakoon.Fragments.mySettings;
import com.app.rakoon.Helpers.SignalHelper;
import com.app.rakoon.Helpers.VerticesHelper;
import com.app.rakoon.R;
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
import java.util.Objects;

import mil.nga.grid.features.Point;
import mil.nga.mgrs.MGRS;


public class SignalActivity extends MapActivity {

	private DatabaseHelper databaseHelper;
	private static int accuracy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		databaseHelper = new DatabaseHelper(SignalActivity.this);
		isPhonePermissionGranted();

		Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.signal_action_bar);

		// button to record sound decibel
		ImageButton getDecibel = findViewById(R.id.getDecibel);

		getDecibel.setOnClickListener(v -> {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				try {
					getSignal();
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			} else {
				Toast.makeText(this, "Location not available.", Toast.LENGTH_SHORT).show();
			}
		});
	}


	private void getSignal() throws ParseException {
		SignalHelper signalHelper = new SignalHelper(this);
		int signalLevel = signalHelper.getSignal();
		Toast.makeText(this, "Signal Level: " + getDescription(signalLevel), Toast.LENGTH_SHORT).show();
		saveInDatabase(signalLevel);
	}

	private String getDescription(int s) {
		if (s >= 3) {
			return "Good";
		} else if (s < 3 && s >= 1) {
			return "Medium";
		} else {
			return "Bad";
		}
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

	List<SignalEntry> signals;

	@Override
	public void fetchData() throws ParseException {
		new Thread(() -> {
			try {
				accuracy = super.getAccuracy();

				signals = databaseHelper.getSignals();

				Map<String, Double> averageSignals;
				averageSignals = calculateSignalAverages(signals);

				for (Map.Entry<String, Double> s : averageSignals.entrySet()) {
					SignalEntry se = new SignalEntry(s.getKey(), s.getValue());
					new Handler(Looper.getMainLooper()).post(() -> {
						try {
							colorMap(se);
						} catch (ParseException e) {
							throw new RuntimeException(e);
						}
					});
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}).start();
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

		int userLimit = mySettings.getNumber(getApplicationContext());

		// Calculating the average signal for each MGRS area
		Map<String, Double> averageSignals = new HashMap<>();
		for (Map.Entry<String, List<Integer>> entry : signalMap.entrySet()) {
			List<Integer> signalList = entry.getValue();

			if (signalList.size() > userLimit) {
				signalList = signalList.subList(0, userLimit);
			}

			double sum = 0;

			for (int d : signalList) {
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
			if (s.getSignalAVG() >= 3) {
				poly.fillColor(Color.rgb(144, 238, 144));
			} else if (s.getSignalAVG() < 3 && s.getSignalAVG() >= 1) {
				poly.fillColor(Color.rgb(255, 215, 0));
			} else {
				poly.fillColor(Color.rgb(255, 0, 0));
			}

			mMap.addPolygon(poly);
			mMap.setOnMapClickListener(arg0 -> {
				Log.d("arg0", arg0.latitude + "-" + arg0.longitude);
				double latitude = arg0.latitude;
				double longitude = arg0.longitude;
				Intent intent = new Intent(SignalActivity.this, AllDataFragment.class);
				intent.putExtra("latitude", String.valueOf(latitude));
				intent.putExtra("longitude", String.valueOf(longitude));
				intent.putExtra("TYPE", "SIGNAL_DATA");
				intent.putExtra("accuracy", String.valueOf(accuracy));
				startActivity(intent);
			});

		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void saveInDatabase(int signal) {
		if (currentLocation == null) {
			Toast.makeText(this, "Location not available.", Toast.LENGTH_SHORT).show();
			return;
		}
		new Thread(() -> {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
			String time = sdf.format(new Date());

			MGRS mgrs = MGRS.from(currentLocation.longitude, currentLocation.latitude);

			String mgrs_1 = mgrs.toString();

			SignalEntry signalEntry = new SignalEntry(mgrs_1, signal, time);

			DatabaseHelper databaseHelper = new DatabaseHelper(SignalActivity.this);

			boolean success = databaseHelper.addSignalEntry(signalEntry);

			//Toast.makeText(this, "Saved: " + success, Toast.LENGTH_SHORT).show();
			runOnUiThread(() -> {

				if (success) {
					List<SignalEntry> newSignal = new ArrayList<>();
					newSignal.add(signalEntry);
					int userLimit = mySettings.getNumber(getApplicationContext());

					if (newSignal.size() > userLimit) {
						newSignal = newSignal.subList(0, userLimit);
					}

					for (SignalEntry s : signals) {
						if (s.getMGRS().equals(mgrs_1)) {
							newSignal.add(s);
						}
					}
					double total = 0;

					for (SignalEntry s : newSignal) {
						total += s.getSignal();
					}
					double average = total / newSignal.size();

					try {
						colorMap(new SignalEntry(mgrs_1, average));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}).start();
	}

	private void isPhonePermissionGranted() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
		}
	}
}


package com.app.rakoon;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.SignalEntry;
import com.app.rakoon.Database.WifiEntry;
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

public class WiFiActivity extends MapActivity{
	private DatabaseHelper databaseHelper;
	private static int accuracy;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		databaseHelper = new DatabaseHelper(WiFiActivity.this);

		// button to record sound decibel
		ImageButton getDecibel = findViewById(R.id.getDecibel);

		getDecibel.setOnClickListener(v -> {
			try {
				getWiFi();
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		});
	}


	private void getWiFi() throws ParseException {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		Network network = connectivityManager.getActiveNetwork();
		NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

		if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
			double signalStrength = 0;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
				signalStrength = capabilities.getSignalStrength();
				saveInDatabase(signalStrength);
			}
			Toast.makeText(this, "signalLevel: " + signalStrength, Toast.LENGTH_SHORT).show();
		}
	};

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

		List<WifiEntry> wifis = databaseHelper.getWiFi();
		Map<String, Double> averageWifis;
		averageWifis = calculateSignalAverages(wifis);

		for (Map.Entry<String, Double> s : averageWifis.entrySet()) {
			WifiEntry we = new WifiEntry(s.getKey(), s.getValue());

			colorMap(we);
		}

	}

	public Map<String, Double> calculateSignalAverages(List<WifiEntry> wifis) throws ParseException {
		Map<String, List<Double>> signalMap = new HashMap<>();
		VerticesHelper verticesHelper = new VerticesHelper(accuracy);

		for (WifiEntry entry : wifis) {
			String MGRS = entry.getMGRS();
			verticesHelper.setBottom_left(MGRS);
			String sw = verticesHelper.getBottom_left();

			double signal = entry.getWifi();

			if (!signalMap.containsKey(sw)) {
				signalMap.put(sw, new ArrayList<>());
			}
			signalMap.get(sw).add(signal);
		}

		// Calculating the average singal for each MGRS area
		Map<String, Double> averageSignals = new HashMap<>();
		for (Map.Entry<String, List<Double>> entry : signalMap.entrySet()) {
			List<Double> signalList = entry.getValue();
			double sum = 0;
			for (double d : signalList) {
				sum += d;
			}

			double average = sum / signalList.size();

			averageSignals.put(entry.getKey(), average);
		}

		return averageSignals;
	}

	private void colorMap(@NonNull WifiEntry s) throws ParseException {
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
			Toast.makeText(this, "avg: " + s.getWifi(), Toast.LENGTH_SHORT).show();

			// check the mean value to color the square
			if (s.getWifi() >= -60) {
				poly.fillColor(Color.rgb(144, 238, 144));
			} else if (s.getWifi() <-60 && s.getWifi() >= -90) {
				poly.fillColor(Color.rgb(255, 215, 0));
			} else {
				poly.fillColor(Color.rgb(255, 0, 0));
			}
			mMap.addPolygon(poly);

		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void saveInDatabase(double signal) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
		String time = sdf.format(new Date());

		MGRS mgrs = MGRS.from(currentLocation.longitude, currentLocation.latitude);

		String mgrs_1 = mgrs.toString();

		WifiEntry wifiEntry = new WifiEntry(mgrs_1, signal, time);

		DatabaseHelper databaseHelper = new DatabaseHelper(WiFiActivity.this);

		boolean success = databaseHelper.addWifiEntry(wifiEntry);
		Toast.makeText(this, "Saved: " + success, Toast.LENGTH_SHORT).show();
		if (success) {
			fetchData();
		}
	}



}

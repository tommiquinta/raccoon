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

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.WifiEntry;
import com.app.rakoon.Fragments.AllDataFragment;
import com.app.rakoon.Fragments.mySettings;
import com.app.rakoon.Helpers.VerticesHelper;
import com.app.rakoon.Helpers.WiFiHelper;
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

public class WiFiActivity extends MapActivity {
	private DatabaseHelper databaseHelper;
	private static int accuracy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.wifi_action_bar);

		databaseHelper = new DatabaseHelper(WiFiActivity.this);

		// button to record sound decibel
		ImageButton getDecibel = findViewById(R.id.getDecibel);

		getDecibel.setOnClickListener(v -> {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				try {
					getWiFi();
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			} else {
				Toast.makeText(this, "Location not available.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void getWiFi() throws ParseException {
		WiFiHelper wiFiHelper = new WiFiHelper(this);
		double signalStrength = wiFiHelper.getWiFi();
		if (signalStrength == 101) {  // wifi error
			Toast.makeText(this, "No WiFi network Available.", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Wifi Level: " + getDescription(signalStrength), Toast.LENGTH_SHORT).show();
			saveInDatabaseAsync(signalStrength);
		}
	}

	private String getDescription(double signal) {
		if (signal >= -60) {
			return "Good";
		} else if (signal < -60 && signal >= -90) {
			return "Medium";
		} else {
			return "Bad";
		}
	}

	// method to wait for map to be loaded
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		super.onMapReady(googleMap);
		this.fetchData();
	}

	List<WifiEntry> wifis;

	@Override
	public void fetchData() {
		new Thread(() -> {
			try {
				accuracy = super.getAccuracy();
				wifis = databaseHelper.getWiFi();

				Map<String, Double> averageWifis;
				averageWifis = calculateSignalAverages(wifis);

				for (Map.Entry<String, Double> s : averageWifis.entrySet()) {
					WifiEntry we = new WifiEntry(s.getKey(), s.getValue());
					new Handler(Looper.getMainLooper()).post(() -> {
						try {
							colorMap(we);
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

		int userLimit = mySettings.getNumber(getApplicationContext());

		// Calculating the average singal for each MGRS area
		Map<String, Double> averageSignals = new HashMap<>();
		for (Map.Entry<String, List<Double>> entry : signalMap.entrySet()) {
			List<Double> signalList = entry.getValue();

			if (signalList.size() > userLimit) {
				signalList = signalList.subList(0, userLimit);
			}

			double sum = 0;

			for (double d : signalList) {
				//Log.d("signal: ", String.valueOf(d));
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

			// check the mean value to color the square
			if (s.getWifi() >= -60) {
				poly.fillColor(Color.rgb(144, 238, 144));
			} else if (s.getWifi() < -60 && s.getWifi() >= -90) {
				poly.fillColor(Color.rgb(255, 215, 0));
			} else {
				poly.fillColor(Color.rgb(255, 0, 0));
			}
			mMap.addPolygon(poly);

			mMap.setOnMapClickListener(arg0 -> {
				Log.d("arg0", arg0.latitude + "-" + arg0.longitude);
				double latitude = arg0.latitude;
				double longitude = arg0.longitude;
				Intent intent = new Intent(WiFiActivity.this, AllDataFragment.class);
				intent.putExtra("latitude", String.valueOf(latitude));
				intent.putExtra("longitude", String.valueOf(longitude));
				intent.putExtra("TYPE", "WIFI_DATA");
				intent.putExtra("accuracy", String.valueOf(accuracy));
				startActivity(intent);
			});

		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void saveInDatabaseAsync(final double signal) {
		if (currentLocation == null) {
			Toast.makeText(this, "Location not available.", Toast.LENGTH_SHORT).show();
			return;
		}
		new Thread(() -> {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
			String time = sdf.format(new Date());

			MGRS mgrs = MGRS.from(currentLocation.longitude, currentLocation.latitude);
			String mgrs_1 = mgrs.toString();

			WifiEntry wifiEntry = new WifiEntry(mgrs_1, signal, time);

			DatabaseHelper databaseHelper = new DatabaseHelper(WiFiActivity.this);

			final boolean success = databaseHelper.addWifiEntry(wifiEntry);

			runOnUiThread(() -> {
				//Toast.makeText(WiFiActivity.this, "Saved: " + success, Toast.LENGTH_SHORT).show();
				if (success) {
					List<WifiEntry> newWiFi = new ArrayList<>();
					newWiFi.add(wifiEntry);
					int userLimit = mySettings.getNumber(getApplicationContext());

					if (userLimit < wifis.size()) {
						wifis = wifis.subList(0, userLimit-1);
					}

					for (WifiEntry s : wifis) {
						if (s.getMGRS().equals(mgrs_1)) {
							newWiFi.add(s);
						}
					}
					double total = 0;

					for (WifiEntry s : newWiFi) {
						total += s.getWifi();
					}
					double average = total / newWiFi.size();

					try {
						colorMap(new WifiEntry(mgrs_1, average));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}).start();
	}
}

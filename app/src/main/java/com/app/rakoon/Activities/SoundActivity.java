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
import com.app.rakoon.Database.SoundEntry;
import com.app.rakoon.Fragments.AllDataFragment;
import com.app.rakoon.Fragments.mySettings;
import com.app.rakoon.Helpers.SoundHelper;
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

public class SoundActivity extends MapActivity {
	// audio
	private boolean isRecording = false;
	private static int accuracy;
	private DatabaseHelper databaseHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.sound_action_bar);

		databaseHelper = new DatabaseHelper(SoundActivity.this);

		// button to record sound decibel
		ImageButton getDecibel = findViewById(R.id.getDecibel);

		getDecibel.setOnClickListener(v -> {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				if (!isRecording) {
					try {
						startRecording();
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				} else {
					stopRecording();
				}
			} else {
				Toast.makeText(this, "Location not available.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		super.onMapReady(googleMap);
		try {
			fetchData();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	List<SoundEntry> sounds;

	@Override
	public void fetchData() throws ParseException {
		new Thread(() -> {
			try {
				accuracy = super.getAccuracy();
				sounds = databaseHelper.getSounds();

				Map<String, Double> averageDecibels;
				averageDecibels = calculateDecibelAverages(sounds);

				for (Map.Entry<String, Double> s : averageDecibels.entrySet()) {
					SoundEntry se = new SoundEntry(s.getKey(), s.getValue());
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

	public Map<String, Double> calculateDecibelAverages(List<SoundEntry> soundEntries) throws ParseException {
		Map<String, List<Double>> decibelMap = new HashMap<>();
		VerticesHelper verticesHelper = new VerticesHelper(accuracy);

		for (SoundEntry entry : soundEntries) {
			String MGRS = entry.getMGRS();

			verticesHelper.setBottom_left(MGRS);
			String sw = verticesHelper.getBottom_left();

			double decibel = entry.getDecibel();

			if (!decibelMap.containsKey(sw)) {
				decibelMap.put(sw, new ArrayList<>());
			}
			decibelMap.get(sw).add(decibel);
		}

		int userLimit = mySettings.getNumber(getApplicationContext());

		// Calculating the average decibel for each MGRS area
		Map<String, Double> averageDecibels = new HashMap<>();
		for (Map.Entry<String, List<Double>> entry : decibelMap.entrySet()) {
			List<Double> decibelList = entry.getValue();

			if (decibelList.size() > userLimit) {
				decibelList = decibelList.subList(0, userLimit);
			}

			double sum = 0;

			for (double d : decibelList) {
				sum += d;
			}
			double average = sum / decibelList.size();
			averageDecibels.put(entry.getKey(), average);
		}
		return averageDecibels;
	}

	private void colorMap(@NonNull SoundEntry s) throws ParseException {
		String sw = s.getMGRS();
		// this substring make the marker go on the bottom-left corner of the 10m x 10m square i'm currently in
		//Log.d("sw: ", sw.toString());
		// now mgrs is the location point in MGRS coord, i have to find the corresponding square
		accuracy = super.getAccuracy();

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
			if (s.getDecibel() <= 50) {
				poly.fillColor(Color.rgb(144, 238, 144));
			} else if (s.getDecibel() > 50 && s.getDecibel() <= 90) {
				poly.fillColor(Color.rgb(255, 215, 0));
			} else {
				poly.fillColor(Color.rgb(255, 0, 0));
			}

			mMap.addPolygon(poly);
			mMap.setOnMapClickListener(arg0 -> {
				Log.d("arg0", arg0.latitude + "-" + arg0.longitude);
				double latitude = arg0.latitude;
				double longitude = arg0.longitude;
				Intent intent = new Intent(SoundActivity.this, AllDataFragment.class);
				intent.putExtra("latitude", String.valueOf(latitude));
				intent.putExtra("longitude", String.valueOf(longitude));
				intent.putExtra("TYPE", "SOUND_DATA");
				intent.putExtra("accuracy", String.valueOf(accuracy));
				startActivity(intent);
			});

		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void startRecording() throws ParseException {
		if (isMicrophonePermissionGranted()) {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
				return;
			}
			SoundHelper soundHelper = new SoundHelper(this);
			double sound = soundHelper.getSound();
			if (sound != Double.POSITIVE_INFINITY && sound != Double.NEGATIVE_INFINITY) {
				displayDecibel(sound);
				saveInDatabase(sound);
			}
		}
	}


	private void saveInDatabase(double db) throws ParseException {
		if (currentLocation == null) {
			Toast.makeText(this, "Location not available.", Toast.LENGTH_SHORT).show();
			return;
		}
		new Thread(() -> {

			double decibel = Math.floor(db * 100) / 100;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
			String time = sdf.format(new Date());

			MGRS mgrs = MGRS.from(currentLocation.longitude, currentLocation.latitude);

			String mgrs_1 = mgrs.toString();

			SoundEntry soundEntry = new SoundEntry(mgrs_1, decibel, time);

			DatabaseHelper databaseHelper = new DatabaseHelper(SoundActivity.this);

			boolean success = databaseHelper.addSoundEntry(soundEntry);
			runOnUiThread(() -> {
				if (success) {
					List<SoundEntry> newSounds = new ArrayList<>();
					newSounds.add(soundEntry);
					int userLimit = mySettings.getNumber(getApplicationContext());

					if (userLimit < sounds.size()) {
						sounds = sounds.subList(0, userLimit-1);
					}

					for (SoundEntry s : sounds) {
						if (s.getMGRS().equals(mgrs_1)) {
							newSounds.add(s);
						}
					}
					double total = 0;

					for (SoundEntry s : newSounds) {
						total += s.getDecibel();
					}
					double average = total / newSounds.size();

					try {
						colorMap(new SoundEntry(mgrs_1, average));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}).start();
	}

	private void stopRecording() {
		if (isRecording) {
			isRecording = false;
		}
	}

	private void displayDecibel(double db) {
		String decibelText = String.format("Decibel Level: %.2f dB", db);
		Toast.makeText(this, decibelText, Toast.LENGTH_SHORT).show();
	}

	// open permissions settings
	private boolean isMicrophonePermissionGranted() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
			return false;
		}
		return true;
	}


}

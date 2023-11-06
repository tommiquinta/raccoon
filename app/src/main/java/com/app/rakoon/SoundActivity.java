package com.app.rakoon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.SoundEntry;
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
import mil.nga.mgrs.tile.MGRSTileProvider;

public class SoundActivity extends MapActivity {
	// Current Location is set to Bologna
	// audio
	private boolean isRecording = false;

	public GoogleMap mMap;

	private AudioRecord audioRecord;
	private int bufferSize;
	private short[] audioData;

	private DatabaseHelper databaseHelper;

	/**
	 * MGRS tile provider
	 */
	private MGRSTileProvider tileProvider = null;

	public SoundActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// initialize database DAO
		super.onCreate(savedInstanceState);
		databaseHelper = new DatabaseHelper(SoundActivity.this);



		// get the API key
	/*	try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			String apiKey = ai.metaData.getString("com.google.android.geo.API_KEY");

			// Initializing the Places API with the help of our API_KEY
			if (!Places.isInitialized()) {
				Places.initialize(getApplicationContext(), apiKey);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

	 */
		// button to record sound decibel
		//fetchData();
		ImageButton getDecibel = findViewById(R.id.getDecibel);

		bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
		audioData = new short[bufferSize];

		getDecibel.setOnClickListener(v -> {
			if (!isRecording) {
				try {
					startRecording();
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			} else {
				stopRecording();
			}
		});
	}


	private void fetchData() {
		List<SoundEntry> sounds = databaseHelper.getSounds();
		Map<String, Double> averageDecibels;
		averageDecibels = calculateDecibelAverages(sounds);

		for (Map.Entry<String, Double> s : averageDecibels.entrySet()) {
			SoundEntry se = new SoundEntry(s.getKey(), s.getValue());
			colorMap(se);
		}

	}

	public static Map<String, Double> calculateDecibelAverages(List<SoundEntry> soundEntries) {
		Map<String, List<Double>> decibelMap = new HashMap<>();

		for (SoundEntry entry : soundEntries) {
			String MGRS = entry.getMGRS();
			// mgrs for 10 meter squares
			String sw = MGRS.substring(0, 9) + "" + MGRS.substring(10, 14);

			double decibel = entry.getDecibel();

			if (!decibelMap.containsKey(sw)) {
				decibelMap.put(sw, new ArrayList<>());
			}
			decibelMap.get(sw).add(decibel);
		}

		// Calculating the average decibel for each MGRS area
		Map<String, Double> averageDecibels = new HashMap<>();
		for (Map.Entry<String, List<Double>> entry : decibelMap.entrySet()) {
			List<Double> decibelList = entry.getValue();
			double sum = 0;
			for (Double d : decibelList) {
				sum += d;
			}
			double average = sum / decibelList.size();
			averageDecibels.put(entry.getKey(), average);
		}

		return averageDecibels;
	}

	private void colorMap(@NonNull SoundEntry s) {

		String sw = s.getMGRS();

		// this substring make the marker go on the bottom-left corner of the 10m x 10m square i'm currently in
		//Log.d("sw: ", sw.toString());

		// now mgrs is the location point in MGRS coord, i have to find the corresponding square
		VerticesHelper verticesHelper = new VerticesHelper();
		verticesHelper.setBottom_left(sw);

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
			if(s.getDecibel() <= 60){
				poly.fillColor(Color.rgb(144,238,144));
			} else if(s.getDecibel() > 60 && s.getDecibel() <= 90){
				poly.fillColor(Color.rgb(255, 215, 0));
			} else{
				poly.fillColor(Color.rgb(255, 0, 0));
			}
			mMap.addPolygon(poly);

		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void startRecording() throws ParseException {
		if (isMicrophonePermissionGranted()) {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
				return;
			}
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			audioRecord.startRecording();
			isRecording = true;
			Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show();

			// wait 1 sec before measuring
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			double amplitude = getAmplitude();
			double db = 30 * Math.log10(amplitude);
			displayDecibel(db);
			stopRecording();
			saveInDatabase(db);
		}
	}

	private void saveInDatabase(double db) {
		double decibel = Math.floor(db * 100) / 100;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
		String time = sdf.format(new Date());

		MGRS mgrs = MGRS.from(currentLocation.longitude, currentLocation.latitude);

		String mgrs_1 = mgrs.toString();

		SoundEntry soundEntry = new SoundEntry(mgrs_1, decibel, time);

		DatabaseHelper databaseHelper = new DatabaseHelper(SoundActivity.this);

		boolean success = databaseHelper.addSoundEntry(soundEntry);
		Toast.makeText(this, "Saved: " + success, Toast.LENGTH_SHORT).show();
		if(success){
			fetchData();
		}
	}

	private void stopRecording() {
		if (isRecording) {
			isRecording = false;
			audioRecord.stop();
			audioRecord.release();
		}
	}


	private double getAmplitude() {
		double maxAmplitude = 0;

		audioRecord.read(audioData, 0, bufferSize);
		for (short s : audioData) {
			if (Math.abs(s) > maxAmplitude) {
				maxAmplitude = Math.abs(s);
			}
		}
		return maxAmplitude;
	}

	private void displayDecibel(double db) {
		String decibelText = String.format("Decibel Level: %.2f dB", db);
		Toast.makeText(this, decibelText, Toast.LENGTH_SHORT).show();
	}

	private boolean isMicrophonePermissionGranted() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
			return false;
		}
		return true;
	}
}

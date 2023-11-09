package com.app.rakoon.Services;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.SoundEntry;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import mil.nga.grid.features.Point;
import mil.nga.mgrs.MGRS;

public class SoundService extends Service {

	private AudioRecord audioRecord;
	private int bufferSize;
	private short[] audioData;
	private BroadcastReceiver locationReceiver;



	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		locationReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() != null && intent.getAction().equals("LOCATION_UPDATE")) {
					Location location = intent.getParcelableExtra("location");
					handleLocationUpdate(location);
				}
			}
		};

		registerReceiver(locationReceiver, new IntentFilter("LOCATION_UPDATE"));

		return START_STICKY;
	}

	private void handleLocationUpdate(Location location) {
		// Gestisci la posizione aggiornata qui
		LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
		try {
			startRecording(currentLocation);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void startRecording(LatLng currentLocation) throws ParseException {
		if (isMicrophonePermissionGranted()) {
			// Initialize audio recording parameters
			bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
			audioData = new short[bufferSize];

			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

				return;
			}
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			audioRecord.startRecording();

			// wait 1 sec before measuring
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			double amplitude = getAmplitude();
			double db = 30 * Math.log10(amplitude);

			stopRecording();
			saveInDatabase(db, currentLocation);
		}
	}

	private void stopRecording() {
		if (audioRecord != null) {
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

	private void saveInDatabase(double db, LatLng currentLocation) throws ParseException {
		// Perform database operations here
		// You can use the code from the original SoundActivity's saveInDatabase method

		// For example:
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		MGRS mgrs = MGRS.from(currentLocation.longitude, currentLocation.latitude);
		String mgrsString = mgrs.toString();

		double decibel = Math.floor(db * 100) / 100;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
		String time = sdf.format(new Date());

		SoundEntry soundEntry = new SoundEntry(mgrsString, decibel, time);

		boolean success = databaseHelper.addSoundEntry(soundEntry);
		showToast("Saved in database: " + success);
		stopSelf();

	}

	// Helper method to display a toast
	private void showToast(final String message) {
		if (getApplicationContext() != null) {
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		}
	}

	// open permissions settings
	private boolean isMicrophonePermissionGranted() {
		// Check and request microphone permission if not granted
		// You can use the code from the original SoundActivity's isMicrophonePermissionGranted method
		return true;  // Replace with the actual permission check result
	}
}

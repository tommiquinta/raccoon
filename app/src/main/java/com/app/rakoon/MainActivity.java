package com.app.rakoon;
// MainActivity.java

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.app.rakoon.Services.MyService;

//import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
	private final int PERMISSION_ID = 42;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		requestPermissions();

		/**
		 *
		 * testins the service
		 */

		/**
		 * sound map activity
		 */
		Button btnGoToSoundActivity = findViewById(R.id.createSoundMapButton);

		btnGoToSoundActivity.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, SoundActivity.class);
			startActivity(intent);

		});

		/**
		 * wifi map activity
		 */
		Button btnGoToWiFIActivity = findViewById(R.id.createWiFiMapButton);

		btnGoToWiFIActivity.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, WiFiActivity.class);
			startActivity(intent);

		});

		/**
		 * signal map activity
		 */
		Button btnGoToCellularActivity = findViewById(R.id.createSignalMapButton);

		btnGoToCellularActivity.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, SignalActivity.class);
			startActivity(intent);

		});

		/**
		 * data activity
		 */
		Button goToData = findViewById(R.id.data);

		goToData.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, YourDataActivity.class);
			startActivity(intent);
		});

		/**
		 * settings activity
		 */

		Button goToSettings = findViewById(R.id.settings);

		goToSettings.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(intent);
		});

	}

	// Check if notifications permissions are granted to the application
	private boolean checkPermissions() {
		return ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}

	// Request permissions if not granted before
	private void requestPermissions() {
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
	}

}

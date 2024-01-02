package com.app.rakoon;
// MainActivity.java

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.BackoffPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.app.rakoon.Helpers.LocationManager;
import com.app.rakoon.Helpers.LocationWork;
import com.app.rakoon.Helpers.PermissionManager;
import com.app.rakoon.Services.MyService;

import java.util.concurrent.TimeUnit;

//import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
	private final int PERMISSION_ID = 42;

	public Activity activity;

	private final String[] foreground_location_permission = {Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION};

	private final String[] background_location_permission = {Manifest.permission.ACCESS_BACKGROUND_LOCATION};

	private PermissionManager permissionManager;
	private LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		activity = MainActivity.this;

		permissionManager = PermissionManager.getInstance(this);
		locationManager = LocationManager.getInstance(this);


		Button fore = findViewById(R.id.foregorund);
		Button back = findViewById(R.id.back);

		fore.setOnClickListener(v -> {
			if (!permissionManager.checkPermissions(foreground_location_permission)) {
				permissionManager.askPermissions(MainActivity.this, foreground_location_permission, 200);
			}
		});

		back.setOnClickListener(v -> {

			if (!permissionManager.checkPermissions(background_location_permission)) {
				permissionManager.askPermissions(MainActivity.this, background_location_permission, 200);
			} else {
				if (locationManager.isLocationEnabled()) {
					startLocationWOrk();
				} else {
					locationManager.createLocationRequest();
					Toast.makeText(MainActivity.this, "Location service is not enabled", Toast.LENGTH_SHORT).show();
				}
			}
		});



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

	private void startLocationWOrk() {
		OneTimeWorkRequest foregroundRequest = new OneTimeWorkRequest.Builder(LocationWork.class)
				.addTag("Location Work")
				.setBackoffCriteria(BackoffPolicy.LINEAR,
						OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
				.build();

		WorkManager.getInstance(MainActivity.this).enqueue(foregroundRequest);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {

		super.onRequestPermissionsResult(requestCode, permissions, grantResult);
		if (permissionManager.handlePermissionResult(MainActivity.this, 100, permissions, grantResult)) {
			Log.d("TAG", "1");
			if (locationManager.isLocationEnabled()) {
				startLocationWOrk();
			} else {
				locationManager.createLocationRequest();
				Toast.makeText(MainActivity.this, "Not enabled", Toast.LENGTH_SHORT).show();
			}
		}
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

package com.app.rakoon.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.app.rakoon.Fragments.mySettings;
import com.app.rakoon.R;
import com.app.rakoon.Services.Constants;
import com.app.rakoon.Services.MyService;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

	private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1;
	private static final int REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION = 2;
	private SharedPreferences.OnSharedPreferenceChangeListener listener;

	private boolean isLocationServiceRunning() {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		if (activityManager != null) {
			for (ActivityManager.RunningServiceInfo service :
					activityManager.getRunningServices(Integer.MAX_VALUE)) {
				if (MyService.class.getName().equals(service.service.getClassName())) {
					if (service.foreground) {
						return true;
					}
				}
			}
			return false;
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);

		Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.settings_custom_bar);

		askNotificationPermission();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		listener = (sharedPreferences1, key) -> {
			if (key.equals("background")) {
				boolean notificationsEnabled = sharedPreferences1.getBoolean("background", false);

				if (notificationsEnabled) {
					if (checkBackgroundLocationPermission()) {
						startService();
					} else {
						requestBackgroundLocationPermission();
					}
				} else {
					stopService();
				}
			}
		};

		sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings_container, new mySettings())
				.commit();
	}



	private void askNotificationPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.POST_NOTIFICATIONS},
					REQUEST_CODE_NOTIFICATION_PERMISSION);
		}
	}

	private boolean checkBackgroundLocationPermission() {
		return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
				== PackageManager.PERMISSION_GRANTED;
	}

	private void requestBackgroundLocationPermission() {
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
				REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION);
	}

	public void startService() {
		if (!isLocationServiceRunning()) {
			Intent serviceIntent = new Intent(this, MyService.class);
			serviceIntent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
			serviceIntent.putExtra("type", "x");
			startService(serviceIntent);
			Toast.makeText(this, "Location Service Enabled", Toast.LENGTH_SHORT).show();
		}
	}

	public void stopService() {
		if (isLocationServiceRunning()) {
			Intent serviceIntent = new Intent(this, MyService.class);
			serviceIntent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
			stopService(serviceIntent);
			Toast.makeText(this, "Location Service Disabled", Toast.LENGTH_SHORT).show();
		}
	}

	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startService();
			} else {
				Toast.makeText(this, "Permission denied. Please enable background location in your device settings.", Toast.LENGTH_SHORT).show();
			}
		}
	}
}

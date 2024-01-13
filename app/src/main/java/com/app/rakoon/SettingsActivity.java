package com.app.rakoon;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.app.rakoon.Fragments.Settings;
import com.app.rakoon.Services.Constants;
import com.app.rakoon.Services.MyService;

public class SettingsActivity extends AppCompatActivity {

	private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
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

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		listener = (sharedPreferences1, key) -> {

			switch (key) {
				case "SoundBG": {
					askNotificationsPermission();
					boolean permissionGiven = checkPermission();
					if (permissionGiven) {
						boolean notificationsEnabled = sharedPreferences1.getBoolean("SoundBG", false);

						if (notificationsEnabled) {
							startService(Constants.SOUND);
						} else {
							stopService(Constants.SOUND);
						}
						break;
					} else {
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putBoolean("mute", false);
						editor.apply();
						Toast.makeText(this, "Please allow Push Notifications permission in you device's Settings.", Toast.LENGTH_SHORT).show();
					}
					break;
				}
				case "WiFiBG": {
					boolean notificationsEnabled = sharedPreferences1.getBoolean("WiFiBG", false);
					if (notificationsEnabled) {
						startService(Constants.WIFI);
					} else {
						stopService(Constants.WIFI);
					}
					break;
				}
				case "SignalBG": {
					boolean notificationsEnabled = sharedPreferences1.getBoolean("SignalBG", false);
					if (notificationsEnabled) {
						startService(Constants.SIGNAL);
					} else {
						stopService(Constants.SIGNAL);
					}
					break;
				}

			}
		};
		sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings_container, new Settings())
				.commit();
	}

	private boolean checkPermission() {
		return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
	}

	private boolean askNotificationsPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
			return false;
		}
		return true;
	}


	public void startService(String type) {
		if (!isLocationServiceRunning()) {
			Intent serviceIntent = new Intent(this, MyService.class);
			serviceIntent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
			serviceIntent.putExtra("type", type);
			startService(serviceIntent);
			Toast.makeText(this, "Location Service Enabled", Toast.LENGTH_SHORT).show();
		}
	}

	public void stopService(String type) {
		if (isLocationServiceRunning()) {
			Intent serviceIntent = new Intent(this, MyService.class);
			serviceIntent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
			startService(serviceIntent);
			Toast.makeText(this, "Location Service Disabled", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(this, "Notifications Permission given.", Toast.LENGTH_SHORT).show();

			boolean notificationsEnabled = sharedPreferences.getBoolean("SoundBG", false);
			if (notificationsEnabled) {
				startService(Constants.SOUND);
			} else {
				stopService(Constants.SOUND);
			}
		}
	}

}

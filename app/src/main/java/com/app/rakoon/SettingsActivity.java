package com.app.rakoon;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;


import com.app.rakoon.Fragments.Settings;
import com.app.rakoon.Services.Constants;
import com.app.rakoon.Services.MyService;
import com.app.rakoon.databinding.ActivityMapBinding;
import com.google.android.gms.location.LocationServices;

public class SettingsActivity extends AppCompatActivity {

	private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;




	private SharedPreferences.OnSharedPreferenceChangeListener listener;
	private boolean isLocationServiceRunning() {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		if(activityManager != null){
			for(ActivityManager.RunningServiceInfo service:
				activityManager.getRunningServices(Integer.MAX_VALUE)) {
				if(MyService.class.getName().equals(service.service.getClassName())){
					if(service.foreground){
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
		setContentView(R.layout.settings_layout); // Assicurati che il layout sia corretto

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		listener = (sharedPreferences1, key) -> {

			if (key.equals("notifications")) {
				// Esegui le azioni necessarie quando la preferenza "notifications" cambia
				boolean notificationsEnabled = sharedPreferences1.getBoolean("notifications", false);

				if (notificationsEnabled) {
					startService();
				} else {
					stopService();
				}
			}
		};
		sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings_container, new Settings())
				.commit();
	}


	public void startService() {
		if(!isLocationServiceRunning()){
			Intent serviceIntent = new Intent(this, MyService.class);
			serviceIntent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
			startService(serviceIntent);
			Toast.makeText(this, "Location Service Enabled", Toast.LENGTH_SHORT).show();
		}
	}

	public void stopService() {
		if(isLocationServiceRunning()) {
			Intent serviceIntent = new Intent(this, MyService.class);
			serviceIntent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
			startService(serviceIntent);
			Toast.makeText(this, "Location Service Disabled", Toast.LENGTH_SHORT).show();
		}
	}


}

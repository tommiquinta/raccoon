package com.app.rakoon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.app.rakoon.Fragments.Settings;
import com.app.rakoon.Services.MyService;

public class SettingsActivity extends AppCompatActivity {

	private SharedPreferences.OnSharedPreferenceChangeListener listener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout); // Assicurati che il layout sia corretto



		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		listener = (sharedPreferences1, key) -> {

			if (key.equals("sound")) {
				// Esegui le azioni necessarie quando la preferenza "notifications" cambia
				boolean notificationsEnabled = sharedPreferences1.getBoolean("sound", false);

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

		Intent serviceIntent = new Intent(this, MyService.class);
		serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
		ContextCompat.startForegroundService(this, serviceIntent);
	}

	public void stopService() {
		Intent serviceIntent = new Intent(this, MyService.class);
		stopService(serviceIntent);
	}


}

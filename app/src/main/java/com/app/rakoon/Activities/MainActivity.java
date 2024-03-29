package com.app.rakoon.Activities;
// MainActivity.java

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.rakoon.R;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {
	private final int PERMISSION_ID = 42;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.custom_action_bar);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			requestPermissions();
		}

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
			Intent intent = new Intent(MainActivity.this, MyDataActivity.class);
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

	// Request permissions if not granted before
	@RequiresApi(api = Build.VERSION_CODES.P)
	private void requestPermissions() {
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.FOREGROUND_SERVICE}, PERMISSION_ID);
	}

}

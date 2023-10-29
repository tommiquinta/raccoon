package com.app.rakoon;
// MainActivity.java

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

//import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/**
		 * sound map activity
		 */
		Button btnGoToSoundActivity = findViewById(R.id.createSoundMapButton);

		btnGoToSoundActivity.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, MapActivity.class);
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
}

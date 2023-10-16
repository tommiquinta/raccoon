package com.app.rakoon;
// MainActivity.java

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Trova il riferimento al bottone
		Button btnGoToSoundActivity = findViewById(R.id.createSoundMapButton);

		// Aggiungi il listener per il click del bottone
		btnGoToSoundActivity.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, MapActivity.class);
				startActivity(intent);

			}
		});
	}
}

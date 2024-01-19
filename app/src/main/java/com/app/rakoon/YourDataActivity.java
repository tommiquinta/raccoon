package com.app.rakoon;

import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.SignalEntry;
import com.app.rakoon.Database.SoundEntry;
import com.app.rakoon.Database.WifiEntry;

import java.util.List;

public class YourDataActivity extends AppCompatActivity {
	private ListView data_view;
	DatabaseHelper databaseHelper;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data_layout);
		databaseHelper = new DatabaseHelper(YourDataActivity.this);
		/*
		updateView();

		data_view.setOnItemClickListener((parent, view, position, l) -> {
			SignalEntry soundEntry = (SignalEntry) parent.getItemAtPosition(position);
			databaseHelper.deleteOne(soundEntry);
			updateView();
		});
		*/
		Button delete_sound = findViewById(R.id.delete_sound);

		delete_sound.setOnClickListener(v -> {
			delete_sound();
		});

		Button delete_wifi = findViewById(R.id.delete_wifi);

		delete_wifi.setOnClickListener(v -> {
			delete_wifi();
		});

		Button delete_signal = findViewById(R.id.delete_signal);

		delete_signal.setOnClickListener(v -> {
			delete_signal();
		});

	}

	private void delete_signal() {
		databaseHelper.deleteSignal();
		Toast.makeText(this, "Data deleted successfully", Toast.LENGTH_SHORT).show();
	}

	private void delete_wifi() {
		databaseHelper.deleteWifi();
		Toast.makeText(this, "Data deleted successfully", Toast.LENGTH_SHORT).show();

	}

	private void delete_sound() {
		databaseHelper.deleteSunds();
		Toast.makeText(this, "Data deleted successfully", Toast.LENGTH_SHORT).show();
	}

	protected void updateView() {
		/*
		data_view = findViewById(R.id.data_view);
		DatabaseHelper databaseHelper = new DatabaseHelper(YourDataActivity.this);
		List<SignalEntry> sounds = databaseHelper.getSignals();
		//Toast.makeText(this, "Size: " + sounds.size(), Toast.LENGTH_SHORT).show();
		ArrayAdapter<SignalEntry> arrayAdapter = new ArrayAdapter<SignalEntry>(YourDataActivity.this, android.R.layout.simple_list_item_1, sounds);
		data_view.setAdapter(arrayAdapter);
		 */
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}

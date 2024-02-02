package com.app.rakoon.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.R;

import java.util.Objects;

public class MyDataActivity extends AppCompatActivity {
	private ListView data_view;
	DatabaseHelper databaseHelper;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data_layout);

		Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.data_custom_bar);

		databaseHelper = new DatabaseHelper(MyDataActivity.this);

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

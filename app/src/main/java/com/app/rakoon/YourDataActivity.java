package com.app.rakoon;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.SignalEntry;
import com.app.rakoon.Database.SoundEntry;
import com.app.rakoon.Database.WifiEntry;
import com.app.rakoon.Services.SoundService;

import java.util.List;

public class YourDataActivity extends AppCompatActivity {
	private ListView data_view;

	@Override

	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data_layout);
		DatabaseHelper databaseHelper = new DatabaseHelper(YourDataActivity.this);
		updateView();

		data_view.setOnItemClickListener((parent, view, position, l) -> {
			SoundEntry soundEntry = (SoundEntry) parent.getItemAtPosition(position);
			databaseHelper.deleteOne(soundEntry);
			updateView();
		});
	}

	protected void updateView() {
		data_view = findViewById(R.id.data_view);

		DatabaseHelper databaseHelper = new DatabaseHelper(YourDataActivity.this);
		List<SoundEntry> sounds = databaseHelper.getSounds();

		//Toast.makeText(this, "Size: " + sounds.size(), Toast.LENGTH_SHORT).show();

		ArrayAdapter<SoundEntry> arrayAdapter = new ArrayAdapter<SoundEntry>(YourDataActivity.this, android.R.layout.simple_list_item_1, sounds);
		data_view.setAdapter(arrayAdapter);
	}


}

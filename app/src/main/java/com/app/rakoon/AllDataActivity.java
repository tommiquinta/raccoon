package com.app.rakoon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.Entry;
import com.app.rakoon.Database.SignalEntry;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mil.nga.mgrs.MGRS;

public class AllDataActivity extends AppCompatActivity {
	String lat;
	String lon;
	String accuracy;
	String type;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_data_layout);

		AdapterView<Adapter> data_view = findViewById(R.id.data_view);
		DatabaseHelper databaseHelper = new DatabaseHelper(this);

		data_view.setOnItemClickListener((parent, view, position, l) -> {
			SignalEntry soundEntry = (SignalEntry) parent.getItemAtPosition(position);
			databaseHelper.deleteOne(soundEntry);
			try {
				updateView();
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		});

		Intent myIntent = getIntent();

		lat = myIntent.getStringExtra("latitude");
		lon = myIntent.getStringExtra("longitude");
		accuracy = myIntent.getStringExtra("accuracy");
		type = myIntent.getStringExtra("TYPE");

		try {
			updateView();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	protected void updateView() throws ParseException {
		DatabaseHelper databaseHelper = new DatabaseHelper(AllDataActivity.this);
		List<? extends Entry> entryList = null;
		List<Entry> new_list = new ArrayList<>();

		switch (type) {
			case "SIGNAL_DATA":
				entryList = databaseHelper.getSignals();
				break;
			case "SOUND_DATA":
				entryList = databaseHelper.getSounds();
				break;
			case "WIFI_DATA":
				entryList = databaseHelper.getWiFi();
				break;
		}

		AdapterView<Adapter> data_view = findViewById(R.id.data_view);

		MGRS mgrs = MGRS.from(Double.parseDouble(lon), Double.parseDouble(lat));

		String bottom_left = String.valueOf(mgrs);

		int acc = Integer.parseInt(accuracy);

		long easting = MGRS.parse(bottom_left).getEasting();
		easting = easting / acc;
		long northing = MGRS.parse(bottom_left).getNorthing();
		northing = northing / acc;

		bottom_left = bottom_left.substring(0, 5) + easting + northing;


		for(Entry s: entryList){
			String new_bottom_left = String.valueOf(s.getMGRS());

			long new_easting = MGRS.parse(new_bottom_left).getEasting();
			new_easting = new_easting / acc;
			long new_northing = MGRS.parse(new_bottom_left).getNorthing();
			new_northing = new_northing / acc;

			new_bottom_left = new_bottom_left.substring(0, 5) + new_easting + new_northing;

			if(new_bottom_left.equals(bottom_left)){
				new_list.add(s);
			}
		}

		ArrayAdapter<Entry> arrayAdapter = new ArrayAdapter<Entry>(AllDataActivity.this, android.R.layout.simple_list_item_1, new_list);
		data_view.setAdapter(arrayAdapter);

	}
}

package com.app.rakoon.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.Entry;
import com.app.rakoon.Database.SignalEntry;
import com.app.rakoon.Database.SoundEntry;
import com.app.rakoon.Database.WifiEntry;
import com.app.rakoon.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mil.nga.mgrs.MGRS;

public class AllDataFragment extends FragmentActivity {
	String lat;
	String lon;
	String accuracy;
	String type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_data_layout);

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
		DatabaseHelper databaseHelper = new DatabaseHelper(AllDataFragment.this);
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


		assert entryList != null;
		for (Entry s : entryList) {
			String new_bottom_left = String.valueOf(s.getMGRS());

			long new_easting = MGRS.parse(new_bottom_left).getEasting();
			new_easting = new_easting / acc;
			long new_northing = MGRS.parse(new_bottom_left).getNorthing();
			new_northing = new_northing / acc;

			new_bottom_left = new_bottom_left.substring(0, 5) + new_easting + new_northing;

			if (new_bottom_left.equals(bottom_left)) {
				new_list.add(s);
			}
		}

		if (new_list.isEmpty()) {
			TextView textView = findViewById(R.id.average);
			textView.setText("no data for this zone");
			textView.setVisibility(View.VISIBLE);
			return;
		} else {
			TextView textView = findViewById(R.id.average);
			textView.setVisibility(View.VISIBLE);
			double total = 0;

			int userLimit = mySettings.getNumber(getApplicationContext());
			if (new_list.size() > userLimit) {
				new_list = new_list.subList(0, userLimit);
			}

			for (Entry e : new_list) {
				switch (type) {
					case "SIGNAL_DATA":
						SignalEntry s = (SignalEntry) e;
						total += s.getSignal();
						break;
					case "SOUND_DATA":
						SoundEntry se = (SoundEntry) e;
						total += se.getDecibel();
						break;
					case "WIFI_DATA":
						WifiEntry we = (WifiEntry) e;
						total += we.getWifi();
						break;
				}
			}
			double average = total / new_list.size();
			String averageString = String.valueOf(average);

			if (averageString.length() > 5) {
				averageString = averageString.substring(0, 5);
			}

			if (new_list.size() < userLimit) {
				textView.setText("Average of all measurements:\n" + averageString);
			} else {
				textView.setText("Average of the last " + userLimit + " measurements:\n" + averageString);
			}
		}

		ArrayAdapter<Entry> arrayAdapter = new ArrayAdapter<Entry>(AllDataFragment.this, android.R.layout.simple_list_item_1, new_list);
		data_view.setAdapter(arrayAdapter);
	}
}
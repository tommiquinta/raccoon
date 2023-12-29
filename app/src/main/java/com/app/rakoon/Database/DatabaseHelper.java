package com.app.rakoon.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

// this is the DAO class, the one that will do all the operations
public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String SOUND_DATA = "SOUND_DATA";
	public static final String SIGNAL_DATA = "SIGNAL_DATA";
	public static final String WIFI_DATA = "WIFI_DATA";
	public static final String DECIBEL = "DECIBEL";
	public static final String MGRS = "MGRS";
	public static final String TIME = "TIME";
	public static final String ID = "ID";
	public static final String SIGNAL = "SIGNAL";
	public static final String WIFI = "WIFI";

	public DatabaseHelper(@Nullable Context context) {
		super(context, "user_data.db", null, 6);
	}

	// called first time the db is accessed --> code to create an app
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createSoundTableStatement = "CREATE TABLE " + SOUND_DATA + " (" + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " + MGRS + " TEXT, " + DECIBEL + " FLOAT, " + TIME + " TEXT)";

		db.execSQL(createSoundTableStatement);
	}

	// update the database if app gets updated, used for compatibility
	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

	}

	// INSERT A SOUND ENTRY
	public boolean addSoundEntry(@NonNull SoundEntry soundEntry) {
		SQLiteDatabase db = this.getWritableDatabase(); // with WRITE, the db is locked. no other process can update  or write, creating a bottleneck

		ContentValues cv = new ContentValues(); // hashmap

		cv.put(MGRS, soundEntry.getMGRS());
		cv.put(DECIBEL, soundEntry.getDecibel());
		cv.put(TIME, soundEntry.getTime());

		long insert = db.insert(SOUND_DATA, null, cv);

		if (insert == -1) {
			return false;
		} else {
			return true;
		}
	}

	public boolean addSignalEntry(@NonNull SignalEntry signalEntry) {
		SQLiteDatabase db = this.getWritableDatabase(); // with WRITE, the db is locked. no other process can update  or write, creating a bottleneck
		ContentValues cv = new ContentValues(); // hashmap

		cv.put(MGRS, signalEntry.getMGRS());
		cv.put(SIGNAL, signalEntry.getSignal());
		cv.put(TIME, signalEntry.getTime());

		long insert = db.insert(SIGNAL_DATA, null, cv);

		if (insert == -1) {
			return false;
		} else {
			return true;
		}
	}

	public boolean addWifiEntry(@NonNull WifiEntry wifiEntry) {
		SQLiteDatabase db = this.getWritableDatabase(); // with WRITE, the db is locked. no other process can update  or write, creating a bottleneck
		ContentValues cv = new ContentValues(); // hashmap

		cv.put(MGRS, wifiEntry.getMGRS());
		cv.put(WIFI, wifiEntry.getWifi());
		cv.put(TIME, wifiEntry.getTime());

		long insert = db.insert(WIFI_DATA, null, cv);

		if (insert == -1) {
			return false;
		} else {
			return true;
		}
	}

	// GET ALL SOUNDS ENTRIES
	public List<SoundEntry> getSounds() {
		List<SoundEntry> soundData = new ArrayList<>();
		String query = "SELECT * FROM " + SOUND_DATA + " ORDER BY ID DESC";

		SQLiteDatabase db = this.getReadableDatabase(); // with READ, no bottleneck is created
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.moveToFirst()) {
			// loop through the results and create a new object, then returning it to the list
			do {
				int id = cursor.getInt(0);
				String MGRS = cursor.getString(1);
				double decibel = cursor.getDouble(2);
				String time = cursor.getString(3);

				SoundEntry se = new SoundEntry(id, MGRS, decibel, time);
				soundData.add(se);
			} while (cursor.moveToNext());  // proceed to the db one at a time
		} else {
			// add nothing to list
		}
		cursor.close();
		db.close();
		return soundData;
	}

	public List<SignalEntry> getSignals() {
		List<SignalEntry> signalData = new ArrayList<>();
		String query = "SELECT * FROM " + SIGNAL_DATA + " ORDER BY ID DESC";

		SQLiteDatabase db = this.getReadableDatabase(); // with READ, no bottleneck is created
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.moveToFirst()) {
			// loop through the results and create a new object, then returning it to the list
			do {
				int id = cursor.getInt(0);
				String MGRS = cursor.getString(1);
				int signal = cursor.getInt(2);
				String time = cursor.getString(3);

				SignalEntry sd = new SignalEntry(id, MGRS, signal, time);
				signalData.add(sd);
			} while (cursor.moveToNext());  // proceed to the db one at a time
		} else {
			// add nothing to list
		}
		cursor.close();
		db.close();
		return signalData;
	}

	public List<WifiEntry> getWiFi() {
		List<WifiEntry> wifiData = new ArrayList<>();
		String query = "SELECT * FROM " + WIFI_DATA + " ORDER BY ID DESC";

		SQLiteDatabase db = this.getReadableDatabase(); // with READ, no bottleneck is created
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.moveToFirst()) {
			// loop through the results and create a new object, then returning it to the list
			do {
				int id = cursor.getInt(0);
				String MGRS = cursor.getString(1);
				double wifi = cursor.getDouble(2);
				String time = cursor.getString(3);

				WifiEntry we = new WifiEntry(id, MGRS, wifi, time);
				wifiData.add(we);
			} while (cursor.moveToNext());  // proceed to the db one at a time
		} else {
			// add nothing to list
		}
		cursor.close();
		db.close();
		return wifiData;
	}

	// DELETE ONE SOUND ENTRY
	public boolean deleteOne(SoundEntry soundEntry) {
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "DELETE FROM " + SOUND_DATA + " WHERE  " + ID + " = " + soundEntry.getId();

		Cursor cursor = db.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean deleteOne(SignalEntry signalEntry) {
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "DELETE FROM " + SIGNAL_DATA + " WHERE  " + ID + " = " + signalEntry.signal_id;

		Cursor cursor = db.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean deleteOne(WifiEntry wifiEntry) {
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "DELETE FROM " + WIFI_DATA + " WHERE  " + ID + " = " + wifiEntry.getId();

		Cursor cursor = db.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			return true;
		} else {
			return false;
		}
	}
}

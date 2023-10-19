package com.app.rakoon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

// this is the DAO class, the one that will do all the operations
public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String SOUND_DATA = "SOUND_DATA";
	public static final String LATITUDE = "LATITUDE";
	public static final String LONGITUDE = "LONGITUDE";
	public static final String DECIBEL = "DECIBEL";
	public static final String ID = "ID";

	public DatabaseHelper(@Nullable Context context) {
		super(context, "user_data.db", null, 1);
	}

	// called first time the db is accessed --> code to create an app
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createTableStatement = "CREATE TABLE " + SOUND_DATA + " (" + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " + LATITUDE + " FLOAT, " + LONGITUDE + " FLOAT, " + DECIBEL + " FLOAT)";

		db.execSQL(createTableStatement);
	}

	// update the database if app gets updated, used for compatibility
	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

	}

	// INSERT A SOUND ENTRY
	public boolean addEntry(@NonNull SoundEntry soundEntry) {
		SQLiteDatabase db = this.getWritableDatabase(); // with WRITE, the db is locked. no other process can update  or write, creating a bottleneck
		ContentValues cv = new ContentValues(); // hashmap

		cv.put(LATITUDE, soundEntry.getLatitude());
		cv.put(LONGITUDE, soundEntry.getLongitude());
		cv.put(DECIBEL, soundEntry.getDecibel());

		long insert = db.insert(SOUND_DATA, null, cv);

		if (insert == -1) {
			return false;
		} else {
			return true;
		}
	}

	// GET ALL SOUNDS ENTRIES
	public List<SoundEntry> getSounds() {
		List<SoundEntry> soundData = new ArrayList<>();
		String query = "SELECT * FROM " + SOUND_DATA;

		SQLiteDatabase db = this.getReadableDatabase(); // with READ, no bottleneck is created
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.moveToFirst()) {
			// loop through the results and create a new object, then returning it to the list
			do {
				int id = cursor.getInt(0);
				double latitude = cursor.getDouble(1);
				double longitude = cursor.getDouble(2);
				double decibel = cursor.getDouble(3);

				SoundEntry se = new SoundEntry(id, latitude, longitude, decibel);
				soundData.add(se);
			} while (cursor.moveToNext());  // proceed to the db one at a time
		} else {
			// add nothing to list
		}
		cursor.close();
		db.close();
		return soundData;
	}


	// DELETE ONE SOUND ENTRY
	public boolean deleteOne(SoundEntry soundEntry) {
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "DELETE FROM " + SOUND_DATA + " WHERE  " + ID + " = " + soundEntry.getSound_id();

		Cursor cursor = db.rawQuery(query, null);
		if (cursor.moveToFirst()) {

			return true;

		} else {

			return false;
		}
	}
}

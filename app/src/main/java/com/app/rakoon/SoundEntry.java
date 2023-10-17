package com.app.rakoon;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Sound")
public class SoundEntry {
	@PrimaryKey(autoGenerate = true)
	int sound_id;

	@ColumnInfo(name = "latitude")
	double latitude;

	@ColumnInfo(name = "longitude")
	double longitude;

	@ColumnInfo(name = "decibel")
	double decibel;

	@Ignore
	public SoundEntry() {
	}

	public SoundEntry(double latitude, double longitude, double decibel) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.decibel = decibel;
		this.sound_id = 0;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getDecibel() {
		return decibel;
	}

	public void setDecibel(double decibel) {
		this.decibel = decibel;
	}
}

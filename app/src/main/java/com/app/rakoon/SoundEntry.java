package com.app.rakoon;


import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SoundEntry {
	int sound_id;

	double latitude;

	double longitude;
	String MGRS;

	double decibel;

	SimpleDateFormat time;

	String timeString;

	public SoundEntry() {
	}

	public SoundEntry(String MGRS, double decibel, String timeString) {
		this.MGRS = MGRS;
		this.decibel = decibel;
		this.timeString = timeString;
	}

	public SoundEntry(int sound_id, double latitude, double longitude, double decibel, String timeString) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.decibel = decibel;
		this.sound_id = sound_id;
		this.timeString = timeString;
	}

	public SoundEntry(int sound_id, String MGRS, double decibel, String timeString) {
		this.sound_id = sound_id;
		this.MGRS = MGRS;
		this.decibel = decibel;
		this.timeString = timeString;
	}

	public int getSound_id() {
		return this.sound_id;
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

	public String getTime() {
		return timeString;
	}

	public void setTime(SimpleDateFormat time) {
		this.time = time;
	}


	public String getMGRS() {
		return MGRS;
	}

	public void setMGRS(String MGRS) {
		this.MGRS = MGRS;
	}
	@Override
	public String toString() {
		return
				"ID=" + sound_id +
						", MGRS=" + MGRS +
						", DB=" + decibel +
						", TIME= " + timeString;
	}
}

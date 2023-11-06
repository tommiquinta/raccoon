package com.app.rakoon.Database;


import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SoundEntry implements Entry {
	private int sound_id;
	private String MGRS;
	private double decibel;
	private String timeString;

	public SoundEntry() {
	}

	public SoundEntry(String MGRS, double decibel, String timeString) {
		this.MGRS = MGRS;
		this.decibel = decibel;
		this.timeString = timeString;
	}

	public SoundEntry(int sound_id, String MGRS, double decibel, String timeString) {
		this.sound_id = sound_id;
		this.MGRS = MGRS;
		this.decibel = decibel;
		this.timeString = timeString;
	}

	public SoundEntry(String MGRS, Double decibel) {
		this.MGRS = MGRS;
		this.decibel = decibel;
	}

	public int getId() {
		return sound_id;
	}

	public double getDecibel() {
		return decibel;
	}

	public String getTime() {
		return timeString;
	}

	public String getMGRS() {
		return MGRS;
	}

	@Override
	public String toString() {
		return "ID=" + sound_id + ", MGRS=" + MGRS + ", DB=" + decibel + ", TIME= " + timeString;
	}

}

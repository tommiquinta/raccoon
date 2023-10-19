package com.app.rakoon;


public class SoundEntry {
	int sound_id;

	double latitude;

	double longitude;

	double decibel;

	public SoundEntry() {
	}

	public SoundEntry(double latitude, double longitude, double decibel) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.decibel = decibel;
	}

	public SoundEntry(int sound_id, double latitude, double longitude, double decibel) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.decibel = decibel;
		this.sound_id = sound_id;
	}

	public int getSound_id() {
		return sound_id;
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

	@Override
	public String toString() {
		return
				"ID=" + sound_id +
						", LAT=" + latitude +
						", LONG=" + longitude +
						", DB=" + decibel;
	}
}

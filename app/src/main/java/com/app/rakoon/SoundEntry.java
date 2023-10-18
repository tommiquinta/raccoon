package com.app.rakoon;


public class SoundEntry {
	int sound_id;

	double latitude;

	double longitude;

	double decibel;

	public SoundEntry() {}

	public SoundEntry(double latitude, double longitude, double decibel) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.decibel = decibel;
		this.sound_id = 0;
	}

	public SoundEntry(int sound_id, double latitude, double longitude, double decibel) {
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

	@Override
	public String toString() {
		return "SoundEntry{" +
				"sound_id=" + sound_id +
				", latitude=" + latitude +
				", longitude=" + longitude +
				", decibel=" + decibel +
				'}';
	}
}

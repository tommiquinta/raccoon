package com.app.rakoon.Database;

public class WifiEntry implements Entry {
	int wifi_id;
	String MGRS;
	double wifi;
	String timeString;

	public WifiEntry() {
	}

	public WifiEntry(String MGRS, double wifi, String timeString) {
		this.MGRS = MGRS;
		this.wifi = wifi;
		this.timeString = timeString;
	}

	public WifiEntry(int wifi_id, String MGRS, double wifi, String timeString) {
		this.wifi_id = wifi_id;
		this.MGRS = MGRS;
		this.wifi = wifi;
		this.timeString = timeString;
	}

	public WifiEntry(String MGRS, double wifi) {
		this.MGRS = MGRS;
		this.wifi = wifi;
	}


	public double getWifi() {
		return wifi;
	}

	@Override
	public String getTime() {
		return timeString;
	}

	@Override
	public int getId() {
		return wifi_id;
	}

	public String getMGRS() {
		return MGRS;
	}


	@Override
	public String toString() {
		return
				"ID=" + wifi_id +
						", MGRS=" + MGRS +
						", WIFI=" + wifi +
						", TIME= " + timeString;
	}

}

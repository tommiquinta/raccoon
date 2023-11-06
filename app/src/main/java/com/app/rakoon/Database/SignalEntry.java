package com.app.rakoon.Database;

public class SignalEntry {
	int signal_id;
	String MGRS;
	int signal;

	String timeString;

	public SignalEntry() {
	}

	public SignalEntry(String MGRS, int signal, String timeString) {
		this.MGRS = MGRS;
		this.signal = signal;
		this.timeString = timeString;
	}

	public SignalEntry(int signal_id, String MGRS, int signal, String timeString) {
		this.signal_id = signal_id;
		this.MGRS = MGRS;
		this.signal = signal;
		this.timeString = timeString;
	}

	public SignalEntry(String MGRS, int signal) {
		this.MGRS = MGRS;
		this.signal = signal;
	}

	public int getSignal_id() {
		return this.signal_id;
	}


	public int getSignal() {
		return signal;
	}

	public String getTime() {
		return timeString;
	}

	public String getMGRS() {
		return MGRS;
	}


	@Override
	public String toString() {
		return
				"ID=" + signal_id +
						", MGRS=" + MGRS +
						", SIGNAL=" + signal +
						", TIME= " + timeString;
	}
}

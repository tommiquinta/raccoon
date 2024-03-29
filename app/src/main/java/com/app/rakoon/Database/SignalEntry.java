package com.app.rakoon.Database;

public class SignalEntry implements Entry {
	int signal_id;
	String MGRS;
	int signal;
	double signalAvg;
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

	public SignalEntry(String MGRS, double signalAvg) {
		this.MGRS = MGRS;
		this.signalAvg = signalAvg;
	}

	public int getSignal_id() {
		return this.signal_id;
	}


	public int getSignal() {
		return signal;
	}

	@Override
	public String getTime() {
		return timeString;
	}

	@Override
	public int getId() {
		return signal_id;
	}

	public String getMGRS() {
		return MGRS;
	}
	@Override
	public String toString() {
		return
				"Signal = " + signal + ", \nDate: " + timeString;
	}

	public double getSignalAVG() {
		return signalAvg;
	}
}

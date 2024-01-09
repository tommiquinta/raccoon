package com.app.rakoon.Helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.widget.Toast;

import java.text.ParseException;

public class WiFiHelper {
	private final Context context;
	private double signalStrength;
	public WiFiHelper(Context context) {
		this.context = context;
	}

	public double getWiFi() throws ParseException {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Network network = connectivityManager.getActiveNetwork();
		NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

		if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
				signalStrength = capabilities.getSignalStrength();
			}
		}
		return signalStrength;
	};
}

package com.app.rakoon.Helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import java.text.ParseException;

public class WiFiHelper {
	private final Context context;
	public WiFiHelper(Context context) {
		this.context = context;
	}

	public double getWiFi() throws ParseException {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Network network = connectivityManager.getActiveNetwork();

		NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

		if (capabilities != null) {
			if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
					return capabilities.getSignalStrength();
				}
			}
			return 101;
		}
		return 101;
	}
}

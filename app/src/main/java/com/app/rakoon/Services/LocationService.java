package com.app.rakoon.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class LocationService extends Service {

	private LocationManager locationManager;
	private LocationListener locationListener;

	@Override
	public void onCreate() {
		super.onCreate();
		setupLocationManager();
	}

	private void setupLocationManager() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				// Invia la posizione corrente a SoundService
				sendLocationBroadcast(location);
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
			}
		};

		// Richiedi aggiornamenti sulla posizione
		if (locationManager != null) {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				return;
			}
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		}
	}

	private void sendLocationBroadcast(Location location) {
		Intent intent = new Intent("LOCATION_UPDATE");
		intent.putExtra("location", location);
		sendBroadcast(intent);
		stopSelf();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Rimuovi il listener alla distruzione del servizio
		if (locationManager != null) {
			locationManager.removeUpdates(locationListener);
		}
	}
}

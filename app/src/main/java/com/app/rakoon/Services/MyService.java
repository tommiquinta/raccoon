package com.app.rakoon.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.app.rakoon.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 A service is a Component, like an Activity, with the difference that it does not have a user interface, but runs in the background

 normal service -> user is not aware that the service is running; may be stopped due to memory necesities
 foreground service -> user is aware that the app is doing something in the background with the use of a notification; user can also interact with the notifiction

 Also, foreground service won't be killed even if the system is low on memory.

 */
public class MyService extends Service {

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {  // creates a Bound Service -> you can ahve one active instance and multiple components connected to it.
		return null;
	}

	private static final String CHANNEL_ID = "ForegroundServiceChannel";
	private FusedLocationProviderClient fusedLocationProviderClient;

	@Override
	public void onCreate() {
		super.onCreate();
		fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		createNotificationChannel();
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this,
				0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

		// Definisci un LocationCallback per ottenere aggiornamenti continui della posizione
		LocationCallback locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				if (locationResult != null) {
					Location location = locationResult.getLastLocation();
					updateNotification(location.getLatitude(), location.getLongitude());
				}
			}
		};

		// Richiedi aggiornamenti continui della posizione
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return Service.START_STICKY_COMPATIBILITY;
		}
		fusedLocationProviderClient.requestLocationUpdates(createLocationRequest(), locationCallback, null);

		return START_NOT_STICKY;
	}

	private void updateNotification(double latitude, double longitude) {
		Intent notificationIntent = new Intent(this, MainActivity.class);

		PendingIntent pendingIntent = PendingIntent.getActivity(this,
				0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setContentTitle("Posizione Attuale")
				.setContentText("Lat: " + latitude + ", Lng: " + longitude)
				.setContentIntent(pendingIntent)
				.build();

		startForeground(1, notification);
	}

	private LocationRequest createLocationRequest() {
		return new LocationRequest.Builder()
				.setInterval(30000)  // Modificato a 30 secondi, o regola secondo le tue esigenze
				.setFastestInterval(15000)  // Intervallo più veloce possibile tra gli aggiornamenti in millisecondi
				.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)  // Priorità bilanciata tra precisione e consumo energetico
				.build();
	}



	@Override
	public void onDestroy() {
		// Here is a good place to stop the LocationEngine.
		super.onDestroy();
	}

	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel serviceChannel = new NotificationChannel(
					CHANNEL_ID,
					"Foreground Service Channel",
					NotificationManager.IMPORTANCE_DEFAULT
			);

			NotificationManager manager = getSystemService(NotificationManager.class);
			manager.createNotificationChannel(serviceChannel);
		}
	}
}

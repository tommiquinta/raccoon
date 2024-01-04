package com.app.rakoon.Services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.app.rakoon.Database.SignalEntry;
import com.app.rakoon.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.SimpleDateFormat;
import java.util.Date;

import mil.nga.mgrs.MGRS;

/**
 * A service is a Component, like an Activity, with the difference that it does not have a user interface, but runs in the background
 * <p>
 * normal service -> user is not aware that the service is running; may be stopped due to memory necesities
 * foreground service -> user is aware that the app is doing something in the background with the use of a notification; user can also interact with the notifiction
 * <p>
 * Also, foreground service won't be killed even if the system is low on memory.
 */
public class MyService extends Service {

	private final LocationCallback locationCallback = new LocationCallback() {
		@Override
		public void onLocationResult(@NonNull LocationResult locationResult) {
			super.onLocationResult(locationResult);
			if (locationResult.getLastLocation() != null) {
				double latitude = locationResult.getLastLocation().getLatitude();
				double longitude = locationResult.getLastLocation().getLongitude();
				Location location = locationResult.getLastLocation();
				saveindb(location);
				Log.d("LOCATION_UPDATE", latitude + ", " + longitude);
			}
		}
	};

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public void saveindb(Location location){
		DB_service dbService = new DB_service(this);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
		String time = sdf.format(new Date());

		MGRS mgrs = MGRS.from(location.getLongitude(), location.getLatitude());

		SignalEntry se = new SignalEntry(mgrs.toString(), 50, time);

		dbService.save(se);
		Log.d("SALVATO: " , "TRUE");



	}
	private void startLocationService() {
		String channelId = "location_notification_channel";
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Intent resultIntent = new Intent();
		PendingIntent pendingIntent = PendingIntent.getActivity(
				getApplicationContext(),
				0,
				resultIntent,
				PendingIntent.FLAG_IMMUTABLE
		);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				getApplicationContext(),
				channelId
		);

		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setContentTitle("Location Service");
		builder.setDefaults(NotificationCompat.DEFAULT_ALL);
		builder.setContentText("Running");
		builder.setContentIntent(pendingIntent);
		builder.setAutoCancel(false);
		builder.setOngoing(true);
		builder.setPriority(NotificationCompat.PRIORITY_MAX);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
				NotificationChannel notificationChannel = new NotificationChannel(
						channelId,
						"Location Service",
						NotificationManager.IMPORTANCE_HIGH
				);
				notificationChannel.setDescription("Channel for location service.");
				notificationManager.createNotificationChannel(notificationChannel);
			}
		}

		repeat();

		startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
	}

	public void getLocation(){
		LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
				.setMaxUpdates(1)
				.build();

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		LocationServices.getFusedLocationProviderClient(this)
				.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
	}

	private void stopLocationService(){
		LocationServices.getFusedLocationProviderClient(this)
				.removeLocationUpdates(locationCallback);
		stopForeground(true);
		stopSelf();
	}

	private final Handler handler = new Handler();
	private Runnable runnable;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();
			if (action != null) {
				if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
					startLocationService();
				} else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
					stopLocationService();
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}



	private void repeat() {
		// Avvia il servizio di localizzazione ogni 5 secondi
		handler.postDelayed(runnable = new Runnable() {
			public void run() {
				Log.d("START","partito");
				getLocation();

				handler.postDelayed(this, 5000);
			}
		}, 5000); // Avvia dopo 5 secondi dalla chiamata iniziale
	}

	@Override
	public void onDestroy() {
		// Rimuove il callback per evitare memory leaks
		handler.removeCallbacks(runnable);
		super.onDestroy();
	}
}

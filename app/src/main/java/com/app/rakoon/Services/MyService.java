package com.app.rakoon.Services;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.SignalEntry;
import com.app.rakoon.Database.SoundEntry;
import com.app.rakoon.Database.WifiEntry;
import com.app.rakoon.Fragments.Settings;
import com.app.rakoon.Helpers.SignalHelper;
import com.app.rakoon.Helpers.SoundHelper;
import com.app.rakoon.Helpers.WiFiHelper;
import com.app.rakoon.R;
import com.app.rakoon.SoundActivity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.ParseException;
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
	SignalHelper signalHelper;
	WiFiHelper wiFiHelper;
	SoundHelper soundHelper;

	private final LocationCallback locationCallback = new LocationCallback() {
		@Override
		public void onLocationResult(@NonNull LocationResult locationResult) {
			super.onLocationResult(locationResult);
			if (locationResult.getLastLocation() != null) {
				double latitude = locationResult.getLastLocation().getLatitude();
				double longitude = locationResult.getLastLocation().getLongitude();
				Location location = locationResult.getLastLocation();

				int signal;
				double wifi;
				double sound;

				signal = signalHelper.getSignal();
				try {
					wifi = wiFiHelper.getWiFi();
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				sound = soundHelper.getSound();

				save(location, signal, wifi, sound);

				Log.d("LOCATION_UPDATE", latitude + ", " + longitude + "\nSIGNAL: " + signal);
			}
		}
	};

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void save(Location location, int signal, double wifi, double sound) {
		DatabaseHelper dbService = new DatabaseHelper(this);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
		String time = sdf.format(new Date());

		MGRS mgrs = MGRS.from(location.getLongitude(), location.getLatitude());

		SignalEntry signalEntry = new SignalEntry(mgrs.toString(), signal, time);

		WifiEntry wifiEntry = new WifiEntry(mgrs.toString(), wifi, time);
		SoundEntry soundEntry = new SoundEntry(mgrs.toString(), sound, time);


		dbService.addSignalEntry(signalEntry);
		if (wifiEntry.getWifi() != 101) {
			dbService.addWifiEntry(wifiEntry);
		}

		if (soundEntry.getDecibel() != Double.POSITIVE_INFINITY) {
			dbService.addSoundEntry(soundEntry);
		}
		Log.d("SALVATO: ", "TRUE");
	}

	private void startLocationService() {
		String channelId = "location_notification_channel";
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Intent resultIntent = new Intent();
		PendingIntent pendingIntent = getActivity(
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
		builder.setContentTitle("Raccoon Service");
		builder.setDefaults(NotificationCompat.DEFAULT_ALL);
		builder.setContentText("Running");
		builder.setCategory(NotificationCompat.CATEGORY_LOCATION_SHARING);
		builder.setContentIntent(pendingIntent);
		builder.setOngoing(true);
		builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		builder.setPriority(NotificationCompat.PRIORITY_MAX);

		builder.setDeleteIntent(null);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
				NotificationChannel notificationChannel = new NotificationChannel(
						channelId,
						"Location Service",
						NotificationManager.IMPORTANCE_HIGH
				);
				notificationChannel.setDescription("Channel for location service.");
				notificationChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
				notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
				notificationManager.createNotificationChannel(notificationChannel);
			}
		}

		repeat();

		startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
	}

	public void getLocation() {

		LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
				.setMaxUpdates(1)   // very important
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

	private void stopLocationService() {
		LocationServices.getFusedLocationProviderClient(this)
				.removeLocationUpdates(locationCallback);
		stopForeground(true);
		stopSelf();
	}

	private final Handler handler = new Handler();
	private Runnable runnable;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();

		signalHelper = SignalHelper.getInstance(getApplicationContext());
		wiFiHelper = new WiFiHelper(getApplicationContext());
		soundHelper = new SoundHelper(getApplicationContext());

		if (action != null) {
			if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
				startLocationService();
			} else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
				stopLocationService();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void repeat() {
		handler.postDelayed(runnable = new Runnable() {
			public void run() {
				Log.d("START", "partito");
				getLocation();

				int delayMillis = Settings.getNumericValue(getApplicationContext()) * 60000;
				Log.d("MINUTI", String.valueOf(delayMillis));

				handler.postDelayed(this, delayMillis);
			}
		}, 0); // start immediately after first call
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(runnable);
		super.onDestroy();
	}
}

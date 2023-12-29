package com.app.rakoon.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.app.rakoon.MainActivity;

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

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String input = intent.getStringExtra("inputExtra");
		createNotificationChannel();
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this,
				0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setContentTitle("Foreground Service")
				.setContentText(input)
				.setContentIntent(pendingIntent)
				.build();

		startForeground(1, notification);

		// Here is a good place to handle the location consent.
		// You can already start the LocationEngine here.

		return START_NOT_STICKY;
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

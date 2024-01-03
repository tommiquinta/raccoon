package com.app.rakoon.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.app.rakoon.MainActivity;
import com.app.rakoon.R;
import com.app.rakoon.SignalActivity;

import java.text.ParseException;

public class MyService extends Service {
	private static final String CHANNEL_ID = "ForegroundServiceChannel";
	private Handler handler;
	private Runnable runnable;
	private int notificationId = 1;

	private SignalActivity signalActivity;
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run() {
				signalActivity = new SignalActivity();
				try {
					signalActivity.getSignal();
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				sendNotification();
				handler.postDelayed(this, 5000); // 5000 milliseconds = 5 seconds
			}
		};
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

		// Start the periodic notification
		handler.postDelayed(runnable, 5000);

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		// Stop the periodic notification
		handler.removeCallbacks(runnable);
		super.onDestroy();
	}

	private void sendNotification() {
		// Code to send the notification
		// You can customize the notification content here
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setContentTitle("Periodic Notification")
				.setContentText("This is a notification sent every 5 seconds")
				.setSmallIcon(com.google.android.gms.base.R.drawable.common_google_signin_btn_text_light_normal);

		NotificationManager manager = getSystemService(NotificationManager.class);
		manager.notify(notificationId++, builder.build());
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

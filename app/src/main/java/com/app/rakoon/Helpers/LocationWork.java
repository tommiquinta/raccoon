package com.app.rakoon.Helpers;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.rakoon.R;

public class LocationWork extends Worker {

	private NotificationManager notificationManager;
	private Context context;
	String progress = "Starting work...";
	int NOTIFICATION_ID = 1;
	private LocationManager locationManager;
	private IntentFilter intentFilter;

	public LocationWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
		this.context = context;
		notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

		locationManager = LocationManager.getInstance(context);

		intentFilter = new IntentFilter();
		intentFilter.addAction("local_broadcast");
		LocalBroadcastManager.getInstance(context).registerReceiver(localBroadcastReceiver, intentFilter);
	}

	@NonNull
	@Override
	public Result doWork() {
		setForegroundAsync(showNotifications(progress));
		while (true) {
			if (1 > 2) {
				break;
			}
			locationManager.startLocationUpdates();

			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return Result.success();
	}

	@NonNull
	private ForegroundInfo showNotifications(String progress){
		return new ForegroundInfo(NOTIFICATION_ID, createNotification(progress));
	}

	private Notification createNotification(String progress){
		String CHANNEL_ID = "100";
		String title = "Foreground Work";
		String cancel = "Cancel";

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE))
					.createNotificationChannel(new NotificationChannel(CHANNEL_ID, title, NotificationManager.IMPORTANCE_HIGH));
		}

		Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
				.setContentTitle(title)
				.setTicker(title)
				.setContentText(progress)
				.setSmallIcon(mil.nga.mgrs.R.drawable.common_google_signin_btn_icon_dark_focused)
				.setOngoing(true)
				.build();

		return notification;
	}

	private void updateNotification(String progress){

		Notification notification = createNotification(progress);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}


	BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("TAG", "Broadcasted");
			String progress = intent.getStringExtra("Location");
			updateNotification(progress);
		}
	};

	@Override public void onStopped(){
		LocalBroadcastManager.getInstance(context).unregisterReceiver(localBroadcastReceiver);
		super.onStopped();
	}

}

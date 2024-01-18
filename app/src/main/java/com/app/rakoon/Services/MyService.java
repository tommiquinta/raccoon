package com.app.rakoon.Services;

import static android.app.PendingIntent.getActivity;

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
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.Entry;
import com.app.rakoon.Database.SignalEntry;
import com.app.rakoon.Database.SoundEntry;
import com.app.rakoon.Database.WifiEntry;
import com.app.rakoon.Fragments.Settings;
import com.app.rakoon.Helpers.SignalHelper;
import com.app.rakoon.Helpers.SoundHelper;
import com.app.rakoon.Helpers.WiFiHelper;
import com.app.rakoon.R;
import com.app.rakoon.SettingsActivity;
import com.app.rakoon.SignalActivity;
import com.app.rakoon.SoundActivity;
import com.app.rakoon.WiFiActivity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
	List<SoundEntry> soundList;
	List<SignalEntry> signalList;
	List<WifiEntry> wifiList;
	private final LocationCallback locationCallback = new LocationCallback() {
		@Override
		public void onLocationResult(@NonNull LocationResult locationResult) {
			super.onLocationResult(locationResult);

			if (locationResult.getLastLocation() != null) {

				Location location = locationResult.getLastLocation();

				int signal = 150;
				double wifi = 150;
				double sound = -10;

				if (Settings.get_boolean_bg(getApplicationContext(), Constants.SIGNAL)) {
					signal = signalHelper.getSignal();
				}

				if (Settings.get_boolean_bg(getApplicationContext(), Constants.WIFI)) {
					try {
						wifi = wiFiHelper.getWiFi();
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}

				if (Settings.get_boolean_bg(getApplicationContext(), Constants.SOUND)) {
					sound = soundHelper.getSound();
				}

				try {
					save(location, signal, wifi, sound);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}

				Log.d("LOCATION_UPDATE", locationResult.getLastLocation().getLatitude() + ", " + locationResult.getLastLocation().getLongitude() + "\nSIGNAL: " + signal);
			}
		}
	};

	private void sendNotification(boolean new_area, String type, int id) {

		String title = "New " + type + " measurement!";
		String context = "Who's back?";
		String bigText = "You just made a " + type + " measurement in area that you didn't visit for the past 24 hours.";
		if (new_area) {
			title = "Good job!";
			context = "New " + type + " zone discovered.";
			bigText = "You just made a " + type + " measurement in a 100 meter zone where you have never been before.";
		}

		// open map when a notification is clicked
		Intent intent = null;
		switch (type) {
			case "Signal": {
				intent = new Intent(this, SignalActivity.class);
				break;
			}
			case "WiFI": {
				intent = new Intent(this, WiFiActivity.class);
				break;
			}
			case "Sound": {
				intent = new Intent(this, SoundActivity.class);
				break;
			}
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

		NotificationCompat.Builder newNotificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "new_location_notification_channel")
				.setSmallIcon(R.drawable.ic_launcher_background)
				.setContentTitle(title)
				.setContentText(context)
				.setStyle(new NotificationCompat.BigTextStyle()
						.bigText(bigText))
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setContentIntent(pIntent);


		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
		notificationManager.notify(id, newNotificationBuilder.build());
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void save(Location location, int signal, double wifi, double sound) throws ParseException {
		if (!noneIsChecked()) {
			DatabaseHelper dbService = new DatabaseHelper(this);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
			String time = sdf.format(new Date());

			MGRS mgrs = MGRS.from(location.getLongitude(), location.getLatitude());

			if (Settings.get_boolean_bg(getApplicationContext(), Constants.SIGNAL)) {
				SignalEntry signalEntry = new SignalEntry(mgrs.toString(), signal, time);
				dbService.addSignalEntry(signalEntry);
				check_zone(signalEntry);
			}

			if (Settings.get_boolean_bg(getApplicationContext(), Constants.SOUND)) {
				SoundEntry soundEntry = new SoundEntry(mgrs.toString(), sound, time);
				if (soundEntry.getDecibel() != Double.POSITIVE_INFINITY) {
					dbService.addSoundEntry(soundEntry);
					check_zone(soundEntry);
				}
			}

			if (Settings.get_boolean_bg(getApplicationContext(), Constants.WIFI)) {
				WifiEntry wifiEntry = new WifiEntry(mgrs.toString(), wifi, time);
				if (wifiEntry.getWifi() != 101) {
					dbService.addWifiEntry(wifiEntry);
					check_zone(wifiEntry);
				}
			}

		} else {
			Log.d("chiuso", "tutto chiuso");
		}
	}

	private boolean noneIsChecked() {
		return !Settings.get_boolean_bg(getApplicationContext(), Constants.SOUND) && !Settings.get_boolean_bg(getApplicationContext(), Constants.WIFI) && !Settings.get_boolean_bg(getApplicationContext(), Constants.SIGNAL);
	}

	private void check_zone(Entry entry) throws ParseException {
		String mgrs = entry.getMGRS();
		Date sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm").parse(entry.getTime());
		String new_mgrs = mgrs.substring(0, 8) + mgrs.substring(10, 13);    // 100 meter zone
		String type = "x";
		List<? extends Entry> entryList = null;
		int id = 0;
		if (entry instanceof SignalEntry) {
			type = "Signal";
			entryList = signalList;
			id = 1;
		} else if (entry instanceof SoundEntry) {
			type = "Sound";
			entryList = soundList;
			id = 2;
		} else if (entry instanceof WifiEntry) {
			type = "WiFi";
			entryList = wifiList;
			id = 3;
		}

		if (entryList.isEmpty()) {
			sendNotification(true, type, id);
			return;
		}

		for (Entry s : entryList) {
			String old_mgrs = s.getMGRS();
			String hunder_mgrs = old_mgrs.substring(0, 8) + old_mgrs.substring(10, 13);
			Date old_date = new SimpleDateFormat("yyyy-MM-dd, HH:mm").parse(s.getTime());

			if (hunder_mgrs.equals(new_mgrs)) {
				Log.d("AREA:", "questa are è già stat visitata");
				if (sdf.getTime() - old_date.getTime() > 24 * 60 * 60 * 1000) {
					sendNotification(false, type, id);
					break;
				}
			} else {
				sendNotification(true, type, id);
				Log.d("ARE", "questa are a è nuova");
			}
			break;
		}

		if (entry instanceof SignalEntry) {
			signalList.add((SignalEntry) entry);
		} else if (entry instanceof SoundEntry) {
			soundList.add((SoundEntry) entry);
		} else if (entry instanceof WifiEntry) {
			wifiList.add((WifiEntry) entry);
		}
	}

	private void startLocationService() {
		//Log.d("LISTA: ", soundList.get(0).getTime());

		String channelId = "location_notification_channel";
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		createNotificationChannel();

		Intent resultIntent = new Intent();
		PendingIntent pendingIntent = getActivity(
				getApplicationContext(),
				0,
				resultIntent,
				PendingIntent.FLAG_IMMUTABLE
		);

		Intent intent = new Intent(this, SettingsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

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
		builder.setContentIntent(pIntent);

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

	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "new_location_notification_channel";
			String description = "notifications for new area";
			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel channel = new NotificationChannel("new_location_notification_channel", name, importance);
			channel.setDescription(description);
			channel.setShowBadge(true);

			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}

	}

	public void getLocation() {
		LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
				.setMaxUpdates(1)   // very important
				.build();

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

		DatabaseHelper dbService = new DatabaseHelper(this);

		signalList = dbService.getSignals();
		wifiList = dbService.getWiFi();
		soundList = dbService.getSounds();

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
		}, 5000);
	}

	PowerManager.WakeLock wakeLock;

	@Override
	public void onCreate() {
		super.onCreate();
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"MyApp::MyWakelockTag");
		wakeLock.acquire();
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(runnable);
		super.onDestroy();
		wakeLock.release();
	}
}

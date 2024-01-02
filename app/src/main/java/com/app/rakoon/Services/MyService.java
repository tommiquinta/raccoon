package com.app.rakoon.Services;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.app.rakoon.Helpers.LocationManager;

public class MyService extends Service {

	private static final long INTERVAL = 5000; // Intervallo di aggiornamento in millisecondi (5 secondi)
	private static final int NOTIFICATION_ID =1 ;
	private Handler handler;

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler(Looper.myLooper());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Avvia la richiesta della posizione ogni tot secondi utilizzando il Handler
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				requestLocation();
				handler.postDelayed(this, INTERVAL);
			}
		}, INTERVAL);

		// Avvia il servizio in foreground
		startForeground(NOTIFICATION_ID, createNotification("Starting work..."));

		return START_STICKY;
	}

	private void requestLocation() {
		LocationManager locationManager = LocationManager.getInstance(this);

		// Verifica se i permessi di localizzazione sono stati concessi
			// Crea la richiesta di posizione (puoi usare il tuo metodo createLocationRequest)
		locationManager.createLocationRequest();

			// Avvia gli aggiornamenti della posizione
		locationManager.startLocationUpdates();

		Log.d("LocationUpdateService", "Richiesta posizione attuale");
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		// Rimuovi il callback del handler quando il servizio viene distrutto
		handler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}
}


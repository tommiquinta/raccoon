package com.app.rakoon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.SoundEntry;
import com.app.rakoon.Helpers.VerticesHelper;
import com.google.android.gms.location.LocationRequest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.grid.features.Point;
import mil.nga.mgrs.MGRS;
import mil.nga.mgrs.grid.GridType;

import mil.nga.mgrs.grid.style.Grids;
import mil.nga.mgrs.tile.MGRSTileProvider;

import android.graphics.Color;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

	private final int PERMISSION_ID = 42;
	private FusedLocationProviderClient mFusedLocationClient;
	public GoogleMap mMap;

	// Current Location is set to Bologna
	public LatLng currentLocation;

	/**
	 * MGRS tile provider
	 */
	private MGRSTileProvider tileProvider = null;

	public MapActivity() {
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		// get the API key
	/*	try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			String apiKey = ai.metaData.getString("com.google.android.geo.API_KEY");

			// Initializing the Places API with the help of our API_KEY
			if (!Places.isInitialized()) {
				Places.initialize(getApplicationContext(), apiKey);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

	 */

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		assert mapFragment != null;
		mapFragment.getMapAsync(this);

		Grids grids = Grids.create();
		grids.setWidth(GridType.TEN_METER, 1.0);
		grids.enableLabeler(GridType.TEN_METER);
		tileProvider = MGRSTileProvider.create(this, grids);
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
	}

	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		mMap = googleMap;
		mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		getLastLocation();
	}


	public static Map<String, Double> calculateDecibelAverages(List<SoundEntry> soundEntries) {
		Map<String, List<Double>> decibelMap = new HashMap<>();

		for (SoundEntry entry : soundEntries) {
			String MGRS = entry.getMGRS();
			// mgrs for 10 meter squares
			String sw = MGRS.substring(0, 9) + "" + MGRS.substring(10, 14);

			double decibel = entry.getDecibel();

			if (!decibelMap.containsKey(sw)) {
				decibelMap.put(sw, new ArrayList<>());
			}
			decibelMap.get(sw).add(decibel);
		}

		// Calculating the average decibel for each MGRS area
		Map<String, Double> averageDecibels = new HashMap<>();
		for (Map.Entry<String, List<Double>> entry : decibelMap.entrySet()) {
			List<Double> decibelList = entry.getValue();
			double sum = 0;
			for (Double d : decibelList) {
				sum += d;
			}
			double average = sum / decibelList.size();
			averageDecibels.put(entry.getKey(), average);
		}

		return averageDecibels;
	}


	// Get current location
	@SuppressLint("MissingPermission")
	private void getLastLocation() {
		if (checkPermissions()) {
			if (isLocationEnabled()) {
				mFusedLocationClient.getLastLocation().addOnCompleteListener(this, task -> {
					Location location = task.getResult();
					if (location == null) {
						requestNewLocationData();
					} else {
						// blue moving marker
						mMap.setMyLocationEnabled(true);

						currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
						// add grid layer
						mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));


						// red fixed marker
						// mMap.addMarker(new MarkerOptions().position(currentLocation));

						// PREFERENCES FRO 10 METERS SQUARES GRID --> is there a better way to do this?
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18F));
						//mMap.setMinZoomPreference(18F); // Set a preference for minimum zoom (Zoom out).
						mMap.setMaxZoomPreference(20.5F); // Set a preference for maximum zoom (Zoom In).
					}
				});
			} else {
				Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		} else {
			requestPermissions();
		}
	}


	// Get current location, if shifted
	// from previous location
	@SuppressLint("MissingPermission")
	private void requestNewLocationData() {
		LocationRequest locationRequest = LocationRequest.create().setWaitForAccurateLocation(true);
		locationRequest.setInterval(0);
		locationRequest.setFastestInterval(0);
		locationRequest.setNumUpdates(1);

		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
	}

	// If current location could not be located, use last location
	private final LocationCallback mLocationCallback = new LocationCallback() {
		@Override
		public void onLocationResult(@NonNull LocationResult locationResult) {
			Location lastLocation = locationResult.getLastLocation();

			assert lastLocation != null;
			currentLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
		}
	};

	// Function to check if GPS is on
	private boolean isLocationEnabled() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	// Check if location permissions are granted to the application
	private boolean checkPermissions() {
		return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}

	// Request permissions if not granted before
	private void requestPermissions() {
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
	}

	// when permission has been obtained, do the app work
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_ID && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			getLastLocation();
		}
	}
}

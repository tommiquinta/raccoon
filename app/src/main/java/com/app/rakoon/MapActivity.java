package com.app.rakoon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.rakoon.Database.DatabaseHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.text.ParseException;

import mil.nga.mgrs.grid.GridType;
import mil.nga.mgrs.grid.style.Grids;
import mil.nga.mgrs.tile.MGRSTileProvider;

public abstract class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
	private final int PERMISSION_ID = 42;
	private FusedLocationProviderClient mFusedLocationClient;
	GoogleMap mMap;
	LatLng currentLocation;

	private int accuracy = 10;

	private DatabaseHelper databaseHelper;

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

		// initialize database DAO
		databaseHelper = new DatabaseHelper(MapActivity.this);


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


		// create map fragment
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		assert mapFragment != null;
		mapFragment.getMapAsync(this);

		// create map grid
		Grids grids = Grids.create();
		grids.setWidth(GridType.TEN_METER, 1.0);
		//grids.enableLabeler(GridType.TEN_METER);

		// create the MGRS tile provider
		tileProvider = MGRSTileProvider.create(this, grids);

		// Initializing fused location client
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		ImageButton set100meters = findViewById(R.id.set100m);

		set100meters.setOnClickListener(v -> {
			try {
				resizeGrid(100);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public void resizeGrid(int accuracy) throws ParseException {
		mMap.clear();
		Grids grids = Grids.create();
		grids.setWidth(GridType.HUNDRED_METER, 1.0);
		// create the MGRS tile provider
		tileProvider = MGRSTileProvider.create(this, grids);
		mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.5F));
		mMap.setMinZoomPreference(15.5F); // Set a preference for minimum zoom (Zoom out).
		mMap.setMaxZoomPreference(17.5F); // Set a preference for maximum zoom (Zoom In).
		setAccuracy(accuracy);
		fetchData();
	}


	// method to wait for map to be loaded
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		mMap = googleMap;
		mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		//mMap.setOnCameraIdleListener(this);
		//mMap.setOnMapClickListener(this);
		getLastLocation();
	}

	/**
	 * overrided method
	 */
	public void fetchData() throws ParseException {
	}

	public int getAccuracy(){
		return this.accuracy;
	}

	public void setAccuracy(int a){
		this.accuracy = a;
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
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20F));
						mMap.setMinZoomPreference(18F); // Set a preference for minimum zoom (Zoom out).
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

package com.app.rakoon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

	private final int PERMISSION_ID = 42;
	private FusedLocationProviderClient mFusedLocationClient;
	private GoogleMap mMap;

	// Current Location is set to Bologna
	private LatLng currentLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		// get the API key
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			String apiKey = ai.metaData.getString("com.google.android.geo.API_KEY");

			// Initializing the Places API with the help of our API_KEY
			if (!Places.isInitialized()) {
				Places.initialize(getApplicationContext(), apiKey);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		// create map fragment
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		// Initializing fused location client
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		// button to
		Button backToLocation = findViewById(R.id.currentLoc);
		backToLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getLastLocation();
			}
		});
	}

	// method to wait for map to be loaded
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		mMap = googleMap;
		getLastLocation();
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
						currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
						mMap.clear();
						mMap.addMarker(new MarkerOptions().position(currentLocation));
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16F));
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
		LocationRequest mLocationRequest = new LocationRequest();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(0);
		mLocationRequest.setFastestInterval(0);
		mLocationRequest.setNumUpdates(1);

		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
	}

	// If current location could not be located, use last location
	private final LocationCallback mLocationCallback = new LocationCallback() {
		@Override
		public void onLocationResult(@NonNull LocationResult locationResult) {
			Location mLastLocation = locationResult.getLastLocation();
			currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
		}
	};

	// Function to check if GPS is on
	private boolean isLocationEnabled() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	// Check if location permissions are granted to the application
	private boolean checkPermissions() {
		return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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

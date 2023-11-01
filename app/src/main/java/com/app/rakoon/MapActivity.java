package com.app.rakoon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;

import com.google.android.gms.location.LocationRequest;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.libraries.places.api.Places;

import java.text.DateFormat;
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
import mil.nga.mgrs.grid.style.Grid;

import mil.nga.mgrs.grid.style.Grids;
import mil.nga.mgrs.tile.MGRSTileProvider;

import android.graphics.Color;

/* LIST TO COLOR MAP:
	1. convert latitude and longitude coordinates to the corresponding MGRS square => done, missing handling differnet squares size
	2. get the for angles of the MGRS squares in the LatLong format => done
	3. create a heatmap specifying the 4 angles => done
*/
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

	private final int PERMISSION_ID = 42;
	private FusedLocationProviderClient mFusedLocationClient;
	private GoogleMap mMap;

	// Current Location is set to Bologna
	private LatLng currentLocation;

	// audio
	private boolean isRecording = false;
	private AudioRecord audioRecord;
	private int bufferSize;
	private short[] audioData;

	private DatabaseHelper databaseHelper;

	/**
	 * MGRS tile provider
	 */
	private MGRSTileProvider tileProvider = null;

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
		grids.enableLabeler(GridType.TEN_METER);

		// create the MGRS tile provider
		tileProvider = MGRSTileProvider.create(this, grids);

		// Initializing fused location client
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		// button to record sound decibel
		ImageButton getDecibel = findViewById(R.id.getDecibel);

		bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
		audioData = new short[bufferSize];

		getDecibel.setOnClickListener(v -> {
			if (!isRecording) {
				try {
					startRecording();
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			} else {
				stopRecording();
			}
		});
	}

	// method to wait for map to be loaded
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		mMap = googleMap;
		mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		//mMap.setOnCameraIdleListener(this);
		//mMap.setOnMapClickListener(this);
		getLastLocation();
		fetchData();
	}

	private void fetchData() {
		List<SoundEntry> sounds = databaseHelper.getSounds();
		Map<String, Double> averageDecibels;
		averageDecibels = calculateDecibelAverages(sounds);

		mMap.clear();
		for (Map.Entry<String, Double> s : averageDecibels.entrySet()) {
			SoundEntry se = new SoundEntry(s.getKey(), s.getValue());
			colorMap(se);
		}

	}

	public static Map<String, Double> calculateDecibelAverages(List<SoundEntry> soundEntries) {
		Map<String, List<Double>> decibelMap = new HashMap<>();

		for (SoundEntry entry : soundEntries) {

			String MGRS = entry.MGRS;
			// mgrs for 10 meter squares
			String sw = MGRS.substring(0, 9) + "" + MGRS.substring(10, 14);

			double decibel = entry.decibel;

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

	private void colorMap(@NonNull SoundEntry s) {

		String sw = s.getMGRS();
		
		// this substring make the marker go on the bottom-left corner of the 10m x 10m square i'm currently in
		//Log.d("sw: ", sw.toString());

		// now mgrs is the location point in MGRS coord, i have to find the corresponding square
		VerticesHelper verticesHelper = new VerticesHelper();
		verticesHelper.setBottom_left(sw);

		// bottom right corner
		String se = verticesHelper.getBottom_right();

		// top left corner
		String nw = verticesHelper.getTop_left();

		// top right corner
		String ne = verticesHelper.getTop_right();

		try {
			Point sw_point = MGRS.parse(sw).toPoint();

			Point se_point = MGRS.parse(se).toPoint();

			Point nw_point = MGRS.parse(nw).toPoint();

			Point ne_point = MGRS.parse(ne).toPoint();

			List<LatLng> vertices = new ArrayList<>();

			// polygons vertices latlong
			vertices.add(new LatLng(nw_point.getLatitude(), nw_point.getLongitude()));
			vertices.add(new LatLng(sw_point.getLatitude(), sw_point.getLongitude()));
			vertices.add(new LatLng(se_point.getLatitude(), se_point.getLongitude()));
			vertices.add(new LatLng(ne_point.getLatitude(), ne_point.getLongitude()));

			PolygonOptions rectOptions = new PolygonOptions().addAll(vertices).strokeColor(Color.RED) // border color
					.fillColor(Color.argb(100, 255, 0, 0));

			mMap.addPolygon(rectOptions);

		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
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

	private void startRecording() throws ParseException {
		if (isMicrophonePermissionGranted()) {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
				return;
			}
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			audioRecord.startRecording();
			isRecording = true;
			Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show();

			// wait 1 sec before measuring
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			double amplitude = getAmplitude();
			double db = 20 * Math.log10((double) amplitude);
			displayDecibel(db);
			stopRecording();
			saveInDatabase(db);
		}
	}

	private void saveInDatabase(double db) {
		double decibel = Math.floor(db * 100) / 100;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
		String time = sdf.format(new Date());

		MGRS mgrs = MGRS.from(currentLocation.longitude, currentLocation.latitude);

		String mgrs_1 = mgrs.toString();

		SoundEntry soundEntry = new SoundEntry(mgrs_1, decibel, time);

		DatabaseHelper databaseHelper = new DatabaseHelper(MapActivity.this);

		colorMap(soundEntry);

		boolean success = databaseHelper.addEntry(soundEntry);
		Toast.makeText(this, "Saved: " + success, Toast.LENGTH_SHORT).show();
	}

	private void stopRecording() {
		if (isRecording) {
			isRecording = false;
			audioRecord.stop();
			audioRecord.release();
		}
	}


	private double getAmplitude() {
		double maxAmplitude = 0;

		audioRecord.read(audioData, 0, bufferSize);
		for (short s : audioData) {
			if (Math.abs(s) > maxAmplitude) {
				maxAmplitude = Math.abs(s);
			}
		}
		return maxAmplitude;
	}

	private void displayDecibel(double db) {
		String decibelText = String.format("Decibel Level: %.2f dB", db);
		Toast.makeText(this, decibelText, Toast.LENGTH_SHORT).show();
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

	private boolean isMicrophonePermissionGranted() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
			return false;
		}
		return true;
	}


}

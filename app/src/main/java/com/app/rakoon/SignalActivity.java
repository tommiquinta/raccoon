package com.app.rakoon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.Entry;
import com.app.rakoon.Database.SignalEntry;
import com.app.rakoon.Database.SoundEntry;
import com.app.rakoon.Helpers.VerticesHelper;
import com.google.android.gms.location.LocationRequest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

/* LIST TO COLOR MAP:
	1. convert latitude and longitude coordinates to the corresponding MGRS square => done, missing handling differnet squares size
	2. get the for angles of the MGRS squares in the LatLong format => done
	3. create a heatmap specifying the 4 angles => done
*/
public class SignalActivity extends AppCompatActivity implements OnMapReadyCallback {

	private final int PERMISSION_ID = 42;
	private FusedLocationProviderClient mFusedLocationClient;
	private GoogleMap mMap;

	// Current Location is set to Bologna
	private LatLng currentLocation;

	// audio
	private CellSignalStrengthLte cellSignalStrengthLte;
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
		databaseHelper = new DatabaseHelper(SignalActivity.this);


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

		getDecibel.setOnClickListener(v -> {
			try {
				getSignal();
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		});
	}


	private void getSignal() throws ParseException {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
		CellInfoLte cellInfoLte = null;

		for (CellInfo cellInfo : cellInfoList) {
			if (cellInfo instanceof CellInfoLte) {
				cellInfoLte = (CellInfoLte) cellInfo;
				break;
			}
		}

		//int signalStrength = 0;
		int signalLevel = 0;
		if (cellInfoLte != null) {
			CellSignalStrengthLte signalStrengthLte = cellInfoLte.getCellSignalStrength();
			//signalStrength = signalStrengthLte.getDbm();
			signalLevel = signalStrengthLte.getLevel();

			saveInDatabase(signalLevel);
		}

		Toast.makeText(this, "signalLevel: " + signalLevel, Toast.LENGTH_SHORT).show();

	};

	// method to wait for map to be loaded
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		mMap = googleMap;
		mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		//mMap.setOnCameraIdleListener(this);
		//mMap.setOnMapClickListener(this);
		getLastLocation();
		try {
			fetchData();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void fetchData() throws ParseException {
		List<SignalEntry> signals = databaseHelper.getSignals();
		Map<String, Double> averageSignals;
		averageSignals = calculateSignalAverages(signals);

		for (Map.Entry<String, Double> s : averageSignals.entrySet()) {
			SignalEntry se = new SignalEntry(s.getKey(), s.getValue());
			Log.d("stringa: ", se.toString());

			colorMap(se);
		}

	}

	public Map<String, Double> calculateSignalAverages(List<SignalEntry> signals) {
		Map<String, List<Integer>> signalMap = new HashMap<>();

		for (SignalEntry entry : signals) {
			String MGRS = entry.getMGRS();
			// mgrs for 10 meter squares
			String sw = MGRS.substring(0, 9) + "" + MGRS.substring(10, 14);

			int signal = entry.getSignal();

			if (!signalMap.containsKey(sw)) {
				signalMap.put(sw, new ArrayList<>());
			}
			signalMap.get(sw).add(signal);
		}

		// Calculating the average singal for each MGRS area
		Map<String, Double> averageSignals = new HashMap<>();
		for (Map.Entry<String, List<Integer>> entry : signalMap.entrySet()) {
			List<Integer> signalList = entry.getValue();
			double sum = 0;
			for (int d : signalList) {
				sum += d;
			}

			double average = sum / signalList.size();

			averageSignals.put(entry.getKey(), average);
		}

		return averageSignals;
	}

	private void colorMap(@NonNull SignalEntry s) throws ParseException {
		Toast.makeText(this, "data: " + s.getMGRS() + ": "+  s.getSignalAVG(), Toast.LENGTH_LONG).show();

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

			/**
			 * square with wide border
			 * PolygonOptions rectOptions = new PolygonOptions().addAll(vertices).strokeColor(Color.RED) // border color
			 * 					.fillColor(Color.argb(100, 0, 255, 155));
			 */

			PolygonOptions poly = new PolygonOptions().addAll(vertices).strokeWidth(0);

			// check the mean value to color the square
			if (s.getSignalAVG() >= 3.5) {
				poly.fillColor(Color.rgb(144, 238, 144));
			} else if (s.getSignalAVG() <3.5 && s.getSignalAVG() >= 1) {
				poly.fillColor(Color.rgb(255, 215, 0));
			} else {
				poly.fillColor(Color.rgb(255, 0, 0));
			}
			mMap.addPolygon(poly);

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

	private void saveInDatabase(int signal) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
		String time = sdf.format(new Date());

		MGRS mgrs = MGRS.from(currentLocation.longitude, currentLocation.latitude);

		String mgrs_1 = mgrs.toString();

		SignalEntry signalEntry = new SignalEntry(mgrs_1, signal, time);

		DatabaseHelper databaseHelper = new DatabaseHelper(SignalActivity.this);

		boolean success = databaseHelper.addSignalEntry(signalEntry);
		Toast.makeText(this, "Saved: " + success, Toast.LENGTH_SHORT).show();
		if (success) {
			fetchData();
		}
	}

	private void displaySignal(int signal) {
		String decibelText = String.format("Signal Level: %.2f dB", signal);
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

}

package com.nono.wherewerewe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.nono.gui.MenuListBundle;
import com.nono.gui.MenuListDialog;
import com.nono.gui.MenuListSelectRow;
import com.nono.util.StringUtilities;
import com.nono.wherewerewe.data.IntentWrapper;
import com.nono.wherewerewe.data.PreferenceData;
import com.nono.wherewerewe.data.table.TripTable;
import com.nono.wherewerewe.data.table.WaypointTable;
import com.nono.wherewerewe.db.DbAdapter;
import com.nono.wherewerewe.db.TripTableDb;
import com.nono.wherewerewe.db.WaypointTableDb;

public class MapDetail extends MapActivity {

	private final static int INITIAL_ZOOM_LEVEL = 16;
	private final static float METERS_PER_MILE = 1609.0f;
	private final static float FEET_PER_METER = 3.28083989501f;

	public static final int REQUEST_CODE_MORE_OPTIONS = 30;
	public static final int REQUEST_CODE_NAVIGATE_TO_LOCATION = 31;

	public static final int OPTION_COMPASS_HIDE = 1;
	public static final int OPTION_COMPASS_SHOW = 4;
	public static final int OPTION_NAVIGATOR_HIDE = 8;
	public static final int OPTION_NAVIGATOR_SHOW = 11;
	public static final int OPTION_SHOW_MAP = 15;
	public static final int OPTION_SHOW_SATELLITE = 16;
	public static final int OPTION_SHOW_TRAFFIC = 17;

	private final static long LONG_PRESS_DURATION = 750;

	private PreferenceData preferenceData = null;

	private DbAdapter dbAdapter = null;
	private WaypointTableDb waypointTableDb;
	private TripTableDb tripTableDb;
	private WaypointTable waypointTable = null;

	private MapController mapController = null;
	private FixedMyLocationOverlay myLocationOverlay = null;
	private MapView mapView = null;

	// Compass variables
    private SensorEventListener mListener;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private TroubleshootOverlay troubleshootOverlay = null;
    private CompassOverlay compassOverlay = null;
    private NavigateToOverlay navigateToOverlay = null;
    private float compassHeading = 0.0f;
    private float navigationHeading = 135.0f;
    private Location currentLocation = null;
    private Location destination = null;
    private String distanceUnitAboveThresholdText = null;
    private String distanceUnitBelowThresholdText = null;
    private Handler mLongpressHandler = new Handler();

    private boolean bMapShowing = true;
    private int numItemsUsingSensor = 0;
    private int numItemsUsingLocation = 0;
	private boolean prefsChanged = false;

	private int currentOrientationValue = -1;
	private int m_canvassLongLen = 0;
    private int deviceWidth = 0;
    private int deviceHeight = 0;
    private int canvassWidth = 0;
    private int canvassHeight = 0;

	private boolean troubleshootMode = false;
	
//    private LocationManager locationManager = null;
//    private LocationListener locationListener = null;

    
    /* Derive the declination using the passed-in or computed current location
     i.e.
     	GeomagneticField geoField = new GeomagneticField(
         Double.valueOf(location.getLatitude()).floatValue(),
         Double.valueOf(location.getLongitude()).floatValue(),
         Double.valueOf(location.getAltitude()).floatValue(),
         System.currentTimeMillis()
         );
		declination = geoField.getDeclination();
     */
    private float declination = 0.0f;
    private float bearingToRemoteLocation = 0.0f;
    private float distanceToRemoteLocation = 0.0f;
    private MarkerOverlay markerOverlay;
    private boolean destinationReset = true;
    private int deltaBasedUponOrientation = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        tripTableDb = new TripTableDb(dbAdapter);
        waypointTableDb = new WaypointTableDb(dbAdapter);
        
        Bundle extras = this.getIntent().getExtras();
        IntentWrapper wrapper = (IntentWrapper)extras.getSerializable(WhereWereWe.INTENT_WRAPPER);
        waypointTable = wrapper.getWaypointTable();
        if (waypointTable.getLatitude() == -1f && waypointTable.getLongitude() == -1f) {
        	// Location not set, assume they are going to the summit of Long's Peak.
        	waypointTable.setLatitude(40.255014f);
        	waypointTable.setLongitude(-105.615115f);
        	waypointTable.setAltitude(4346f);
        	waypointTable.setName("Long's Peak, CO");
        }
        preferenceData = wrapper.getPreferenceData();
        if (preferenceData.isMeasurementMetric()) {
        	distanceUnitAboveThresholdText = "m";
        	distanceUnitBelowThresholdText = "m";
        }
        else {
        	distanceUnitAboveThresholdText = "mi";
        	distanceUnitBelowThresholdText = "ft";
        }

        setContentView(R.layout.map_detail);
        mapView = (MapView)findViewById(R.id.myMapView);
        mapView.setClickable(true);
        mapView.setEnabled(true);
        mapView.setSatellite(false);
        mapView.setTraffic(false);
        mapView.setStreetView(false);
        registerForContextMenu(mapView);
		mapView.setBuiltInZoomControls(true);
        mapView.setOnCreateContextMenuListener(this);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mListener = new MySensorListener();

        mapController = mapView.getController();

        destination = new Location("www");
        destination.setAltitude(waypointTable.getAltitude());
        destination.setLongitude(waypointTable.getLongitude());
        destination.setLatitude(waypointTable.getLatitude());
        destination.setTime(waypointTable.getWhen());
       
        setTitle(getTitle() + " - " + waypointTable.getName());
        bMapShowing = true;

        if (troubleshootOverlay != null) {
        	mapView.getOverlays().remove(troubleshootOverlay);
        }
        if ("_NoNoLand_".equals(waypointTable.getName())) {
        	troubleshootMode = true;
        }
        if (troubleshootMode) {
        	troubleshootOverlay = new TroubleshootOverlay(true);
        	mapView.getOverlays().add(troubleshootOverlay);
        }

		determineOrientation();

        if (myLocationOverlay != null) {
        	mapView.getOverlays().remove(myLocationOverlay);
        }
        myLocationOverlay = new FixedMyLocationOverlay(this, mapView);
        mapView.getOverlays().add(myLocationOverlay);

        if (navigateToOverlay != null) {
        	mapView.getOverlays().remove(navigateToOverlay);
        }
        navigateToOverlay = new NavigateToOverlay(preferenceData.isNavigatorVisible(), preferenceData.getNavigatorSize());
        mapView.getOverlays().add(navigateToOverlay);

        if (compassOverlay != null) {
        	mapView.getOverlays().remove(compassOverlay);
        }
        compassOverlay = new CompassOverlay(preferenceData.isCompassVisible(), preferenceData.getCompassSize());
        mapView.getOverlays().add(compassOverlay);

        if (markerOverlay != null) {
        	mapView.getOverlays().remove(markerOverlay);
        }
		Drawable marker = getResources().getDrawable(R.drawable.small_dot);
		markerOverlay = new MarkerOverlay(marker, this);
        OverlayItem overlayitem = new OverlayItem(getNewGeoPoint(waypointTable.getLatitude(), waypointTable.getLongitude()), waypointTable.getName(), waypointTable.getName());
        markerOverlay.addOverlay(overlayitem);
        mapView.getOverlays().add(markerOverlay);
        
        mapView.postInvalidate();
        
        GeoPoint geoPoint = myLocationOverlay.getMyLocation();
        if (geoPoint != null) {
        	mapController.animateTo(myLocationOverlay.getMyLocation());
        	mapController.setZoom(INITIAL_ZOOM_LEVEL);
            currentLocation = myLocationOverlay.getLastFix();
            if(!navigateToOverlay.isVisible()) {
            	myLocationOverlay.disableMyLocation();
            }
        }
        else {
            myLocationOverlay.runOnFirstFix(
                    new Runnable() {
                        public void run() {
                        	mapController.animateTo(myLocationOverlay.getMyLocation());
                        	mapController.setZoom(INITIAL_ZOOM_LEVEL);
                            currentLocation = myLocationOverlay.getLastFix();
                            if(!navigateToOverlay.isVisible()) {
                            	myLocationOverlay.disableMyLocation();
                            }
                        }
                    });
        }
	}

	// This is called when the screen rotates.
    // (onCreate is no longer called when screen rotates due to manifest, see: android:configChanges)
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);

        int lastOrientation = currentOrientationValue;
	    int orientation = determineOrientation();
	    if (currentOrientationValue != lastOrientation) {

	    	if (currentOrientationValue == Surface.ROTATION_0 || currentOrientationValue == Surface.ROTATION_180) {
	    		canvassWidth = deviceWidth;
	    		canvassHeight = deviceHeight;
	    	}
	    	else {
	    		canvassWidth = deviceHeight;
	    		canvassHeight = deviceWidth;
	    	}

	    	if (compassOverlay != null) {
	    		compassOverlay.recalculatePosition();
	    	}
	    	if (navigateToOverlay != null) {
	    		navigateToOverlay.recalculatePosition();
	    	}
	
			logEvent("Orientation changed! Value=" + orientation + ", Delta=" + deltaBasedUponOrientation);
	    }

        mapView.postInvalidate();
        }

	/**
	 * Determine the orientation of the device, and set any dependent settings.
	 */
    private int determineOrientation() {
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	//	int orientation = display.getRotation();
		int orientation = display.getOrientation();
		currentOrientationValue = orientation;
		if (orientation == Surface.ROTATION_0) {
			deltaBasedUponOrientation = 0;
		}
		else if (orientation == Surface.ROTATION_90) {
			deltaBasedUponOrientation = 90;
		}
		else if (orientation == Surface.ROTATION_180) {
			deltaBasedUponOrientation = 180;
		}
		else if (orientation == Surface.ROTATION_270) {
			deltaBasedUponOrientation = -90;
		}

		return orientation;
	}

    private void incrementItemsUsingLocation() {
        if (++numItemsUsingLocation == 1) {
            myLocationOverlay.enableMyLocation();
        }
    }

    private void decrementItemsUsingLocation() {
        if (--numItemsUsingLocation == 0) {
        	myLocationOverlay.disableMyLocation();
        }
    }

    private void incrementItemsUsingSensor() {
    	logEvent("incrementItemsUsingSensor() numItemsUsingSensor=" + (numItemsUsingSensor+1));
        if (++numItemsUsingSensor == 1) {
        	logEvent("incrementItemsUsingSensor() calling mSensorManager.registerListener()");
            mSensorManager.registerListener(mListener, mSensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private void decrementItemsUsingSensor() {
    	logEvent("decrementItemsUsingSensor() numItemsUsingSensor=" + (numItemsUsingSensor-1));
        if (--numItemsUsingSensor == 0) {
        	logEvent("decrementItemsUsingSensor() calling mSensorManager.unregisterListener()");
            mSensorManager.unregisterListener(mListener);
        }
    }

    GeoPoint getNewGeoPoint(double latitude, double longitude) {
    	return new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case 0:
	            // show current location
	        	if (currentLocation != null) {
	        		mapController.animateTo(getNewGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
	        	}
	            return true;
	        case 1:
	        	if (waypointTable != null) {
	        		mapController.animateTo(getNewGeoPoint(waypointTable.getLatitude(), waypointTable.getLongitude()));
	        	}
	            return true;
	        case 2:
	            // Toggle satellite views
	        	mapView.setSatellite(!mapView.isSatellite());
	        	setMapShowing();
	            return true;
	        case 3:
	            // Toggle street views
	        	mapView.setStreetView(!mapView.isStreetView());
	        	setMapShowing();
	            return true;
	        case 4:
	            // Toggle traffic views
	        	mapView.setTraffic(!mapView.isTraffic());
	        	setMapShowing();
	            return true;
	    }

		return super.onContextItemSelected(item);
	}

	private void setMapShowing() {
		bMapShowing = !mapView.isSatellite();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	    menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.show_current_location));
	    menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.show_destination));
	    menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.satellite));
	    menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.map));
	    menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.traffic));
	}

	@Override
	public void onPause() {
		super.onPause();
		
		myLocationOverlay.disableMyLocation();
    	if (mSensorManager != null && numItemsUsingSensor > 0) {
    		mSensorManager.unregisterListener(mListener);
    	}
		compassOverlay.pause();
		navigateToOverlay.pause();
//    	if (locationManager != null && locationListener != null) {
//    		locationManager.removeUpdates(locationListener);
//    	}
	}

    @Override
    protected void onResume() {
        super.onResume();

        if (numItemsUsingLocation > 0) {
        	myLocationOverlay.enableMyLocation();
        }
    	if (mSensorManager != null && numItemsUsingSensor > 0) {
    		mSensorManager.registerListener(mListener, mSensor,
    				SensorManager.SENSOR_DELAY_GAME);
    	}
		compassOverlay.resume();
		navigateToOverlay.resume();
    }

    
    @Override
    protected void onStop(){
    	if (mSensorManager != null && numItemsUsingSensor > 0) {
    		mSensorManager.unregisterListener(mListener);
    		mSensorManager = null;
    	}
		myLocationOverlay.disableMyLocation();
		compassOverlay.pause();
		navigateToOverlay.pause();

		if (prefsChanged) {
	    	// Save user preferences using an Editor object
	        // All objects are from android.context.Context
	        SharedPreferences settings = getSharedPreferences(WhereWereWe.PREFS_FILE, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putString(Preferences.KEY_PREFERENCE_MEASUREMENT, preferenceData.getMeasurement());
	        editor.putString(Preferences.KEY_PREFERENCE_COORDINATE, preferenceData.getCoordinate());
	        editor.putString(Preferences.KEY_PREFERENCE_COMPASS_VISIBLE, preferenceData.isCompassVisible() ? "true" : "false");
	        editor.putString(Preferences.KEY_PREFERENCE_NAVIGATOR_VISIBLE, preferenceData.isNavigatorVisible() ? "true" : "false");
	        editor.putString(Preferences.KEY_PREFERENCE_COMPASS_SIZE, Integer.toString(preferenceData.getCompassSize()));
	        editor.putString(Preferences.KEY_PREFERENCE_NAVIGATOR_SIZE, Integer.toString(preferenceData.getNavigatorSize()));

	        // Commit the edits
	        editor.commit();
		}
        super.onStop();
    }

	@Override
	protected void onDestroy() {
		if (dbAdapter != null) {
			dbAdapter.close();
		}
		super.onDestroy();
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	// Inflate the currently selected menu XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.more:
        		launchMoreMenu();
                return true;
        	case R.id.mylocation:
                // show current location
            	if (currentLocation != null) {
            		mapController.animateTo(getNewGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
            	}
                return true;
        	case R.id.showdestination:
            	if (waypointTable != null) {
            		mapController.animateTo(getNewGeoPoint(waypointTable.getLatitude(), waypointTable.getLongitude()));
            	}
                return true;
        	case R.id.goto_location:
                // change navigate to location
        		showNavigateToLocationMenu();
        		
                return true;
	        case R.id.preferences:
	            // When the Help button is clicked, launch Preferences as a sub-activity
	            Intent launchPreferencesIntent = new Intent().setClass(this, Preferences.class);
	            
	            // Make it a subactivity so we know when it returns
	            startActivityForResult(launchPreferencesIntent, WhereWereWe.REQUEST_CODE_PREFERENCES);
	            return true;
        	}
        return true;
    }

    /**
     * Invoked when the Navigate To Location button is pressed.
     */
    private void showNavigateToLocationMenu() {
		// When done, set waypointTable

    	Intent launchIntent = new Intent().setClass(this, MenuListDialog.class);
	   	Bundle extras = new Bundle();
	   	long waypointID;
	   	long tripID;
	   	String name;
	   	String tripName;
	   	long lastTripID = -1;
	   	long currentTripID = waypointTable.getParentTripId();

	   	MenuListSelectRow row = null;
	   	MenuListBundle bundle = new MenuListBundle(getString(R.string.navigate_to_location),getString(R.string.cancel), 5);
	   	Cursor cursor = waypointTableDb.fetchWaypointDisplayList();
        if (cursor != null) {
	        int columnIndex;
	        while (cursor.moveToNext()) {
	        	columnIndex = 0;
	        	waypointID = cursor.getLong(columnIndex++);
	        	tripID = cursor.getLong(columnIndex++);
	        	name = cursor.getString(columnIndex++);
	        	
	        	if (tripID != lastTripID) {
	        		TripTable trip = tripTableDb.fetchTrip(tripID);
	        		if (trip != null) {
	        			tripName = trip.getName();
	        		}
	        		else {
	        			tripName = getString(R.string.unknown);
	        		}
	        		lastTripID = tripID;
		    	   	row = new MenuListSelectRow(tripName, 5);

		    	   	// Expand the current trip, assuming they most likely will want to navigate to a waypoint in it.
		    	   	if (trip.getId() == currentTripID) {
		    	   		row.toggleExpanded();
		    	   	}
        		   	bundle.addMenuItem(row);
	        	}
	        	row.add(name, R.drawable.forward_32x32, Long.toString(waypointID));
		    }
	        cursor.close();
        }

	   	extras.putSerializable(MenuListDialog.BUNDLE_MENU_LIST_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, REQUEST_CODE_NAVIGATE_TO_LOCATION);
    }

    /**
     * Process the result of the Preferences activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onPreferencesResult(int requestCode, int resultCode, Intent data) {
    	prefsChanged = true;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceData.setMeasurement(settings.getString(Preferences.KEY_PREFERENCE_MEASUREMENT, Preferences.DEFAULT_PREFERENCE_MEASUREMENT));
        preferenceData.setCoordinate(settings.getString(Preferences.KEY_PREFERENCE_COORDINATE, Preferences.DEFAULT_PREFERENCE_COORDINATE));
        Boolean bool = new Boolean(settings.getString(Preferences.KEY_PREFERENCE_COMPASS_VISIBLE, Preferences.DEFAULT_PREFERENCE_COMPASS_VISIBLE));
        preferenceData.setCompassVisible(bool.booleanValue());
        bool = new Boolean(settings.getString(Preferences.KEY_PREFERENCE_NAVIGATOR_VISIBLE, Preferences.DEFAULT_PREFERENCE_NAVIGATOR_VISIBLE));
        preferenceData.setNavigatorVisible(bool.booleanValue());
        Integer num = Integer.parseInt(settings.getString(Preferences.KEY_PREFERENCE_COMPASS_SIZE, Preferences.DEFAULT_PREFERENCE_COMPASS_SIZE));
        preferenceData.setCompassSize(num.intValue());
        num = Integer.parseInt(settings.getString(Preferences.KEY_PREFERENCE_NAVIGATOR_SIZE, Preferences.DEFAULT_PREFERENCE_NAVIGATOR_SIZE));
        preferenceData.setNavigatorSize(num.intValue());

        // Now check for changes
        if (preferenceData.getCompassSize() != compassOverlay.getScale()) {
        	compassOverlay.setScale(preferenceData.getCompassSize());
        }
        if (preferenceData.getNavigatorSize() != navigateToOverlay.getScale()) {
        	navigateToOverlay.setScale(preferenceData.getNavigatorSize());
        }
    }

	private void launchMoreMenu() {
    	Intent launchIntent = new Intent().setClass(this, MenuListDialog.class);
	   	Bundle extras = new Bundle();
	   	MenuListBundle bundle = new MenuListBundle(getString(R.string.options),getString(R.string.cancel), 3);
	   	
	   	MenuListSelectRow row;
	   	if (compassOverlay.isVisible()) {
	   		bundle.add(getString(R.string.hide_compass), R.drawable.compass, Integer.toString(OPTION_COMPASS_HIDE));
	   	}
	   	else {
	   		bundle.add(getString(R.string.show_compass), R.drawable.compass, Integer.toString(OPTION_COMPASS_SHOW));
	   	}

	   	if (navigateToOverlay.isVisible()) {
	   		bundle.add(getString(R.string.hide_navigator), R.drawable.navigator, Integer.toString(OPTION_NAVIGATOR_HIDE));
	   	}
	   	else {
	   		bundle.add(getString(R.string.show_navigator), R.drawable.navigator, Integer.toString(OPTION_NAVIGATOR_SHOW));
	   	}

	   	row = new MenuListSelectRow(getString(R.string.map_mode), 3);
	   	row.add(getString(R.string.map), R.drawable.highway, Integer.toString(OPTION_SHOW_MAP));
	   	row.add(getString(R.string.satellite), R.drawable.satellite, Integer.toString(OPTION_SHOW_SATELLITE));
	   	row.add(getString(R.string.traffic), R.drawable.traffic, Integer.toString(OPTION_SHOW_TRAFFIC));
	   	bundle.addMenuItem(row);

	   	extras.putSerializable(MenuListDialog.BUNDLE_MENU_LIST_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, REQUEST_CODE_MORE_OPTIONS);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_MORE_OPTIONS:
	        	onMoreOptionsResult(requestCode, resultCode, data);
				break;
			case REQUEST_CODE_NAVIGATE_TO_LOCATION:
				onNavigateToLocationResult(data);
				break;
			case WhereWereWe.REQUEST_CODE_PREFERENCES:
		        onPreferencesResult(requestCode, resultCode, data);
				break;
			}
		}
	}

    /**
     * Process the result of the Navigate To Location activity when it finishes.
     * @param data
     */
	private void onNavigateToLocationResult(Intent data) {

        Bundle extras = data.getExtras();
        String idStr = (String)extras.getSerializable(MenuListDialog.BUNDLE_MENU_LIST_RETURN);
        if (idStr != null) {
			int waypointID = Integer.parseInt(idStr);
			WaypointTable waypoint = waypointTableDb.fetchWaypoint(waypointID);
			if (waypoint != null && waypoint.getLatitude() != -1f && waypoint.getLongitude() != -1f) {
				waypointTable = waypoint;
				
		        if (troubleshootOverlay != null) {
		        	mapView.getOverlays().remove(troubleshootOverlay);
		            troubleshootOverlay = null;
		        }

		        if ("_NoNoLand_".equals(waypointTable.getName())) {
		        	troubleshootMode = true;
		        }
		        if (troubleshootMode) {
		        	troubleshootOverlay = new TroubleshootOverlay(true);
		        	mapView.getOverlays().add(troubleshootOverlay);
		        }

		        destination = new Location("www");
		        destination.setAltitude(waypointTable.getAltitude());
		        destination.setLongitude(waypointTable.getLongitude());
		        destination.setLatitude(waypointTable.getLatitude());
		        destination.setTime(waypointTable.getWhen());
		        destinationReset = true;

		        setTitle(getTitle() + " - " + waypointTable.getName());

		        mapView.postInvalidate();
			}
        }
	}

    /**
     * Process the result of the main options activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onMoreOptionsResult(int requestCode, int resultCode, Intent data) {

        Bundle extras = data.getExtras();
        String selectedStr = (String)extras.getSerializable(MenuListDialog.BUNDLE_MENU_LIST_RETURN);
        if (selectedStr != null) {
        	try {
				int selected = Integer.parseInt(selectedStr);
				switch(selected) {
				case OPTION_COMPASS_SHOW:
					compassOverlay.setVisible(true);
					break;
				case OPTION_COMPASS_HIDE:
					compassOverlay.setVisible(false);
					break;
				case OPTION_NAVIGATOR_SHOW:
					navigateToOverlay.setVisible(true);
					break;
				case OPTION_NAVIGATOR_HIDE:
					navigateToOverlay.setVisible(false);
					break;
				case OPTION_SHOW_MAP:
	            	mapView.setStreetView(!mapView.isStreetView());
		        	setMapShowing();
					break;
				case OPTION_SHOW_SATELLITE:
	            	mapView.setSatellite(!mapView.isSatellite());
		        	setMapShowing();
					break;
				case OPTION_SHOW_TRAFFIC:
	            	mapView.setTraffic(!mapView.isTraffic());
		        	setMapShowing();
					break;
				}
			} catch (NumberFormatException e) {
				Toast.makeText(this, "Invalid entry! Expected a number but received " + selectedStr, Toast.LENGTH_SHORT).show();
			} 
        }
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    switch (keyCode) {
	        case KeyEvent.KEYCODE_DPAD_UP: // zoom in
	        	mapController.zoomIn();
	            return true;
	        case KeyEvent.KEYCODE_DPAD_DOWN: // zoom out
	        	mapController.zoomOut();
	            return true;
	        case KeyEvent.KEYCODE_BACK: // go back (meaning exit the app)
	            finish();
	            return true;
	        default:
	            return false;
	    }
	}


    public String formatDistanceText(float distanceInMeters) {
    	StringBuilder sb = new StringBuilder();
    	if (preferenceData.isMeasurementMetric()) {
    		sb.append((int)distanceInMeters);
    		sb.append(" ");
    		sb.append(distanceUnitAboveThresholdText);
    	}
    	else {
    		if (distanceInMeters > METERS_PER_MILE) {
    			float miles = distanceInMeters / METERS_PER_MILE;
    			sb.append(StringUtilities.formatData(miles, 1));
        		sb.append(" ");
        		sb.append(distanceUnitAboveThresholdText);
    		}
    		else {
    			float feet = distanceInMeters * FEET_PER_METER;
    			sb.append((int)feet);
        		sb.append(" ");
        		sb.append(distanceUnitBelowThresholdText);
    		}
    	}
    	return sb.toString();
    }

    private void logEvent(String text) {
		Log.i(this.getClass().getName(), text);	
    }

	/**
	 * Draw an overlay on the map
	 */
    private class CompassOverlay extends Overlay implements LongpressListener {

    	private static final int SCALE_MIN = 1;
    	private static final int SCALE_MAX = 20;

    	private int m_compassHeight = 60;
    	private int m_compassWidth = 10;
    	private int m_compassTouchRadius = m_compassWidth * 2;
    	private int m_scale = 4;
    	private boolean m_visible = true;

    	private Paint   mPaint = new Paint();
        private Path    mPath = new Path();
        private float cx = -1;
        private float cy = -1;
        private boolean initialPress = false;
        private boolean touchingMe = false;
        private boolean checkIfStillTouchingMe = false;
        private int textHalfWidth = -1;
        private String text = "N";
        private boolean initialized = false;
    	private boolean calculatePosition = true;
        private int savedDistanceFromRight = 0;
        private int savedDistanceFromBottom = 0;

        private PressDurationTimer pressDurationTimer = new PressDurationTimer(this);

        /**
         * @param marker the push-pin
         */
        public CompassOverlay(boolean visible, int scale) {
            super();
            m_scale = scale;
            m_visible = visible;
            if (m_visible) {
            	logEvent("CompassOverlay constructor calling incrementItemsUsingSensor()");
            	incrementItemsUsingSensor();
            }
            else {
            	logEvent("CompassOverlay constructor NOT calling incrementItemsUsingSensor()");
            }
        }

        private void initialize(int width, int height) {
        	
        	m_canvassLongLen = Math.max(height, width);

        	setPath();
        	cx = 30;
        	cy = 50;
        	calculatePosition = false;

        	initialized = true;
        }

        private void doCalculatePosition(int width, int height) {
        	if (cx < 0) {
        		cx = 30;
        	}
        	if (cy < 0) {
        		cy = 50;
        	}

        	if (cx + m_compassWidth > width) {
        		// We've changed orientation, so the current height was recently the width, so use that to calculate savedDistanceFromRight
        		savedDistanceFromRight = height - (int)cx;
            	cx = width - m_compassHeight - 60;
        	}
        	else {
        		if (savedDistanceFromRight > 0) {
        			cx = width - savedDistanceFromRight;
        		}
        		savedDistanceFromRight = 0;
        	}
    		
        	if (cy + m_compassHeight > height) {
        		// We've changed orientation, so the current width was recently the height, so use that to calculate savedDistanceFromBottom
        		savedDistanceFromBottom = width - (int)cy;
            	cy = height - m_compassHeight - 40;
        	}
        	else {
        		if (savedDistanceFromBottom > 0) {
        			cy = height - savedDistanceFromBottom;
        		}
        		savedDistanceFromBottom = 0;
        	}

        	calculatePosition = false;
        }

        public void recalculatePosition() {
        	calculatePosition = true;        	
        }

        private void setPath() {
        	mPath = new Path();

        	m_compassHeight = (int)((m_canvassLongLen / 20) * (m_scale / 2)); // m_scale can range from 1 to 8
        	m_compassWidth = m_compassHeight / 3;
        	m_compassTouchRadius = m_compassWidth;

            // Construct a wedge-shaped path
//          mPath.moveTo(0, -50);
//          mPath.lineTo(-20, 60);
//          mPath.lineTo(0, 50);
//          mPath.lineTo(20, 60);

            mPath.moveTo(0, -((m_compassHeight / 3) + (m_compassHeight / 10))); 	// (0, -26)
            mPath.lineTo(-(m_compassWidth / 2), -(m_compassHeight / 3));			// (-5,-20)
            mPath.lineTo(0, -((m_compassHeight / 3) * 2));						// (0, -40)
            mPath.lineTo(0, (m_compassHeight / 3));								// (0, 20)
            mPath.close();
        }

        public int getScale() {
        	return m_scale;
        }

        public void setScale(int scale) {
        	if (m_scale >= SCALE_MIN && m_scale <= SCALE_MAX) {
        		m_scale = scale;
    	        mapView.postInvalidate();
        	}
        }

        public void larger() {
        	if (m_scale < SCALE_MAX) {
        		m_scale++;
    	        mapView.postInvalidate();
        	}
        	setPath();
        }

        public boolean isLargest() {
        	return m_scale == SCALE_MAX;
        }
        
        public void smaller() {
        	if (m_scale > SCALE_MIN) {
        		m_scale--;
    	        mapView.postInvalidate();
        	}
        	setPath();
        }

        public boolean isSmallest() {
        	return m_scale == SCALE_MIN;
        }

        public boolean isVisible() {
			return m_visible;
		}

		public void setVisible(boolean visible) {
			m_visible = visible;
			if (m_visible) {
				resume();
            	logEvent("CompassOverlay setVisible(true) calling incrementItemsUsingSensor()");
	            incrementItemsUsingSensor();
			}
			else {
				pause();
            	logEvent("CompassOverlay setVisible(false) calling decrementItemsUsingSensor()");
	            decrementItemsUsingSensor();
			}
	        mapView.postInvalidate();
		}

		public void resume() {
        	initialPress = false;
        	touchingMe = false;
        	checkIfStillTouchingMe = false;
        }

        public void pause() {
        	if (initialPress) {
        		initialPress = false;
        		mLongpressHandler.removeCallbacks(pressDurationTimer);
        	}
        	touchingMe = false;
        	checkIfStillTouchingMe = false;
        }

        @Override
		public boolean onTouchEvent(MotionEvent event, MapView view) {
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
            	if (Math.abs(event.getX() - cx) <= m_compassTouchRadius &&
            			Math.abs(event.getY() - cy) <= m_compassTouchRadius) {
            	    mLongpressHandler.removeCallbacks(pressDurationTimer);
            	    mLongpressHandler.postDelayed(pressDurationTimer, LONG_PRESS_DURATION);
            	    touchingMe = true;
            	}
            	else {
            		touchingMe = false;
            		checkIfStillTouchingMe = false;
            	}
            	initialPress = touchingMe;
                return touchingMe;
            }
            else if (MotionEvent.ACTION_UP == event.getAction()) {
            	// Happens after the moves are done, if there were any, so reset the flags
            	if (initialPress) {
            		initialPress = false;
            		mLongpressHandler.removeCallbacks(pressDurationTimer);
            	}
            	touchingMe = false;
            	checkIfStillTouchingMe = false;
            	return false;
            }

            if (MotionEvent.ACTION_MOVE != event.getAction() || !touchingMe || initialPress) {
                return false;
            }

            if (checkIfStillTouchingMe) {
            	// Only happens the first move event after the long press timer went off to make sure they are still pressing this overlay
            	checkIfStillTouchingMe = false;            	
            	if (!(Math.abs(event.getX() - cx) <= m_compassTouchRadius &&
            			Math.abs(event.getY() - cy) <= m_compassTouchRadius)) {
            		touchingMe = false;
            		return false;
            	}
            }

//        	System.out.println("Compass Move!");
            cx = event.getX();
            cy = event.getY();
            return true;
		}

		@Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {

            if (!m_visible) {
            	return;
            }

            Paint paint = mPaint;
            if (!shadow) {
	            paint.setAntiAlias(true);

            	if (canvassWidth == 0) {
            		// Get the canvas height and width from the system only once. In the future, every time we change orientation they will be swapped.
        	    	canvassWidth = canvas.getWidth();
        	    	canvassHeight = canvas.getHeight();
        	    	
        	    	// This next check sets the device dimensions. These will not change once they are set.
        	    	if (currentOrientationValue == Surface.ROTATION_0 || currentOrientationValue == Surface.ROTATION_180) {
        	    		// If the device orientation is not rotated or it is upside down, then the canvas dimensions match the device dimensions.
        	    		deviceWidth = canvassWidth;
        	    		deviceHeight = canvassHeight;
        	    	}
        	    	else {
        	    		// If the device orientation is sideways, then the canvas dimensions do not match the device dimensions, so swap them.
        	    		deviceWidth = canvassHeight;
        	    		deviceHeight = canvassWidth;
        	    	}
            	}

            	if (!initialized) {
	            	initialize(canvassWidth, canvassHeight);
	            }
	        	if (calculatePosition) {
            		doCalculatePosition(canvassWidth, canvassHeight);
	        	}

	            if (touchingMe) {
	            	if (initialPress) {
	            		// They are holding the icon but it is not yet moving
	    	            paint.setColor(Color.rgb(228, 212, 122)); // yellowish
	            	}
	            	else {
	            		// They are moving the icon
	    	            paint.setColor(Color.rgb(102, 0, 0)); // redish
	            	}
		            paint.setStyle(Paint.Style.FILL);
	                paint.setAlpha(100);
	                canvas.drawCircle(cx, cy, m_compassHeight / 2, paint);
	                paint.setAlpha(255);
	            }

	            paint.setColor(bMapShowing ? Color.BLACK : Color.WHITE);
	            paint.setStyle(Paint.Style.STROKE);
	            float stroke = paint.getStrokeWidth();
	            paint.setStrokeWidth(2);

	            canvas.save();
	            canvas.translate(cx, cy);
	            canvas.rotate(-compassHeading);
	            canvas.drawPath(mPath, mPaint);
	            canvas.restore();
	            paint.setColor(Color.BLUE);
	            paint.setStrokeWidth(stroke);
	            paint.setTextSize(Math.min(36, m_compassHeight/2));
	            if (textHalfWidth < 0) {
	            	Rect bounds = new Rect(0, 0, 0, 0);
	            	paint.getTextBounds(text, 0, text.length(), bounds);
	            	textHalfWidth = (bounds.width() / 3) * 2;
	            }
	            canvas.drawText(text, cx - textHalfWidth, cy + (m_compassHeight / 3) - 10, paint);

            }
        }

		@Override
		public void longPress() {
			initialPress = false;
			checkIfStillTouchingMe = true;
			mapView.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING );
		}
	}

	/**
	 * Draw an overlay on the map
	 */
    private class NavigateToOverlay extends Overlay implements LongpressListener {

    	private static final int SCALE_MIN = 1;
    	private static final int SCALE_MAX = 20;

    	private int m_compassHeight = 60;
    	private int m_compassWidth = 20;
    	private int m_compassTouchRadius = m_compassWidth;
    	
    	private int m_scale = 4;

    	private Paint   mPaint = new Paint();
        private Path    mPath = null;
        private float cx = -1;
        private float cy = -1;
        private boolean initialPress = false;
        private boolean touchingMe = false;
        private boolean checkIfStillTouchingMe = false;
        private String text = "";
        private int textHalfWidth = -1;
        private boolean initialized = false;
    	private boolean m_visible = true;
    	private boolean calculatePosition = true;
        private int savedDistanceFromRight = 0;
        private int savedDistanceFromBottom = 0;

        private PressDurationTimer pressDurationTimer = new PressDurationTimer(this);

        /**
         * @param marker the push-pin
         */
        public NavigateToOverlay(boolean visible, int scale) {
            super();
            m_scale = scale;
            m_visible = visible;
            if (m_visible) {
            	incrementItemsUsingLocation();
            	logEvent("NavigateToOverlay constructor calling incrementItemsUsingSensor()");
            	incrementItemsUsingSensor();
            }
            else {
            	logEvent("NavigateToOverlay constructor NOT calling incrementItemsUsingSensor()");
            }
        }

        private void initialize(int width, int height) {

        	m_canvassLongLen = Math.max(height, width);

        	setPath();

        	cx = width - m_compassHeight - 10;
    		savedDistanceFromRight = width - (int)cx;
        	cy = 50;
        	calculatePosition = false;

        	initialized = true;
        }

        private void doCalculatePosition(int width, int height) {

        	if (cx < 0) {
        		cx = 90;
        	}
        	if (cy < 0) {
        		cy = 50;
        	}

//            Log.i(this.getClass().getName(), "doCalculatePosition(jjs): " +
//            		" width="+width + " height=" + height + " savedDistanceFromRight=" + savedDistanceFromRight + " cx=" + cx);

        	if (cx + m_compassWidth > width) {
        		// We've changed orientation, so the current height was recently the width, so use that to calculate savedDistanceFromRight
        		savedDistanceFromRight = height - (int)cx;
            	cx = width - m_compassHeight - 10;
//                Log.i(this.getClass().getName(), "doCalculatePosition(jjs): " +
//                		" set savedDistanceFromRight to "+savedDistanceFromRight + ", set csx to " + cx);
        	}
        	else {
        		if (savedDistanceFromRight > 0) {
        			cx = width - savedDistanceFromRight;
//                    Log.i(this.getClass().getName(), "doCalculatePosition(jjs): " +
//                    		" set cx to "+cx);
        		}
        		savedDistanceFromRight = 0;
        	}
    		
        	if (cy + m_compassHeight > height) {
        		// We've changed orientation, so the current width was recently the height, so use that to calculate savedDistanceFromBottom
        		savedDistanceFromBottom = width - (int)cy;
            	cy = height - m_compassHeight - 40;
        	}
        	else {
        		if (savedDistanceFromBottom > 0) {
            		// We've changed orientation, so the current width was recently the height, so use that to calculate cy
        			cy = height - savedDistanceFromBottom;
        		}
        		savedDistanceFromBottom = 0;
        	}

        	calculatePosition = false;
        }

        public void recalculatePosition() {
        	calculatePosition = true;        	
        }

        public int getScale() {
        	return m_scale;
        }

        public void setScale(int scale) {
        	if (m_scale >= SCALE_MIN && m_scale <= SCALE_MAX) {
        		m_scale = scale;
    	        mapView.postInvalidate();
        	}
        }

        public void larger() {
        	if (m_scale < SCALE_MAX) {
        		m_scale++;
    	        mapView.postInvalidate();
        	}
        	setPath();
        }
        
        public boolean isLargest() {
        	return m_scale == SCALE_MAX;
        }
        
        public void smaller() {
        	if (m_scale > SCALE_MIN) {
        		m_scale--;
    	        mapView.postInvalidate();
        	}
        	setPath();
        }

        public boolean isSmallest() {
        	return m_scale == SCALE_MIN;
        }

        public boolean isVisible() {
			return m_visible;
		}

		public void setVisible(boolean visible) {
			m_visible = visible;
			if (m_visible) {
				resume();
            	incrementItemsUsingLocation();
            	logEvent("NavigateToOverlay setVisible(true) calling incrementItemsUsingSensor()");
	            incrementItemsUsingSensor();
			}
			else {
				pause();
				decrementItemsUsingLocation();
            	logEvent("NavigateToOverlay setVisible(false) calling decrementItemsUsingSensor()");
				decrementItemsUsingSensor();
			}
	        mapView.postInvalidate();
		}

        private void setPath() {
        	mPath = new Path();

        	m_compassHeight = (int)((m_canvassLongLen / 20) * (m_scale / 2)); // m_scale can range from 1 to 8
        	m_compassWidth = m_compassHeight / 3;
        	m_compassTouchRadius = m_compassWidth;

            // Construct a wedge-shaped path
//          mPath.moveTo(0, -50);
//          mPath.lineTo(-20, 60);
//          mPath.lineTo(0, 50);
//          mPath.lineTo(20, 60);

        	int halfheight = m_compassHeight / 2;
        	int halfwidth = m_compassWidth / 2;
            mPath.moveTo(0, -halfheight);
            mPath.lineTo(-halfwidth, halfheight);
            mPath.lineTo(0, halfheight - (m_compassHeight / 10));
            mPath.lineTo(halfwidth, halfheight);
            mPath.close();
        }

        public void setText(String txt) {
        	text = txt;
        	textHalfWidth = -1;
        }

        public void setText(float distanceInMeters) {
        	text = formatDistanceText(distanceInMeters);
        	textHalfWidth = -1;
        }

        public void resume() {
        	initialPress = false;
        	touchingMe = false;
        	checkIfStillTouchingMe = false;
        }

        public void pause() {
        	if (initialPress) {
        		initialPress = false;
        		mLongpressHandler.removeCallbacks(pressDurationTimer);
        	}
        	touchingMe = false;
        	checkIfStillTouchingMe = false;
        }

        @Override
		public boolean onTouchEvent(MotionEvent event, MapView view) {
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
            	if (Math.abs(event.getX() - cx) <= m_compassTouchRadius &&
            			Math.abs(event.getY() - cy) <= m_compassTouchRadius) {
            	    mLongpressHandler.removeCallbacks(pressDurationTimer);
            	    mLongpressHandler.postDelayed(pressDurationTimer, LONG_PRESS_DURATION);
            	    touchingMe = true;
//            		System.out.println("NavigateToOverlay: onTouchEvent(ACTION_DOWN) setting initialPress, touchingMe to true");
            	}
            	else {
//            		System.out.println("NavigateToOverlay: onTouchEvent(ACTION_DOWN) setting initialPress, touchingMe to FALSE");
            		touchingMe = false;
                	checkIfStillTouchingMe = false;
            	}
            	initialPress = touchingMe;
                return touchingMe;
            }
            else if (MotionEvent.ACTION_UP == event.getAction()) {
            	// Happens after the moves are done, if there were any, so reset the flags
            	if (initialPress) {
//            		System.out.println("NavigateToOverlay: onTouchEvent(ACTION_UP) initialPress was true, setting false and removing callback");
            		initialPress = false;
            		mLongpressHandler.removeCallbacks(pressDurationTimer);
            	}
//            	else {
//            		System.out.println("NavigateToOverlay: onTouchEvent(ACTION_UP) initialPress was false");
//            	}
            	touchingMe = false;
            	checkIfStillTouchingMe = false;
            	return false;
            }

            if (MotionEvent.ACTION_MOVE != event.getAction() || !touchingMe || initialPress) {
                return false;
            }

            if (checkIfStillTouchingMe) {
            	// Only happens the first move event after the long press timer went off to make sure they are still pressing this overlay
            	checkIfStillTouchingMe = false;            	
            	if (!(Math.abs(event.getX() - cx) <= m_compassTouchRadius &&
            			Math.abs(event.getY() - cy) <= m_compassTouchRadius)) {
            		touchingMe = false;
            		return false;
            	}
            }

//    		System.out.println("NavigateToOverlay: onTouchEvent(ACTION_MOVE) moving");
            cx = event.getX();
            cy = event.getY();
            return true;
		}

		@Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {

            if (!m_visible) {
            	return;
            }

            Paint paint = mPaint;
            if (!shadow) {
//              canvas.drawColor(Color.WHITE);
	            paint.setAntiAlias(true);
	            paint.setStyle(Paint.Style.FILL);

            	if (canvassWidth == 0) {
            		// Get the canvas height and width from the system only once. In the future, every time we change orientation they will be swapped.
        	    	canvassWidth = canvas.getWidth();
        	    	canvassHeight = canvas.getHeight();
        	    	
        	    	// This next check sets the device dimensions. These will not change once they are set.
        	    	if (currentOrientationValue == Surface.ROTATION_0 || currentOrientationValue == Surface.ROTATION_180) {
        	    		// If the device orientation is not rotated or it is upside down, then the canvas dimensions match the device dimensions.
        	    		deviceWidth = canvassWidth;
        	    		deviceHeight = canvassHeight;
        	    	}
        	    	else {
        	    		// If the device orientation is sideways, then the canvas dimensions do not match the device dimensions, so swap them.
        	    		deviceWidth = canvassHeight;
        	    		deviceHeight = canvassWidth;
        	    	}
            	}

        		if (troubleshootMode) {
        			StringBuilder sb = new StringBuilder();
        			sb.append("Or:");
        			sb.append(currentOrientationValue);
        			sb.append(" w:");
        			sb.append(canvassWidth);
        			sb.append(" h:");
        			sb.append(canvassHeight);
        	        troubleshootOverlay.setText(sb.toString());
        		}
	            if (!initialized) {
	            	initialize(canvassWidth, canvassHeight);
	            }
//	            Log.i(this.getClass().getName(), "draw(jjs): calculatePosition=" + calculatePosition +
//	            		" canvassWidth="+canvassWidth + " cx=" + cx + " canvassHeight=" + canvassHeight);
	        	if (calculatePosition) {
            		doCalculatePosition(canvassWidth, canvassHeight);
	        	}

	            if (touchingMe) {
	            	if (initialPress) {
	            		// They are holding the icon but it is not yet moving
	    	            paint.setColor(Color.rgb(228, 212, 122)); // yellowish
	            	}
	            	else {
	            		// They are moving the icon
	    	            paint.setColor(Color.rgb(102, 0, 0)); // redish
	            	}
	                paint.setAlpha(100);
	                canvas.drawCircle(cx, cy, m_compassHeight / 2, paint);
	                paint.setAlpha(255);
	            }

	            paint.setColor(bMapShowing ? Color.BLACK : Color.WHITE);
	            canvas.save();
	            canvas.translate(cx, cy);
	            canvas.rotate(navigationHeading);
	            canvas.drawPath(mPath, mPaint);
	            canvas.restore();
	            int textSize = 16;
	            if (m_compassHeight < 41) {
	            	textSize = 12;
	            }
	            else if (m_compassHeight < 101) {
	            	textSize = 16;
	            }
	            else {
	            	textSize = 18;
	            }
	            paint.setTextSize(textSize);
	            if (textHalfWidth < 0) {
	            	Rect bounds = new Rect(0, 0, 0, 0);
	            	paint.getTextBounds(text, 0, text.length(), bounds);
	            	textHalfWidth = bounds.width() / 2;
	            }
	            canvas.drawText(text, cx - textHalfWidth, cy + (m_compassHeight / 2) + 10, paint);

            }
        }

		@Override
		public void longPress() {
//    		System.out.println("NavigateToOverlay: longPress() setting initialPress to FALSE");
			initialPress = false;
			checkIfStillTouchingMe = true;
			mapView.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING );
		}
	}

    private class MyLocationListener implements LocationListener 
    {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
            	if (currentLocation == null) {
            		Log.i(this.getClass().getName(), "Initially setting current location in listener");
            		currentLocation = location;
                    Drawable marker = getResources().getDrawable(R.drawable.small_dot);
                    marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
               	 	Toast.makeText(MapDetail.this, "Setting Current Location from onCreate()", Toast.LENGTH_SHORT).show();
            	}
            	else {
            		Log.i(this.getClass().getName(), "Setting current location in listener");
            		currentLocation = location;
            	}
            	GeoPoint point = getNewGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
             	mapController.animateTo(point);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, 
            Bundle extras) {}
    }        

    private class MySensorListener implements SensorEventListener {
//    	private final static long DECLINATION_CHECK_TIME = 1000 * 60; // 1 minute
    	private final static long DECLINATION_CHECK_TIME = 1000; // 1 second
    	private long nextTimeToCheckeclination = 0;
    	private long nextTimeToShowMsg = 0;
        private long nextTimeToWriteText = 0;


        public void onSensorChanged(SensorEvent event) {
        	boolean currentlyNavigating = currentLocation != null && navigateToOverlay.isVisible();
        	long currentTime = 0;
        	if (!currentlyNavigating) {
        	   	if (navigateToOverlay.isVisible()) {
                    currentTime = System.currentTimeMillis();
                    if (nextTimeToShowMsg == 0) {
                    	// Allow a little time for the location to be gathered before we report that we can't get it 
                    	nextTimeToShowMsg = currentTime + (1000 * 20); // 20 seconds
                    }
                    else if (currentTime > nextTimeToShowMsg) {
                    	// No location to be had for a while, so report it.
                    	Toast.makeText(MapDetail.this, getString(R.string.log_no_location_for_sensor), Toast.LENGTH_SHORT).show();
                    	nextTimeToShowMsg = currentTime + (1000 * 60 * 5);
                    }
        	   	}
        	   	if (!compassOverlay.isVisible()) {
            		return;
        	   	}
        	}
        	else {
        		nextTimeToShowMsg = 0;
        	}

        	float[] mValues = event.values;
            if (mValues != null && mValues.length > 0) {
            	// values[0]: Azimuth, angle between the magnetic north direction and the y-axis, around the z-axis (0 to 359). 0=North, 90=East, 180=South, 270=West
            	// values[1]: Pitch, rotation around x-axis (-180 to 180), with positive values when the z-axis moves toward the y-axis.
            	// values[2]: Roll, rotation around y-axis (-90 to 90), with positive values when the x-axis moves toward the z-axis.
            	float azimuth = mValues[0];
            	if (currentTime == 0) {
            		currentTime = System.currentTimeMillis();
            	}
                if (currentLocation != null && (destinationReset || currentTime > nextTimeToCheckeclination)) {
                	// Don't need to do this all the time because it will be a very small change.
                	// It depends on their location
                 	GeomagneticField geoField = new GeomagneticField(
                 	         Double.valueOf(currentLocation.getLatitude()).floatValue(),
                 	         Double.valueOf(currentLocation.getLongitude()).floatValue(),
                 	         Double.valueOf(currentLocation.getAltitude()).floatValue(),
                 	         currentTime
                 	         );
                 	declination = geoField.getDeclination();
                 	nextTimeToCheckeclination = currentTime + DECLINATION_CHECK_TIME;
                }

                // Correct the heading for the difference between magnetic north and true north
                compassHeading = azimuth + declination;
                if (compassHeading > 359) {
                	compassHeading -= 360;
                }
                if (deltaBasedUponOrientation != 0) {
                	compassHeading += deltaBasedUponOrientation;
                    if (compassHeading > 359) {
                    	compassHeading -= 360;
                    }
                    else if (compassHeading < 0) {
                    	compassHeading += 360;
                    }
                }
                
                if (currentlyNavigating) {
	                // Get the bearing on the destination location
	                bearingToRemoteLocation = currentLocation.bearingTo(destination);
	                // The bearing will be between -180 and 180, correct them to be between 0 and 360
	                if (bearingToRemoteLocation < 0) {
	                	bearingToRemoteLocation += 360;
	                }
	                float distance = currentLocation.distanceTo(destination);
	                float deltaDistance = Math.abs(distanceToRemoteLocation - distance);
	
	            	if (destinationReset || deltaDistance > 10) {
	            		distanceToRemoteLocation = distance;
	                	// Avoid meaningless text formatting
	            		boolean setText = false;
	            		if (distance > 8000) {
	            			if (deltaDistance > 100) {
	            				setText = true;
	            			}
	            		}
	            		else if (distance > 1000) {
	            			if (deltaDistance > 40) {
	            				setText = true;
	            			}
	            		}
	            		else {
	            			if (deltaDistance > 10) {
	            				setText = true;
	            			}
	            		}
	
	            		if (troubleshootMode && currentTime > nextTimeToWriteText) {
	            			StringBuilder sb = new StringBuilder();
	            			sb.append("Or:");
	            			sb.append(currentOrientationValue);
	            			sb.append("Delta:");
	            			sb.append(deltaBasedUponOrientation);
	            			sb.append("Head:");
	            			sb.append(compassHeading);
	            	        troubleshootOverlay.setText(sb.toString());
	            	        nextTimeToWriteText = currentTime + 1000;
	            		}
	
	            		if (setText) {
	            			navigateToOverlay.setText(distanceToRemoteLocation);
	            		}
	            	}
	            	
	                // Correct the heading to point to the destination
	                navigationHeading = bearingToRemoteLocation - compassHeading;
	
	                // The navigation heading will now be between -180 and 180, correct them to be between 0 and 360
	                if (navigationHeading < 0) {
	                	navigationHeading += 360;
	                }
                }

                destinationReset = false;
                mapView.postInvalidate(); // ??? Don't know if this is needed!!!
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private class FixedMyLocationOverlay extends MyLocationOverlay {
    	private boolean bugged = false;

    	private Paint accuracyPaint;
    	private Point center;
    	private Point left;
    	private Drawable drawable;
    	private int width;
    	private int height;

    	public FixedMyLocationOverlay(Context context, MapView mapView) {
    		super(context, mapView);
    	}

//    	@Override
//		protected boolean dispatchTap() {
//			return super.dispatchTap();
//		}

		@Override
		public synchronized void onLocationChanged(Location location) {
			super.onLocationChanged(location);
			currentLocation = location;
		}

		@Override
    	protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLoc, long when) {
    		if (!bugged) {
    			try {
    				super.drawMyLocation(canvas, mapView, lastFix, myLoc, when);
    			} catch (Exception e) {
    				bugged = true;
    			}
    		}

    		if (bugged) {
    			if (drawable == null) {
    				accuracyPaint = new Paint();
    				accuracyPaint.setAntiAlias(true);
    				accuracyPaint.setStrokeWidth(2.0f);
    				
    				drawable = mapView.getContext().getResources().getDrawable(R.drawable.small_dot);
    				width = drawable.getIntrinsicWidth();
    				height = drawable.getIntrinsicHeight();
    				center = new Point();
    				left = new Point();
    			}
    			Projection projection = mapView.getProjection();
    			
    			double latitude = lastFix.getLatitude();
    			double longitude = lastFix.getLongitude();
    			float accuracy = lastFix.getAccuracy();
    			
    			float[] result = new float[1];

    			Location.distanceBetween(latitude, longitude, latitude, longitude + 1, result);
    			float longitudeLineDistance = result[0];

    			GeoPoint leftGeo = new GeoPoint((int)(latitude*1e6), (int)((longitude-accuracy/longitudeLineDistance)*1e6));
    			projection.toPixels(leftGeo, left);
    			projection.toPixels(myLoc, center);
    			int radius = center.x - left.x;
    			
    			accuracyPaint.setColor(0xff6666ff);
    			accuracyPaint.setStyle(Style.STROKE);
    			canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

    			accuracyPaint.setColor(0x186666ff);
    			accuracyPaint.setStyle(Style.FILL);
    			canvas.drawCircle(center.x, center.y, radius, accuracyPaint);
    						
    			drawable.setBounds(center.x - width / 2, center.y - height / 2, center.x + width / 2, center.y + height / 2);
    			drawable.draw(canvas);
    		}
    	}

    }

	/**
	 * Draw an overlay on the map
	 */
    private class TroubleshootOverlay extends Overlay implements LongpressListener {

    	private static final int SCALE_MIN = 1;
    	private static final int SCALE_MAX = 20;

    	private int m_overlayHeight = 30;
    	private int m_overlayWidth = 0;
    	private boolean m_visible = true;

    	private Paint   mPaint = new Paint();
        private float cx = -1;
        private float cy = -1;
        private boolean initialPress = false;
        private boolean touchingMe = false;
        private boolean checkIfStillTouchingMe = false;
        private String text = null;
        private boolean initialized = false;

        private PressDurationTimer pressDurationTimer = new PressDurationTimer(this);

        public void setText(String text) {
        	this.text = text;
        	if (text != null) {
	            Paint paint = mPaint;
	        	Rect bounds = new Rect(0, 0, 0, 0);
	            paint.setTextSize(30);
	        	paint.getTextBounds(text, 0, text.length(), bounds);
	        	m_overlayWidth = bounds.width() + 10;
	        	m_overlayHeight = bounds.height() + 15;
	        	if (m_overlayWidth < 50) {
	        		m_overlayWidth = 50;
	        	}
	        	if (m_overlayHeight < 30) {
	        		m_overlayHeight = 30;
	        	}
        	}
        	else {
        		m_overlayHeight = 30;
        		m_overlayWidth = 200;
        	}
        }

         /**
         * @param marker the push-pin
         */
        public TroubleshootOverlay(boolean visible) {
            super();
            m_visible = visible;
        	setText("This is a test!");
        }

        private void initialize(int width, int height) {
        	
        	cx = 30;
        	cy = 150;
          
        	initialized = true;
        }

        public boolean isVisible() {
			return m_visible;
		}

		public void setVisible(boolean visible) {
			m_visible = visible;
			if (m_visible) {
				resume();
			}
			else {
				pause();
			}
	        mapView.postInvalidate();
		}

		public void resume() {
        	initialPress = false;
        	touchingMe = false;
        	checkIfStillTouchingMe = false;
        }

        public void pause() {
        	if (initialPress) {
        		initialPress = false;
        		mLongpressHandler.removeCallbacks(pressDurationTimer);
        	}
        	touchingMe = false;
        	checkIfStillTouchingMe = false;
        }

        @Override
		public boolean onTouchEvent(MotionEvent event, MapView view) {
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
            	if (Math.abs(event.getX() - cx) <= m_overlayWidth &&
            			Math.abs(event.getY() - cy) <= m_overlayHeight) {
            	    mLongpressHandler.removeCallbacks(pressDurationTimer);
            	    mLongpressHandler.postDelayed(pressDurationTimer, LONG_PRESS_DURATION);
            	    touchingMe = true;
            	}
            	else {
            		touchingMe = false;
            		checkIfStillTouchingMe = false;
            	}
            	initialPress = touchingMe;
                return touchingMe;
            }
            else if (MotionEvent.ACTION_UP == event.getAction()) {
            	// Happens after the moves are done, if there were any, so reset the flags
            	if (initialPress) {
            		initialPress = false;
            		mLongpressHandler.removeCallbacks(pressDurationTimer);
            	}
            	touchingMe = false;
            	checkIfStillTouchingMe = false;
            	return false;
            }

            if (MotionEvent.ACTION_MOVE != event.getAction() || !touchingMe || initialPress) {
                return false;
            }

            if (checkIfStillTouchingMe) {
            	// Only happens the first move event after the long press timer went off to make sure they are still pressing this overlay
            	checkIfStillTouchingMe = false;            	
            	if (!(Math.abs(event.getX() - cx) <= m_overlayWidth &&
            			Math.abs(event.getY() - cy) <= m_overlayHeight)) {
            		touchingMe = false;
            		return false;
            	}
            }

            cx = event.getX();
            cy = event.getY();
            return true;
		}

		@Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {

            if (!m_visible) {
            	return;
            }
            
            Paint paint = mPaint;
        	if (!shadow) {
	            paint.setAntiAlias(true);

	            if (!initialized) {
	            	initialize(canvas.getWidth(), canvas.getHeight());
	            }

	            if (touchingMe) {
	            	if (initialPress) {
	            		// They are holding the icon but it is not yet moving
	    	            paint.setColor(Color.rgb(228, 212, 122)); // yellowish
	            	}
	            	else {
	            		// They are moving the icon
	    	            paint.setColor(Color.rgb(102, 0, 0)); // redish
	            	}
		            paint.setStyle(Paint.Style.FILL);
	                paint.setAlpha(100);
	                canvas.drawRect(cx - 10, cy - 10, cx + m_overlayWidth + 10, cy + m_overlayHeight + 10, paint);
	                paint.setAlpha(255);
	            }

	            paint.setColor(bMapShowing ? Color.BLACK : Color.WHITE);
	            paint.setStyle(Paint.Style.FILL);
                paint.setAlpha(100);
                canvas.drawRect(cx, cy, cx + m_overlayWidth, cy + m_overlayHeight, paint);

	            paint.setColor(bMapShowing ? Color.WHITE : Color.BLACK);
	            paint.setStyle(Paint.Style.STROKE);
	            float stroke = paint.getStrokeWidth();
	            paint.setStrokeWidth(2);

	            paint.setStrokeWidth(stroke);
	            canvas.drawText(text, cx  + 5, cy + 5 + paint.getTextSize(), paint);
            }
        }

		@Override
		public void longPress() {
			initialPress = false;
			checkIfStillTouchingMe = true;
			mapView.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING );
		}
	}
}

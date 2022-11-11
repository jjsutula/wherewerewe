package com.nono.wherewerewe;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.text.Html;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nono.data.MediaConst;
import com.nono.gui.CommentBundle;
import com.nono.gui.CommentDialog;
import com.nono.gui.IconListBundle;
import com.nono.gui.IconListDialog;
import com.nono.gui.MediaLibraryBundle;
import com.nono.gui.MediaLibraryDialog;
import com.nono.gui.MediaLibrarySelectRow;
import com.nono.gui.MenuListBundle;
import com.nono.gui.MenuListDialog;
import com.nono.gui.MenuListSelectRow;
import com.nono.gui.MultiSelectListBundle;
import com.nono.gui.MultiSelectListDialog;
import com.nono.gui.MultiSelectListReturnBundle;
import com.nono.gui.MultiSelectListReturnRow;
import com.nono.util.FileUtilities;
import com.nono.util.MediaMapper;
import com.nono.util.MediaUtilities;
import com.nono.util.StringUtilities;
import com.nono.wherewerewe.data.DbConst;
import com.nono.wherewerewe.data.IntentWrapper;
import com.nono.wherewerewe.data.PreferenceData;
import com.nono.wherewerewe.data.SettingsConst;
import com.nono.wherewerewe.data.table.SettingsTable;
import com.nono.wherewerewe.data.table.TripTable;
import com.nono.wherewerewe.data.table.WaypointTable;
import com.nono.wherewerewe.db.DbAdapter;
import com.nono.wherewerewe.db.SettingsTableDb;
import com.nono.wherewerewe.db.TripTableDb;
import com.nono.wherewerewe.db.WaypointTableDb;

public class WhereWereWe extends Activity {
	
	public static final String INTENT_WRAPPER = "IntentWrapper";

	public static final String PREFS_FILE = "WwwPrefsFile";

	public static final int BUTTON_GALLERY_LOCATION_NOTES = 1;
	public static final int BUTTON_GALLERY_MAP = 2;
	public static final int BUTTON_GALLERY_AUDIO = 3;
	public static final int BUTTON_GALLERY_CAMERA = 4;
	public static final int BUTTON_GALLERY_VIDEO = 5;
	public static final int BUTTON_GALLERY_LOCATION = 6;
	public static final int BUTTON_GALLERY_MEDIA_LIBRARY = 7;
	
	public static final int OPTION_LOCATION_REFRESH = 1;
	public static final int OPTION_LOCATION_DETAILS = 2;
	public static final int OPTION_LOCATION_MOVE = 3;
	public static final int OPTION_LOCATION_ADD = 4;
	public static final int OPTION_LOCATION_REMOVE = 5;
	public static final int OPTION_LOCATION_NOTES = 6;
	public static final int OPTION_LOCATION_NEW_HERE = 7;
	public static final int OPTION_DISPLAY_ONLY = 8;

	public static final int OPTION_TRIP_ADD = 11;
	public static final int OPTION_TRIP_DETAILS = 12;
	public static final int OPTION_TRIP_REMOVE = 13;
	public static final int OPTION_SWITCH_LOCATION = 14;
	public static final int OPTION_TRIP_NOTES = 15;
	public static final int OPTION_SWITCH_TRIP = 16;
	public static final int OPTION_START_TRAIL = 17;
	public static final int OPTION_STOP_TRAIL = 18;
	public static final int OPTION_CREATE_ALTITUDE_ALERT = 19;
	public static final int OPTION_BACKUP_DB = 31;
	public static final int OPTION_RESTORE_DB = 32;

	public static final int REQUEST_CODE_PREFERENCES = 1;
	public static final int REQUEST_CODE_TRIP_DETAIL = 2;
	public static final int REQUEST_CODE_LOCATION_DETAIL = 4;
	public static final int REQUEST_CODE_COMMENT = 7;
	public static final int REQUEST_CODE_MAIN_OPTIONS = 8;
	public static final int REQUEST_CODE_SWITCH_LOCATION = 9;
	public static final int REQUEST_CODE_LOCATION_MOVE = 10;
	public static final int REQUEST_CODE_SWITCH_TRIP = 11;
	public static final int REQUEST_CODE_LOCATION_REMOVE = 12;
	public static final int REQUEST_CODE_TRIP_REMOVE = 13;
	public static final int REQUEST_CODE_RECORD_AUDIO = 14;
	public static final int REQUEST_CODE_RECORD_VIDEO = 15;
	public static final int REQUEST_CODE_TAKE_PICTURE = 16;
	public static final int REQUEST_CODE_SHOW_MAP = 17;
	public static final int REQUEST_CODE_IMAGE_GALLERY = 18;
	public static final int REQUEST_CODE_MEDIA_LIBRARY = 19;
	public static final int REQUEST_CODE_IMPORT_MEDIA = 20;

	public static final long LOCATION_MIN_PERIOD_MILLIS = 1000 * 2;
	public static final long LOCATION_MIN_DISTANCE_METERS = 10;

	private final static double FEET_PER_METER = 3.28083989501d;
	private final static int PRECISION = 6;
	private final static short LATITUDE = 1;
	private final static short LONGITUDE = 2;
	
	private final static char DEGREES_SYMBOL = 176;
	private final static char MINUTES_SYMBOL = '\'';
	private final static char SECONDS_SYMBOL = '\"';
	private static final int HIGH_VIDEO_QUALITY = 1;

	private TripTableDb tripTableDb = null;
	private WaypointTableDb waypointTableDb = null;
	private DbAdapter dbAdapter = null;
    private PreferenceData preferenceData = new PreferenceData();
	private SettingsTableDb settingsTableDb = null;
    
    private TextView altitudeHeaderView;
    private TextView latitudeHeaderView;
    private TextView longitudeHeaderView;
    private TextView address1HeaderView;
    private TextView altitudeView;
    private TextView latitudeView;
    private TextView longitudeView;
    private TextView whenView;
    private TextView address1View;
    private TextView address2View;
    private TextView address3View;
    private TextView address4View;

    private Button metaButton;
    
	private DateFormat dateFormat;
	private DateFormat timeFormat;

    private SettingsTable currentTripSettings = null;
    private SettingsTable currentWaypointSettings = null;

    private TripTable currentTripTable = null;
    private WaypointTable currentWaypointTable = null;

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private boolean okToUpdateLocation = false;
    private int currentCommentType = OPTION_LOCATION_NOTES;
    
    private String currentTripFilePath = null;

    private String mediaPath = null;

    int[][] buttonGalleryImageIDs = {
            {R.drawable.comments, BUTTON_GALLERY_LOCATION_NOTES},
            {R.drawable.ic_launcher_map, BUTTON_GALLERY_MAP},
            {R.drawable.flag_big, BUTTON_GALLERY_LOCATION},
            {R.drawable.mic, BUTTON_GALLERY_AUDIO},
            {R.drawable.camera, BUTTON_GALLERY_CAMERA},
            {R.drawable.video, BUTTON_GALLERY_VIDEO},
            {R.drawable.media, BUTTON_GALLERY_MEDIA_LIBRARY}
    };

    private PendingIntent mAlertSender = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
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

        dateFormat = android.text.format.DateFormat.getLongDateFormat(getApplicationContext());
        timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());

        dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        tripTableDb = new TripTableDb(dbAdapter);
        waypointTableDb = new WaypointTableDb(dbAdapter);

        altitudeHeaderView = (TextView) findViewById(R.id.text_altitude_header);
        latitudeHeaderView = (TextView) findViewById(R.id.text_latitude_header);
        longitudeHeaderView = (TextView) findViewById(R.id.text_longitude_header);
        address1HeaderView = (TextView) findViewById(R.id.text_address1_header);
        
        altitudeView = (TextView) findViewById(R.id.text_altitude);
        latitudeView = (TextView) findViewById(R.id.text_latitude);
        longitudeView = (TextView) findViewById(R.id.text_longitude);
        whenView = (TextView) findViewById(R.id.text_when);
        address1View = (TextView) findViewById(R.id.text_address1);
        address2View = (TextView) findViewById(R.id.text_address2);
        address3View = (TextView) findViewById(R.id.text_address3);
        address4View = (TextView) findViewById(R.id.text_address4);
        
        ColorStateList colorStateList = address1View.getTextColors();

        metaButton = (Button) findViewById(R.id.meta_button);        
        metaButton.setBackgroundColor(getResources().getColor(android.R.color.black));
        metaButton.setTextColor(colorStateList);

        settingsTableDb = new SettingsTableDb(dbAdapter);

        Gallery gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setAdapter(new ImageAdapter(this));
        gallery.setSelection((buttonGalleryImageIDs.length)/2);
        gallery.setOnItemClickListener(new OnItemClickListener() 
        {
            public void onItemClick(AdapterView parent, 
            View v, int position, long id) 
            {
            	int option = buttonGalleryImageIDs[position][1];
            	parent.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING );

        		switch (option) {
        		case BUTTON_GALLERY_LOCATION_NOTES:
        			editComments(OPTION_LOCATION_NOTES);
        			break;
        		case BUTTON_GALLERY_MAP:
        			showMap();
        			break;
        		case BUTTON_GALLERY_LOCATION:
        			displayLocationMenu();
        			break;
        		case BUTTON_GALLERY_AUDIO:
        			recordAudio();
        			break;
        		case BUTTON_GALLERY_CAMERA:
        			takePicture();
        			break;
        		case BUTTON_GALLERY_VIDEO:
        			recordVideo();
        			break;
        		case BUTTON_GALLERY_MEDIA_LIBRARY:
        			launchMediaLibrary();
        			break;
        		}
           }
        });

        refreshMainScreen();
    }

    /**
     * Show the current data on the screen
     */
    private void refreshMainScreen() {
        currentTripSettings = settingsTableDb.fetchSetting(SettingsConst.CURRENT_TRIP);
        if (currentTripSettings.getSetting() == null) {
        	showLatestTrip();
        	return;
    	}
        currentTripFilePath = null;
		currentTripTable = tripTableDb.fetchTrip(currentTripSettings.getSettingLong());
		if (currentTripTable == null) {
        	showLatestTrip();
        	return;
		}

		// Trip verified, now check for locations
        currentWaypointSettings = settingsTableDb.fetchSetting(SettingsConst.CURRENT_WAYPOINT);
        if (currentWaypointSettings.getSetting() == null) {
        	showLatestLocation();
        	return;
    	}
		currentWaypointTable = waypointTableDb.fetchWaypoint(currentWaypointSettings.getSettingLong());
		if (currentWaypointTable == null) {
        	showLatestLocation();
        	return;
		}

		showLocationInfo();
    }

    private String formatDatetime(long date) {
    	
    	Date displayDate;
    	if (date == 0) {
    		displayDate = new Date();
    	}
    	else {
    		displayDate = new Date(date);
    	}

    	StringBuilder sb = new StringBuilder();
    	return sb.append(dateFormat.format(displayDate)).append(" ").append(timeFormat.format(displayDate)).toString();
    }

    private void showLocationInfo() {
        metaButton.setText(Html.fromHtml(formatMetaInfo(currentTripTable.getName(), currentWaypointTable.getName(), currentTripTable.getStartDate())));

    	if (currentWaypointTable.getAltitude() <= 0 && currentWaypointTable.getLatitude() == -1.0 && currentWaypointTable.getLongitude() == -1.0) {
    		// No location info set, so blank out address info
            altitudeHeaderView.setText(null);
            latitudeHeaderView.setText(null);
            longitudeHeaderView.setText(null);
            address1HeaderView.setText(null);
            altitudeView.setText(null);
            latitudeView.setText(null);
            longitudeView.setText(null);
            whenView.setText(null);
            address1View.setText(null);
            address2View.setText(null);
            address3View.setText(null);
            address4View.setText(null);
    	}
    	else {
            altitudeHeaderView.setText(getResources().getString(R.string.altitude) + ":");
            latitudeHeaderView.setText(getResources().getString(R.string.latitude) + ":");
            longitudeHeaderView.setText(getResources().getString(R.string.longitude) + ":");
            address1HeaderView.setText(getResources().getString(R.string.address) + ":");

            altitudeView.setText(formatAltitude(currentWaypointTable.getAltitude()));
	        latitudeView.setText(formatCoordinate(currentWaypointTable.getLatitude(), LATITUDE));
	        longitudeView.setText(formatCoordinate(currentWaypointTable.getLongitude(), LONGITUDE));
            whenView.setText(formatDatetime(currentWaypointTable.getWhen()));
	
	        String[] displayLines = new String[4];
	        displayLines = formatAddressText(displayLines, currentWaypointTable.getAddress());
	        address1View.setText(displayLines[0]);
	        address2View.setText(displayLines[1]);
	        address3View.setText(displayLines[2]);
	        address4View.setText(displayLines[3]);
    	}
    }

    private String formatMetaInfo(String trip, String location, long date) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<strong>");
    	sb.append(getResources().getString(R.string.location));
    	sb.append(":</strong> ");
    	sb.append(location);
    	sb.append("<br/>");
    	sb.append("<strong>");
    	sb.append(getResources().getString(R.string.trip));
    	sb.append(":</strong> ");
    	sb.append(trip);
//    	sb.append("<br/>");
//    	sb.append(formatDate(date));

    	return sb.toString();
    }

    /**
     * Format the altitude.
     * @param altitude The altitude.
     * @return The altitude, fit for display.
     */
    private String formatAltitude(double altitude) {
    	
    	if (altitude == -1.0) {
    		return null;
    	}

    	StringBuilder sb = new StringBuilder();
    	// Altitude is always stored in the database in meters
    	if (preferenceData.isMeasurementMetric()) {
    		sb.append(Math.round(altitude));
    		sb.append(" m");
    	}
    	else {
    		altitude = altitude * FEET_PER_METER;
    		sb.append(Math.round(altitude));
    		sb.append(" ft");
    	}

    	return sb.toString();
    }

    /**
     * Format the latitude or longitude.
     * @param coordinate The latitude or longitude to format.
     * @param type Whether it's latitude or longitude.
     * @return The latitude or longitude, fit for display.
     */
    private String formatCoordinate(double coordinate, short type) {

    	if (coordinate == -1.0) {
    		return null;
    	}

    	int sign;

    	if (coordinate < 0) {
    		sign = -1;
    		// strip the sign
    		coordinate *= sign;
    	}
    	else {
    		sign = 1;
    	}

    	StringBuilder sb = new StringBuilder();
    	if (preferenceData.isCoordinateDecimalDegrees()) {
    		sb.append(StringUtilities.formatData(coordinate, PRECISION));
    	}
    	else {
    		int degrees = (int)coordinate;
    		sb.append(degrees);
    		sb.append(DEGREES_SYMBOL);
    		sb.append(" ");
    		coordinate -= degrees;
    		
    		coordinate *= 60;
    		int minutes = (int)coordinate;
    		sb.append(minutes);
    		sb.append(MINUTES_SYMBOL);
    		coordinate -= minutes;
    		
    		coordinate *= 60;
    		long seconds = Math.round(coordinate);
    		sb.append(seconds);
    		sb.append(SECONDS_SYMBOL);
    	}

		sb.append(" ");
		if (type == LATITUDE) {
			if (sign < 0) {
				sb.append("S");
			}
			else {
				sb.append("N");				
			}
		}
		else {
			if (sign < 0) {
				sb.append("W");
			}
			else {
				sb.append("E");				
			}
		}

    	return sb.toString();
    }

    /**
     * Format the address lines.
     * @param displayLines The returned formatted address lines.
     * @param address The address to format.
     * @return An array of address lines.
     */
    private String[] formatAddressText(String[] displayLines, String address) {
    	if (address == null) {
    		address = "";
    	}

    	String[] lines = address.split("[\n]");
		for (int ndx = 0; ndx < displayLines.length; ndx++) {
			if (ndx < lines.length) {
				// As long as there is an address line to move, move it.
				displayLines[ndx] = lines[ndx];
			}
			if (ndx == displayLines.length - 1 && lines.length > displayLines.length) {
				// If we've reached the final display line and there are still more line to move,
				// concatenate all the rest of the lines at the end.
				for (int ndx1 = ndx + 1; ndx1 < lines.length; ndx1++) {
					displayLines[ndx] += lines[ndx1];
				}
			}
		}
		
		return displayLines;
    }

    private void importMedia() {
		IntentWrapper wrapper = new IntentWrapper();
		wrapper.setWaypointTable(currentWaypointTable);
		wrapper.setPreferenceData(preferenceData);
    	
		Intent importMediaIntent = new Intent(Intent.ACTION_VIEW).setClass(this, MediaImportDialog.class);
	    Bundle extras = new Bundle();
	    extras.putSerializable(WhereWereWe.INTENT_WRAPPER, wrapper);
	    importMediaIntent.putExtras(extras);

	    // Make it a subactivity so we know when it returns
	    startActivityForResult(importMediaIntent, REQUEST_CODE_IMPORT_MEDIA);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

    	// Inflate the currently selected menu XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.preferences:
            // When the Help button is clicked, launch Preferences as a sub-activity
            Intent launchPreferencesIntent = new Intent().setClass(this, Preferences.class);
            
            // Make it a subactivity so we know when it returns
            startActivityForResult(launchPreferencesIntent, REQUEST_CODE_PREFERENCES);
            break;
    	case R.id.upload:
    		importMedia();
            return true;
        case R.id.more:
			showMainOptions();
            break;

        }
        return true;
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_PREFERENCES:
	        	onPreferencesResult(requestCode, resultCode, data);
				break;
			case REQUEST_CODE_COMMENT:
	        	onCommentResult(requestCode, resultCode, data);
				break;
			case REQUEST_CODE_LOCATION_DETAIL:
        		onLocationDetailResult(requestCode, resultCode, data);
				break;
			case REQUEST_CODE_MAIN_OPTIONS:
	        	if (data != null) {
	        		onMainOptionsResult(requestCode, resultCode, data);
	        	}
				break;
			case REQUEST_CODE_TRIP_DETAIL:
        		onTripDetailResult(requestCode, resultCode, data);
				break;
			case REQUEST_CODE_SWITCH_LOCATION:
	        	if (data != null) {
	        		onSwitchLocationResult(requestCode, resultCode, data);
	        	}
				break;
			case REQUEST_CODE_LOCATION_MOVE:
	        	if (data != null) {
	        		onLocationMoveResult(requestCode, resultCode, data);
	        	}
				break;
			case REQUEST_CODE_SWITCH_TRIP:
	        	if (data != null) {
	        		onSwitchTripResult(requestCode, resultCode, data);
	        	}
				break;
			case REQUEST_CODE_LOCATION_REMOVE:
	        	if (data != null) {
	        		onLocationRemoveResult(requestCode, resultCode, data);
	        	}
				break;
			case REQUEST_CODE_TRIP_REMOVE:
	        	if (data != null) {
	        		onTripRemoveResult(requestCode, resultCode, data);
	        	}
				break;
			case REQUEST_CODE_RECORD_AUDIO:
	        	if (data != null) {
	        		onRecordAudioResult(requestCode, resultCode, data);
	        	}
	        	else {
	        		Toast.makeText(this, "Audio data was null!", Toast.LENGTH_SHORT).show();
	        	}
				break;
			case REQUEST_CODE_RECORD_VIDEO:
        		onRecordVideoResult(requestCode, resultCode, data);
				break;
			case REQUEST_CODE_TAKE_PICTURE:
        		onCameraResult(requestCode, resultCode, data);
				break;
			case REQUEST_CODE_IMAGE_GALLERY:
        		onImageGalleryResult(requestCode, resultCode, data);
				break;
			case REQUEST_CODE_MEDIA_LIBRARY:
        		onMediaLibraryResult(requestCode, resultCode, data);
				break;
			}
		}
		else {
			switch (requestCode) {
			case REQUEST_CODE_RECORD_VIDEO:
        		onRecordVideoResult(requestCode, resultCode, data);
				break;
			case REQUEST_CODE_TAKE_PICTURE:
				onCameraResult(requestCode, resultCode, data);
				break;
			}
		}
	}
    
    /**
     * Process the result of the Preferences activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onPreferencesResult(int requestCode, int resultCode, Intent data) {
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

        showLocationInfo();
    }

	@Override
	protected void onDestroy() {
		if (dbAdapter != null) {
			dbAdapter.close();
		}
		super.onDestroy();
	}
    
    @Override
    protected void onStop(){
    	
    	if (locationManager != null && locationListener != null) {
    		locationManager.removeUpdates(locationListener);
    		locationListener = null;
    		locationManager = null;
    	}

    	// Save user preferences using an Editor object
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Preferences.KEY_PREFERENCE_MEASUREMENT, preferenceData.getMeasurement());
        editor.putString(Preferences.KEY_PREFERENCE_COORDINATE, preferenceData.getCoordinate());
        editor.putString(Preferences.KEY_PREFERENCE_COMPASS_VISIBLE, preferenceData.isCompassVisible() ? "true" : "false");
        editor.putString(Preferences.KEY_PREFERENCE_NAVIGATOR_VISIBLE, preferenceData.isNavigatorVisible() ? "true" : "false");
        editor.putString(Preferences.KEY_PREFERENCE_COMPASS_SIZE, Integer.toString(preferenceData.getCompassSize()));
        editor.putString(Preferences.KEY_PREFERENCE_NAVIGATOR_SIZE, Integer.toString(preferenceData.getNavigatorSize()));

        // Commit the edits
        editor.commit();

        writeLog("Calling onStop()");
        super.onStop();
    }

	@Override
	protected void onRestart() {
		super.onRestart();
        writeLog("Calling onRestart()");
	}

	@Override
	protected void onStart() {
		super.onStart();
        writeLog("Calling onStart()");
	}

	@Override
	public void onPause() {
        writeLog("Calling onPause()");
		super.onPause();
	}

	@Override
    protected void onResume() {
        super.onResume();
        writeLog("Calling onResume()");

    }


	public void buttonClickHandler(View view) {

    	view.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING );

		switch (view.getId()) {
		case R.id.location_button:
			showMainOptions();
			break;
		}
	}

    private void launchMediaLibrary() {
    	Intent launchIntent = new Intent().setClass(this, MediaLibraryDialog.class);
	   	Bundle extras = new Bundle();
	   	MediaLibraryBundle bundle = new MediaLibraryBundle(getString(R.string.media_library),null, 10);
	   	
		MediaMapper mediaMapper = new MediaMapper();
		buildMediaLibraryByType(bundle, MediaConst.TYPE_IMAGE, mediaMapper);
		buildMediaLibraryByType(bundle, MediaConst.TYPE_VIDEO, mediaMapper);
		buildMediaLibraryByType(bundle, MediaConst.TYPE_AUDIO, mediaMapper);

	   	extras.putSerializable(MediaLibraryDialog.BUNDLE_MEDIA_LIBRARY_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, REQUEST_CODE_MEDIA_LIBRARY);
    	

//    	try
//        {
//	        Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
//			StringBuilder sb = new StringBuilder();
//			sb.append(getCurrentTripFilePath());
//			sb.append(FileUtilities.IMAGE_FOLDER);
//			sb.append("/logon.png");
//	        File file = new File(sb.toString()); 
//	        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
//	        String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//	        myIntent.setDataAndType(Uri.fromFile(file),mimetype);
//	        startActivity(myIntent);
//            System.out.println("Finished.");
//        }
//        catch (Exception e) 
//        {
//            String data = e.getMessage();
//            System.out.println(data);
//        }
    }

	private void buildMediaLibraryByType(MediaLibraryBundle bundle, int type, MediaMapper mediaMapper) {
		StringBuilder sb = new StringBuilder();
    	String thumbnailPath = null;
    	String name;
    	String path;
    	String mediaDirPath;
    	ArrayList<MediaLibrarySelectRow> rowList = new ArrayList<MediaLibrarySelectRow>();
		sb.append(getCurrentTripFilePath());
		switch (type) {
			case (MediaConst.TYPE_AUDIO):
				sb.append(FileUtilities.AUDIO_FOLDER);
				break;
			case (MediaConst.TYPE_IMAGE):
				sb.append(FileUtilities.IMAGE_FOLDER);
				break;
			case (MediaConst.TYPE_VIDEO):
				sb.append(FileUtilities.VIDEO_FOLDER);
				break;
		}
		mediaDirPath = sb.toString();
		File mediaDir = new File(mediaDirPath);
		if (mediaDir.exists()) {
	    	MediaLibrarySelectRow row;
			File[] files= mediaDir.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					name = file.getName();
					sb.setLength(0);
					sb.append(mediaDirPath);
					sb.append('/');
					sb.append(name);
					path = sb.toString();
					type = mediaMapper.getType(name);
					row = null;
					switch (type) {
					case (MediaConst.TYPE_AUDIO):
						row = new MediaLibrarySelectRow(name, R.drawable.audio_small, path, type);
						break;
					case (MediaConst.TYPE_IMAGE):
		    			thumbnailPath = MediaUtilities.getThumbnailPath(path);
		    			if (thumbnailPath == null) {
		    				row = new MediaLibrarySelectRow(name, R.drawable.camera_small, path, type);
		    				bundle.addImagePathNeedingThumbnail(path);
		    			}
		    			else {
		    				row = new MediaLibrarySelectRow(name, thumbnailPath, path, type);
		    			}
						break;
					case (MediaConst.TYPE_VIDEO):
						row = new MediaLibrarySelectRow(name, R.drawable.video_small, path, type);
						break;
					}
					if (row != null) {
						rowList.add(row);
					}
				}
			}
		}

		Collections.sort(rowList);
		for (MediaLibrarySelectRow row : rowList) {
			bundle.addMenuItem(row);
		}
	}

    /**
     * Invoked when the location move button is pressed.
     */
    private void displayLocationMenu() {

    	Intent launchIntent = new Intent().setClass(this, IconListDialog.class);
	   	Bundle extras = new Bundle();
	   	IconListBundle bundle = new IconListBundle(getString(R.string.location_options), getString(R.string.cancel));

	   	bundle.add(getString(R.string.this_is_new_location), R.drawable.flag_big, Integer.toString(OPTION_LOCATION_NEW_HERE));
	   	bundle.add(getString(R.string.refresh_current_location), R.drawable.reload_48x48, Integer.toString(OPTION_LOCATION_REFRESH));
	   	bundle.add(getString(R.string.start_trail), R.drawable.trail_start, Integer.toString(OPTION_START_TRAIL));
	   	bundle.add(getString(R.string.stop_trail), R.drawable.trail_stop, Integer.toString(OPTION_STOP_TRAIL));
	   	bundle.add(getString(R.string.create_altitude_alert), R.drawable.alert, Integer.toString(OPTION_CREATE_ALTITUDE_ALERT));
	   	 
	   	extras.putSerializable(IconListDialog.BUNDLE_ICON_LIST_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns. We will pretend this is the main options menu 
        startActivityForResult(launchIntent, REQUEST_CODE_MAIN_OPTIONS);
    }

	private void recordAudio() {
		Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
		startActivityForResult(intent, REQUEST_CODE_RECORD_AUDIO);
	}

	private void recordVideo() {
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, HIGH_VIDEO_QUALITY);

		mediaPath = generateUniqueFilepath(FileUtilities.LOCATION_VIDEO);
		writeLog("recordVideo() mediaPath='" + mediaPath + "'");
		File file = new File(mediaPath);
		try {
			String lastMediaPath = file.getCanonicalPath();
			// Clear the setting for the media store to ensure there is only one entry after this operation
	        settingsTableDb.removeSetting(SettingsConst.LAST_MEDIA_STORE_LOCATION);
	        settingsTableDb.createSettings(SettingsConst.LAST_MEDIA_STORE_LOCATION, lastMediaPath);
			writeLog("recordVideo() lastMediaPath='" + lastMediaPath + "'");
		} catch (IOException e) {
	        settingsTableDb.removeSetting(SettingsConst.LAST_MEDIA_STORE_LOCATION);
			writeLog("recordVideo() lastMediaPath excption = " + e);
			Log.e(WhereWereWe.class.getName(), "IOException occurred while attempting to save the video path " + mediaPath, e);
		}
        ContentValues content = new ContentValues(); 
		StringBuilder sb = new StringBuilder();
		sb.append(currentWaypointTable.getName());
        content.put(Video.Media.TITLE, sb.toString()); 
        content.put(Video.Media.DATE_TAKEN, System.currentTimeMillis() / 1000);
        content.put(Video.Media.BUCKET_ID, mediaPath.hashCode());
        content.put(Video.Media.BUCKET_DISPLAY_NAME,"WhereWereWe");
        content.put(Video.Media.MIME_TYPE, "video/3gpp");
        content.put(Video.Media.DESCRIPTION, "WhereWereWe video image");
        content.put(Video.Media.DATA, mediaPath);
        Uri outputFileUri = getContentResolver().insert( Video.Media.EXTERNAL_CONTENT_URI , content);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO);
	}

	private void takePicture() {

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mediaPath = generateUniqueFilepath(FileUtilities.LOCATION_IMAGE);
		writeLog("takePicture() mediaPath='" + mediaPath + "'");
		File file = new File(mediaPath);
		try {
			String lastMediaPath = file.getCanonicalPath();
			// Clear the setting for the media store to ensure there is only one entry after this operation
	        settingsTableDb.removeSetting(SettingsConst.LAST_MEDIA_STORE_LOCATION);
	        settingsTableDb.createSettings(SettingsConst.LAST_MEDIA_STORE_LOCATION, lastMediaPath);
			writeLog("takePicture() lastMediaPath='" + lastMediaPath + "'");
		} catch (IOException e) {
	        settingsTableDb.removeSetting(SettingsConst.LAST_MEDIA_STORE_LOCATION);
			writeLog("takePicture() lastMediaPath exception = " + e);
			Log.e(WhereWereWe.class.getName(), "IOException occurred while attempting to save the image path " + mediaPath, e);
		}
        ContentValues content = new ContentValues(); 
		StringBuilder sb = new StringBuilder();
		sb.append(currentWaypointTable.getName());
        content.put(Images.Media.TITLE, sb.toString()); 
        content.put(Images.Media.BUCKET_ID, mediaPath.hashCode());
        content.put(Images.Media.BUCKET_DISPLAY_NAME,"WhereWereWe");
        content.put(Images.Media.DATE_TAKEN, System.currentTimeMillis() / 1000);
        content.put(Images.Media.MIME_TYPE, "image/jpeg");
        content.put(Images.Media.DESCRIPTION, "WhereWereWe camera image");
        content.put(Images.Media.DATA, mediaPath);
        Uri outputFileUri = getContentResolver().insert( Images.Media.EXTERNAL_CONTENT_URI , content);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
	}

	private String generateUniqueFilepath(int type) {
        String destPath = FileUtilities.getFilePath(type, getCurrentTripFilePath(), generateUniqueFilename(type));

        // Make sure the folder exists
		int ndx = destPath.lastIndexOf('/');
		if (ndx > 0) {
			String dirpath = destPath.substring(0, ndx);
			FileUtilities.createDirIfNotExists(dirpath);
		}

		return destPath;
	}

	private String generateUniqueFilename(int type) {
        StringBuilder sb = new StringBuilder();

    	String prefix;
		String suffix;
		switch(type) {
		case FileUtilities.LOCATION_AUDIO:
			prefix = MediaConst.FILE_PREFIX_AUDIO;
			suffix = MediaConst.FILE_SUFFIX_AUDIO;
			break;
		case FileUtilities.LOCATION_IMAGE:
			prefix = MediaConst.FILE_PREFIX_IMAGE;
			suffix = MediaConst.FILE_SUFFIX_IMAGE;
			break;
		case FileUtilities.LOCATION_VIDEO:
			prefix = MediaConst.FILE_PREFIX_VIDEO;
			suffix = MediaConst.FILE_SUFFIX_VIDEO;
			break;
		default:
			prefix = MediaConst.FILE_PREFIX_AUDIO;
			suffix = "";
			break;
		}

        sb.setLength(0);
        sb.append(prefix);
		sb.append(getUniqueString());
		sb.append(suffix);

		return sb.toString();
	}

	private String getUniqueString() {
		Calendar now = Calendar.getInstance();
        StringBuilder sb = new StringBuilder();
        // yyyymmdd_hhmmss
        int val = now.get(Calendar.YEAR);
		sb.append(val);
        val = now.get(Calendar.MONTH) + 1;
        if (val < 10) 
    		sb.append('0');
		sb.append(val);
        val = now.get(Calendar.DAY_OF_MONTH);
        if (val < 10) 
    		sb.append('0');
		sb.append(val);
		sb.append('_');
        val = now.get(Calendar.HOUR_OF_DAY);
        if (val < 10) 
    		sb.append('0');
		sb.append(val);
        val = now.get(Calendar.MINUTE);
        if (val < 10) 
    		sb.append('0');
		sb.append(val);
        val = now.get(Calendar.SECOND);
        if (val < 10) 
    		sb.append('0');
		sb.append(val);
		
		return sb.toString();
	}

    /**
     * Invoked when the comments button is pressed.
     */
    private void editComments(int type) {

    	String title = null;
    	String initialComment = null;
    	
    	Intent launchIntent = new Intent().setClass(this, CommentDialog.class);
	   	Bundle extras = new Bundle();
	   	
	   	currentCommentType = type;
	   	switch(type){
	   	case OPTION_LOCATION_NOTES:
	   		title = getString(R.string.location_notes);
	   		initialComment = currentWaypointTable.getComment();
	   		break;
	   	case OPTION_TRIP_NOTES:
	   		title = getString(R.string.trip_notes);
	   		initialComment = currentTripTable.getComment();
	   		break;
	   		
//	   		OPTION_DISPLAY_ONLY
	   	}
	   	CommentBundle bundle = new CommentBundle(title, initialComment, 12);

	   	extras.putSerializable(CommentDialog.BUNDLE_COMMENT_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, REQUEST_CODE_COMMENT);
    }

    /**
     * Process the result of the comment activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onCommentResult(int requestCode, int resultCode, Intent data) {
    	String comment = null;
    	if (data != null) {
    		Bundle extras = data.getExtras();
        	comment = (String)extras.getSerializable(CommentDialog.BUNDLE_COMMENT_DIALOG_RETURN);
    	}
    	switch(currentCommentType) {
    	case OPTION_LOCATION_NOTES:
            currentWaypointTable.setComment(comment);
            waypointTableDb.updateWaypoint(currentWaypointTable);
            break;
    	case OPTION_TRIP_NOTES:
            currentTripTable.setComment(comment);
            tripTableDb.updateTrip(currentTripTable);
            break;
    	}
    }

    private void showMap() {
		IntentWrapper wrapper = new IntentWrapper();
		wrapper.setWaypointTable(currentWaypointTable);
		wrapper.setPreferenceData(preferenceData);
    	
		Intent launchMapIntent = new Intent(Intent.ACTION_VIEW).setClass(this, MapDetail.class);
	    Bundle extras = new Bundle();
	    extras.putSerializable(WhereWereWe.INTENT_WRAPPER, wrapper);
	    launchMapIntent.putExtras(extras);

	    // Make it a subactivity so we know when it returns
	    startActivityForResult(launchMapIntent, REQUEST_CODE_SHOW_MAP);
    }

    /**
     * Invoked when the location options button is pressed.
     */
    private void showMainOptions() {

    	Intent launchIntent = new Intent().setClass(this, MenuListDialog.class);
	   	Bundle extras = new Bundle();
	   	MenuListBundle bundle = new MenuListBundle(getString(R.string.options),getString(R.string.cancel), 7);
	   	
	   	MenuListSelectRow row;
	   	row = new MenuListSelectRow(getString(R.string.location), 8);
	   	row.add(getString(R.string.new_item), R.drawable.flag, Integer.toString(OPTION_LOCATION_NEW_HERE));
	   	row.add(getString(R.string.refresh_position), R.drawable.reload_48x48, Integer.toString(OPTION_LOCATION_REFRESH));
	   	row.add(getString(R.string.details), R.drawable.details_loc, Integer.toString(OPTION_LOCATION_DETAILS));
	   	row.add(getString(R.string.notes), R.drawable.comments, Integer.toString(OPTION_LOCATION_NOTES));
	   	row.add(getString(R.string.add_remote), R.drawable.add_loc, Integer.toString(OPTION_LOCATION_ADD));
	   	row.add(getString(R.string.open_other), R.drawable.switch_loc, Integer.toString(OPTION_SWITCH_LOCATION));
	   	row.add(getString(R.string.swap_trip ), R.drawable.move_48x48, Integer.toString(OPTION_LOCATION_MOVE));
	   	row.add(getString(R.string.remove_locations), R.drawable.delete_loc, Integer.toString(OPTION_LOCATION_REMOVE));
	   	row.toggleExpanded();
	   	bundle.addMenuItem(row);
	   		   	
	   	row = new MenuListSelectRow(getString(R.string.trip), 5);
	   	row.add(getString(R.string.new_item), R.drawable.add_trip, Integer.toString(OPTION_TRIP_ADD));
	   	row.add(getString(R.string.details), R.drawable.details_trip, Integer.toString(OPTION_TRIP_DETAILS));
	   	row.add(getString(R.string.notes), R.drawable.comments, Integer.toString(OPTION_TRIP_NOTES));
	   	row.add(getString(R.string.open_other), R.drawable.switch_trip, Integer.toString(OPTION_SWITCH_TRIP));
	   	row.add(getString(R.string.remove_trips), R.drawable.delete_trip, Integer.toString(OPTION_TRIP_REMOVE));
	   	bundle.addMenuItem(row);
	   	
	   	
	   	row = new MenuListSelectRow(getString(R.string.maintain), 2);
	   	row.add(getString(R.string.backup_db), R.drawable.disk_48x48, Integer.toString(OPTION_BACKUP_DB));
	   	row.add(getString(R.string.restore_db), R.drawable.undo_48x48, Integer.toString(OPTION_RESTORE_DB));
	   	bundle.addMenuItem(row);

	   	extras.putSerializable(MenuListDialog.BUNDLE_MENU_LIST_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, REQUEST_CODE_MAIN_OPTIONS);
    }

    /**
     * Process the result of the main options activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onMediaLibraryResult(int requestCode, int resultCode, Intent data) {
    	// Nothing to do
    }

    /**
     * Process the result of the main options activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onMainOptionsResult(int requestCode, int resultCode, Intent data) {

        Bundle extras = data.getExtras();
        String selectedStr = (String)extras.getSerializable(MenuListDialog.BUNDLE_MENU_LIST_RETURN);
        if (selectedStr != null) {
        	try {
				int selected = Integer.parseInt(selectedStr);
				switch(selected) {
				case OPTION_LOCATION_REFRESH:
	            	updateCurrentLocation();
					break;
				case OPTION_LOCATION_NEW_HERE:
					createWaypoint();
			        showLocationInfo();
	            	updateCurrentLocation();
					break;
				case OPTION_LOCATION_ADD:
					createWaypoint();
			        showLocationInfo();
					launchLocationDetail();
					break;
				case OPTION_TRIP_ADD:
					createTrip();
					launchTripDetail();
					break;
				case OPTION_LOCATION_DETAILS:
					launchLocationDetail();
					break;
				case OPTION_TRIP_DETAILS:
					launchTripDetail();
					break;
				case OPTION_LOCATION_NOTES:
					editComments(OPTION_LOCATION_NOTES);
					break;
				case OPTION_TRIP_NOTES:
					editComments(OPTION_TRIP_NOTES);
					break;
				case OPTION_SWITCH_LOCATION:
					listLocations(REQUEST_CODE_SWITCH_LOCATION);
					break;
				case OPTION_SWITCH_TRIP:
					listTrips(REQUEST_CODE_SWITCH_TRIP);
					break;
				case OPTION_LOCATION_MOVE:
					listTrips(REQUEST_CODE_LOCATION_MOVE);
					break;
				case OPTION_LOCATION_REMOVE:
					listLocationsForRemove();
					break;
				case OPTION_TRIP_REMOVE:
					listTripsForRemove();
					break;
				case OPTION_START_TRAIL:
					startTrail();
					break;
				case OPTION_STOP_TRAIL:
					stopTrail();
					break;
				case OPTION_CREATE_ALTITUDE_ALERT:
					createAltitudeAlert();
					break;
				case OPTION_BACKUP_DB:
					backupDb();
					break;
				case OPTION_RESTORE_DB:
					restoreDb();
					break;
				}
			} catch (NumberFormatException e) {
				Toast.makeText(this, "Invalid entry! Expected a number but received " + selectedStr, Toast.LENGTH_SHORT).show();
			} 
        }
    }

    /**
     * Process the result of the location detail activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onLocationDetailResult(int requestCode, int resultCode, Intent data) {

    	WaypointTable table = waypointTableDb.fetchWaypoint(currentWaypointSettings.getSettingLong());
		if (table != null) {
			currentWaypointTable = table;
	        showLocationInfo();
		}
	}

	/**
     * Display the location details in a sub-activity
     */
	private void launchLocationDetail() {
		IntentWrapper wrapper = new IntentWrapper();
		wrapper.setWaypointTable(currentWaypointTable);
		wrapper.setPreferenceData(preferenceData);

	    Intent launchWaypointDetailIntent = new Intent().setClass(this, LocationDetail.class);
	    Bundle extras = new Bundle();
	    extras.putSerializable(WhereWereWe.INTENT_WRAPPER, wrapper);
	    launchWaypointDetailIntent.putExtras(extras);

	    // Make it a subactivity so we know when it returns
	    startActivityForResult(launchWaypointDetailIntent, REQUEST_CODE_LOCATION_DETAIL);
	}
    

    /**
     * Invoked when the location move button is pressed.
     */
    private void listTrips(int action) {

    	Intent launchIntent = new Intent().setClass(this, IconListDialog.class);
	   	Bundle extras = new Bundle();
	   	IconListBundle bundle = new IconListBundle(getString(R.string.select_a_trip), getString(R.string.cancel));

	   	TripTable[] list = tripTableDb.getTripList(true);
	   	for (TripTable tripTable : list) {
		   	bundle.add(tripTable.getName(), R.drawable.details_trip, Long.toString(tripTable.getId()));
		}
	   	 
	   	extras.putSerializable(IconListDialog.BUNDLE_ICON_LIST_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, action);
    }

    /**
     * Process the result of the location move activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onLocationMoveResult(int requestCode, int resultCode, Intent data) {

        Bundle extras = data.getExtras();
        String selectedStr = (String)extras.getSerializable(IconListDialog.BUNDLE_ICON_LIST_RETURN);
        if (selectedStr != null) {
        	try {
				long id = Long.parseLong(selectedStr);
		    	TripTable table = tripTableDb.fetchTrip(id);
				if (table != null) {
			        currentTripFilePath = null;
					currentTripTable = table;
					currentTripSettings.setSetting(id);
					settingsTableDb.updateSettings(currentTripSettings);

		    		currentWaypointTable.setParentTripId(currentTripTable.getId());
		    		waypointTableDb.updateWaypoint(currentWaypointTable);
		    		currentWaypointSettings.setSetting(currentWaypointTable.getId());
					settingsTableDb.updateSettings(currentWaypointSettings);

			        showLocationInfo();
				}

			} catch (NumberFormatException e) {
				Toast.makeText(this, "Invalid entry! Expected a number for the Location ID but received " + selectedStr, Toast.LENGTH_SHORT).show();
			} 
        }
    }

    /**
     * Process the result of the switch location activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onSwitchTripResult(int requestCode, int resultCode, Intent data) {

        Bundle extras = data.getExtras();
        String selectedStr = (String)extras.getSerializable(IconListDialog.BUNDLE_ICON_LIST_RETURN);
        if (selectedStr != null) {
        	try {
				long id = Long.parseLong(selectedStr);
		    	TripTable table = tripTableDb.fetchTrip(id);
				if (table != null) {
			        currentTripFilePath = null;
					currentTripTable = table;
					currentTripSettings.setSetting(id);
					settingsTableDb.updateSettings(currentTripSettings);

		    		currentWaypointTable = waypointTableDb.fetchLatestWaypoint(currentTripTable.getId());
		    		if (currentWaypointTable == null) {
		    			currentWaypointTable = waypointTableDb.createWaypoint(currentTripTable.getId());
		    		}
		    		currentWaypointSettings.setSetting(currentWaypointTable.getId());
					settingsTableDb.updateSettings(currentWaypointSettings);
			        showLocationInfo();
				}

			} catch (NumberFormatException e) {
				Toast.makeText(this, "Invalid entry! Expected a number for the Location ID but received " + selectedStr, Toast.LENGTH_SHORT).show();
			} 
        }
    }

    /**
     * Invoked when the Remove Locations option is selected.
     */
    private void listLocationsForRemove() {
    	Intent launchIntent = new Intent().setClass(this, MultiSelectListDialog.class);
       	Bundle extras = new Bundle();
       	MultiSelectListBundle bundle = new MultiSelectListBundle(getString(R.string.select_locations_to_remove), R.drawable.delete_loc, getString(R.string.remove));
 	   	WaypointTable[] list = waypointTableDb.getWaypointList(currentTripTable.getId());
	   	for (WaypointTable waypointTable : list) {
		   	bundle.add(waypointTable.getName(), R.drawable.forward_48x48, Long.toString(waypointTable.getId()));
		}

      	extras.putSerializable(MultiSelectListDialog.BUNDLE_MULTI_LIST_DIALOG, bundle);
       	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, REQUEST_CODE_LOCATION_REMOVE);
    }

    private void updateCurrentLocation() {
//    	startService(new Intent(this, GPSService.class));
    	okToUpdateLocation = true;
    	if (locationManager == null) {
    		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    		locationListener = new MyLocationListener();
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 
                    WhereWereWe.LOCATION_MIN_PERIOD_MILLIS, 
                    WhereWereWe.LOCATION_MIN_DISTANCE_METERS,
                    locationListener);
    	}

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
//        	if (locationManager != null && locationListener != null) {
//        		locationManager.removeUpdates(locationListener);
//        		locationListener = null;
//        		locationManager = null;
//        	}

        	updateLocationGUI(location.getAltitude(), location.getLatitude(), location.getLongitude());
//    		Toast.makeText(this, getString(R.string.location_refreshed), Toast.LENGTH_SHORT).show();
        }
	}

    /**
     * Create an alert to notify you when you have passed a certain altitude.
     */
    private void createAltitudeAlert() {
    	
    }
    
    /**
     * Start leaving bread crumbs.
     */
    private void startTrail() {

    	stopTrail();
    	createWaypoint();
    	SettingsTable settings = new SettingsTable();
    	settings.setType(SettingsConst.CRUMBS_WAYPOINT);
    	settings.setSetting(currentWaypointTable.getId());
    	settingsTableDb.updateSettings(settings);

    	mAlertSender = PendingIntent.getService(WhereWereWe.this,
                0, new Intent(WhereWereWe.this, GPSService.class), 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        long interval = 120*1000;
        long firstTime = SystemClock.elapsedRealtime() + interval;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        firstTime, interval, mAlertSender);
    }

    /**
     * Stop leaving bread crumbs.
     */
    private void stopTrail() {
    	if (mAlertSender != null) {
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.cancel(mAlertSender);
            mAlertSender = null;
    	}
    }

    /**
     * Invoked when the Remove Locations option is selected.
     */
    private void listTripsForRemove() {
    	Intent launchIntent = new Intent().setClass(this, MultiSelectListDialog.class);
       	Bundle extras = new Bundle();
       	MultiSelectListBundle bundle = new MultiSelectListBundle(getString(R.string.select_trips_to_remove), R.drawable.delete_trip, getString(R.string.remove));
	   	TripTable[] list = tripTableDb.getTripList(true);
	   	for (TripTable tripTable : list) {
		   	bundle.add(tripTable.getName(), R.drawable.details_trip, Long.toString(tripTable.getId()));
		}

      	extras.putSerializable(MultiSelectListDialog.BUNDLE_MULTI_LIST_DIALOG, bundle);
       	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, REQUEST_CODE_TRIP_REMOVE);
    }

    /**
     * Process the result of the remove trip activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onTripRemoveResult(int requestCode, int resultCode, Intent data) {

        Bundle extras = data.getExtras();
        MultiSelectListReturnBundle itemList = (MultiSelectListReturnBundle)extras.getSerializable(MultiSelectListDialog.BUNDLE_MULTI_LIST_RETURN);
        boolean switchTrip = false;
        int count = 0;
        for (MultiSelectListReturnRow row : itemList.getList()) {
        	if (row.isSelected()) {
            	try {
    				long id = Long.parseLong(row.getId());
    		    	TripTable table = tripTableDb.fetchTrip(id);
    				if (table != null) {
    					if (table.getId() == currentTripTable.getId()) {
    						// They've selected the current trip for delete, so we'll need to display another one when finished
    						switchTrip = true;
    					}
    			    	tripTableDb.delete(table);
						count++;
    				}

    			} catch (NumberFormatException e) {
    				Toast.makeText(this, "Invalid entry! Expected a number for the Trip ID but received " + row.getId(), Toast.LENGTH_SHORT).show();
    			} catch (Exception e) {
    				Toast.makeText(this, "An error occurred deleting a trip! ", Toast.LENGTH_LONG).show();
    			}
        	}
        }
        if (count > 0) {
			Toast.makeText(this, "Removed " + count + " trips.", Toast.LENGTH_LONG).show();
        }

        if (switchTrip) {
        	showLatestTrip();
        }
    }

    /**
     * Process the result of the Record Video activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onRecordVideoResult(int requestCode, int resultCode, Intent data) {
    	SettingsTable settings = settingsTableDb.fetchSetting(SettingsConst.LAST_MEDIA_STORE_LOCATION);
    	if (settings == null) {
			Toast.makeText(this, "Cannot get the media path for the video.", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if (resultCode == RESULT_OK) {
    		writeLog("onRecordVideoResult() lastMediaPath='" + settings.getSetting() + "'");
	    }
    	else {
    		// The camera application will save the image as 0 bytes if it is canceled. Annoying, but we can fix it by deleting it.
            File f = new File(settings.getSetting());
            if (f.delete()) {
    			writeLog("onRecordVideoResult() deleted empty file for canceled video='" + settings.getSetting() + "'");
            }
    	}
		
        settingsTableDb.removeSetting(SettingsConst.LAST_MEDIA_STORE_LOCATION);
    }
    
    /**
     * Process the result of the Take Picture activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onCameraResult(int requestCode, int resultCode, Intent data) {
    	SettingsTable settings = settingsTableDb.fetchSetting(SettingsConst.LAST_MEDIA_STORE_LOCATION);
    	if (settings == null) {
			Toast.makeText(this, "Cannot get the media path for the image.", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if (resultCode == RESULT_OK) {
	    	MediaUtilities.createThumbnailInBackground(settings.getSetting());
			writeLog("onCameraResult() lastMediaPath='" + settings.getSetting() + "'" + " NAME:" + currentWaypointTable.getName() + " ParentID:" + currentWaypointTable.getParentTripId() + " ID:" + currentWaypointTable.getId() );
	        // Processed a picture, now go back for more
	        takePicture();
	    }
    	else {
    		// The camera application will save the image as 0 bytes if it is canceled. Annoying, but we can fix it by deleting it.
            File f = new File(settings.getSetting());
            if (f.delete()) {
    			writeLog("onCameraResult() deleted empty file for canceled picture='" + settings.getSetting() + "'");
            }
    	}
        settingsTableDb.removeSetting(SettingsConst.LAST_MEDIA_STORE_LOCATION);
    }

  /**
  * Process the result of the Record Audio activity when it finishes.
  * @param requestCode
  * @param resultCode
  * @param data
  */
 private void onRecordAudioResult(int requestCode, int resultCode, Intent data) {
     Uri audioUri = data.getData();
     if (audioUri != null) {
	        
        // Find the recording and move it to the application space on the SD card.
        String recordingFilename = generateUniqueFilename(FileUtilities.LOCATION_AUDIO);
        String recordingFilepath = getFilepathFromUri(audioUri);
        StringBuilder sb = new StringBuilder();
        String destPath = FileUtilities.getFilePath(FileUtilities.LOCATION_AUDIO, getCurrentTripFilePath(), recordingFilename);
        FileUtilities.mvFile(recordingFilepath, destPath);

        // Put the audio meta data into the recording.
        ContentValues content = new ContentValues();
        content.put(Audio.AudioColumns.TITLE, recordingFilename); 
        content.put(Audio.AudioColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        content.put(Audio.AudioColumns.ALBUM, "WhereWereWe");
        content.put(Audio.AudioColumns.ARTIST, "nono");
        content.put(Audio.Media.MIME_TYPE, "audio/amr");
        content.put(Audio.Media.DATA, destPath);
        ContentResolver resolver = getContentResolver();
        resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, content);
    }
 }

    private String getFilepathFromUri(Uri uri) {
    	if (uri == null) {
            return null;    		
    	}

        Cursor c = managedQuery(uri, null, "", null, null);
        if ( c == null || c.getCount() == 0) {
            return null;
        }

        c.moveToFirst();
        int dataIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

        return c.getString(dataIndex);
    }

    /**
     * Display the most recent trip.
     */
    private void showLatestTrip() {
        currentTripFilePath = null;
    	currentTripTable = tripTableDb.fetchLatestTrip();
    	if (currentTripTable == null) {
    		createTrip();
    	}
    	else {
    		currentTripSettings.setSetting(currentTripTable.getId());
    		settingsTableDb.updateSettings(currentTripSettings);

    		currentWaypointTable = waypointTableDb.fetchLatestWaypoint(currentTripTable.getId());
    		if (currentWaypointTable == null) {
    			currentWaypointTable = waypointTableDb.createWaypoint(currentTripTable.getId());
    		}
    		currentWaypointSettings = settingsTableDb.getDefaultSetting(SettingsConst.CURRENT_WAYPOINT);
    		currentWaypointSettings.setSetting(currentWaypointTable.getId());
			settingsTableDb.updateSettings(currentWaypointSettings);
    	}

        showLocationInfo();
    }

    /**
     * Display the latest Waypoint in the current trip.
     */
    private void showLatestLocation() {
    	currentWaypointTable = waypointTableDb.fetchLatestWaypoint(currentTripTable.getId());
    	if (currentWaypointTable == null) {
    		createWaypoint();
    	}
    	else {
    		currentWaypointSettings.setSetting(currentWaypointTable.getId());
			settingsTableDb.updateSettings(currentWaypointSettings);
    	}
		showLocationInfo();
    }

    private void createTrip() {
        currentTripFilePath = null;
		currentTripTable = tripTableDb.createTrip();
		currentTripSettings.setSetting(currentTripTable.getId());
		settingsTableDb.updateSettings(currentTripSettings);
		createWaypoint();
    }

    private void createWaypoint() {
		currentWaypointTable = waypointTableDb.createWaypoint(currentTripTable.getId());
		currentWaypointSettings.setSetting(currentWaypointTable.getId());
		settingsTableDb.updateSettings(currentWaypointSettings);
    }

	/**
     * Display the trip details in a sub-activity
     */
	private void launchTripDetail() {
		IntentWrapper wrapper = new IntentWrapper();
		wrapper.setTripTable(currentTripTable);
		wrapper.setPreferenceData(preferenceData);

	    Intent launchTripDetailIntent = new Intent().setClass(this, TripDetail.class);
	    Bundle extras = new Bundle();
	    extras.putSerializable(WhereWereWe.INTENT_WRAPPER, wrapper);
	    launchTripDetailIntent.putExtras(extras);

	    // Make it a subactivity so we know when it returns
	    startActivityForResult(launchTripDetailIntent, REQUEST_CODE_TRIP_DETAIL);
	}

    /**
     * Process the result of the trip detail activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onTripDetailResult(int requestCode, int resultCode, Intent data) {

    	TripTable table = tripTableDb.fetchTrip(currentTripSettings.getSettingLong());
		if (table != null) {
	        currentTripFilePath = null;
			currentTripTable = table;
            metaButton.setText(Html.fromHtml(formatMetaInfo(currentTripTable.getName(), currentWaypointTable.getName(), currentTripTable.getStartDate())));
		}
	}
    /**
     * Process the result of the image gallery activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onImageGalleryResult(int requestCode, int resultCode, Intent data) {
	}

    /**
     * Invoked when the location options button is pressed.
     */
    private void listLocations(int action) {

    	Intent launchIntent = new Intent().setClass(this, IconListDialog.class);
	   	Bundle extras = new Bundle();
	   	IconListBundle bundle = new IconListBundle(getString(R.string.locations), getString(R.string.cancel));

	   	WaypointTable[] list = waypointTableDb.getWaypointList(currentTripTable.getId());
	   	for (WaypointTable waypointTable : list) {
		   	bundle.add(waypointTable.getName(), R.drawable.forward_48x48, Long.toString(waypointTable.getId()));
		}
	   	 
	   	extras.putSerializable(IconListDialog.BUNDLE_ICON_LIST_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, action);
    }

    /**
     * Process the result of the remove location activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onLocationRemoveResult(int requestCode, int resultCode, Intent data) {

        Bundle extras = data.getExtras();
        MultiSelectListReturnBundle itemList = (MultiSelectListReturnBundle)extras.getSerializable(MultiSelectListDialog.BUNDLE_MULTI_LIST_RETURN);
        boolean switchLocation = false;
        int count = 0;
        for (MultiSelectListReturnRow row : itemList.getList()) {
        	if (row.isSelected()) {
            	try {
    				long id = Long.parseLong(row.getId());
    		    	WaypointTable table = waypointTableDb.fetchWaypoint(id);
    				if (table != null) {
    					if (table.getId() == currentWaypointTable.getId()) {
    						// They've selected the current location for delete, so we'll need to display another one when finished
    						switchLocation = true;
    					}
    			    	waypointTableDb.delete(table);
						count++;
    				}

    			} catch (NumberFormatException e) {
    				Toast.makeText(this, "Invalid entry! Expected a number for the Location ID but received " + row.getId(), Toast.LENGTH_SHORT).show();
    			} catch (Exception e) {
    				Toast.makeText(this, "An error occurred deleting a location! ", Toast.LENGTH_LONG).show();
    			}
        	}
        }
        if (count > 0) {
			Toast.makeText(this, "Removed " + count + " locations.", Toast.LENGTH_LONG).show();
        }

        if (switchLocation) {
        	currentWaypointTable = waypointTableDb.fetchLatestWaypoint(currentTripTable.getId());
        	if (currentWaypointTable == null) {
        		createWaypoint();
        	}
        	else {
        		currentWaypointSettings.setSetting(currentWaypointTable.getId());
        		settingsTableDb.updateSettings(currentWaypointSettings);
        	}
            showLocationInfo();
        	
        }
    }

    /**
     * Process the result of the switch location activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onSwitchLocationResult(int requestCode, int resultCode, Intent data) {

        Bundle extras = data.getExtras();
        String selectedStr = (String)extras.getSerializable(IconListDialog.BUNDLE_ICON_LIST_RETURN);
        if (selectedStr != null) {
        	try {
				long id = Long.parseLong(selectedStr);
		    	WaypointTable table = waypointTableDb.fetchWaypoint(id);
				if (table != null) {
					currentWaypointTable = table;
					currentWaypointSettings.setSetting(id);
					settingsTableDb.updateSettings(currentWaypointSettings);
			        showLocationInfo();
				}

			} catch (NumberFormatException e) {
				Toast.makeText(this, "Invalid entry! Expected a number for the Location ID but received " + selectedStr, Toast.LENGTH_SHORT).show();
			} 
        }
    }

    /**
     * Back up the database and save it to the SD card.
     */
    private void backupDb() {
    	try {
			int numTripsBackedUp = DbAdapter.backupDb(tripTableDb, waypointTableDb);
			if (numTripsBackedUp < 0) {
	    		Toast.makeText(WhereWereWe.this, getString(R.string.err_cannot_write_file), Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Log.e(WhereWereWe.class.getName(), "IOException occurred while attempting to serialize the trips", e);
    		Toast.makeText(WhereWereWe.this, getString(R.string.err_cannot_write_file), Toast.LENGTH_LONG).show();
		}
    }

    /**
     * Restore the database from the SD card.
     */
    private void restoreDb() {
    	String backupFilePath = DbAdapter.getBackupFilePath();

    	File backupFile = new File(backupFilePath);
    	if(!backupFile.exists()) {
    		Toast.makeText(WhereWereWe.this, getString(R.string.no_backup_file_found), Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	String xml = FileUtilities.readFile(backupFilePath);
    	if (xml == null) {
    		Toast.makeText(WhereWereWe.this, getString(R.string.err_cannot_read_file), Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	DbAdapter.importTrips(xml, tripTableDb, waypointTableDb);
    }

    /**
     * Update the location on the screen
     * @param currentAltitude
     * @param currentLatitude
     * @param currentLongitude
     */
    private void updateLocationGUI(double currentAltitude, double currentLatitude, double currentLongitude) {
    	currentWaypointTable.setAltitude(currentAltitude);
    	currentWaypointTable.setLatitude(currentLatitude);
    	currentWaypointTable.setLongitude(currentLongitude);

    	waypointTableDb.updateWaypoint(currentWaypointTable);
    	showLocationInfo();
    }

    private class MyLocationListener implements LocationListener 
    {
//       	private Handler handler = new Handler();

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
            	if (okToUpdateLocation) {
            		updateLocationGUI(location.getAltitude(), location.getLatitude(), location.getLongitude());
            		Toast.makeText(WhereWereWe.this, getString(R.string.location_refreshed), Toast.LENGTH_SHORT).show();
//    	    		handler.post(new UpdateAddressGUI(location.getAltitude(), location.getLatitude(), location.getLongitude()));
    	    		okToUpdateLocation = false;
            	}
            }
            if (locationManager != null) {
            	locationManager.removeUpdates(this);
            	locationManager = null;
            	locationListener = null;
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

    private void dumpCurrentInfo(String title) {
//		StringBuilder sb = new StringBuilder();
//		sb.append("*** WhereWereWe: ");
//		sb.append(title);
//		sb.append("***\n");
//		sb.append("\nCurrentTrip:ID=");
//		sb.append(currentTripTable.getId());
//		sb.append("\nCurrentWaypoint:TripID=");
//		sb.append(currentWaypointTable.getParentTripId());
//		sb.append(",ID=");
//		sb.append(currentWaypointTable.getId());
//		sb.append(",Name=");
//		sb.append(currentWaypointTable.getName());
//
//		String msg = sb.toString();
//    	Log.d(WhereWereWe.class.getName(),msg);
//
//    	sb.setLength(0);
//    	String comment = currentWaypointTable.getComment();
//    	if (comment != null) {
//    		sb.append(comment);
//    		sb.append('\n');
//    	}
//    	sb.append (new Date());
//		sb.append('\n');
//		sb.append(msg);
//    	currentWaypointTable.setComment(sb.toString());
//        waypointTableDb.updateWaypoint(currentWaypointTable);
    }

    private String getCurrentTripFilePath() {
		if (currentTripFilePath == null) {
	        StringBuilder sb = new StringBuilder();
	    	sb.append(Environment.getExternalStorageDirectory());
	    	sb.append(DbConst.EXTERNAL_FILE_STORE);
	    	sb.append('/');
	    	sb.append(FileUtilities.scrubForGoodFileName(currentTripTable.getId(), "trip_", currentTripTable.getName()));
	        currentTripFilePath = sb.toString();
		}
		return currentTripFilePath;
    }

    private void writeLog(String msg) {
//    	Log.d(WhereWereWe.class.getName(),msg);

//    	StringBuilder sb = new StringBuilder();
//    	String comment = currentWaypointTable.getComment();
//    	if (comment != null) {
//    		sb.append(comment);
//    		sb.append('\n');
//    	}
//    	sb.append (new Date());
//		sb.append(msg);
//    	currentWaypointTable.setComment(sb.toString());
//        waypointTableDb.updateWaypoint(currentWaypointTable);
    }
    
    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private int itemBackground;
 
        private int imageHeight = -1;
        private int imageWidth = -1;

        public ImageAdapter(Context c) {
            context = c;
 
            //---setting the style---                
            TypedArray a = obtainStyledAttributes(R.styleable.Gallery);
//            itemBackground = a.getResourceId(
//                    R.styleable.Gallery_android_galleryItemBackground, 0);
            a.recycle();                                                    
        }
 
        //---returns the number of images---
        public int getCount() {
            return buttonGalleryImageIDs.length;
        }
 
        //---returns the ID of an item--- 
        public Object getItem(int position) {
            return position;
        }
 
        public long getItemId(int position) {
            return position;
        }
 
//        /** Returns the size (0.0f to 1.0f) of the views
//        * depending on the 'offset' to the center. */
//        public float getScale(boolean focused, int offset) {
//        /* Formula: 1 / (2 ^ offset) */
//        return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset)));
//        } 

        private void setWidthAndheight() {
        	if (imageHeight < 0 || imageWidth < 0) {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                Bitmap b = BitmapFactory.decodeResource(getResources(), buttonGalleryImageIDs[0][0], o);
                imageHeight = o.outHeight;
                imageWidth = o.outWidth;
        	}
        	
        }
        //---returns an ImageView view---
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(context);

			Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), buttonGalleryImageIDs[position][0]);
			i.setImageBitmap(imageBitmap);
			setWidthAndheight();
            i.setLayoutParams(new Gallery.LayoutParams(imageWidth, imageHeight));
            i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            i.setBackgroundResource(itemBackground);

            return i;
        }
    }
}
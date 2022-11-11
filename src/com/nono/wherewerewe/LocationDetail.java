package com.nono.wherewerewe;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nono.gui.IconListBundle;
import com.nono.gui.IconListDialog;
import com.nono.wherewerewe.data.IntentWrapper;
import com.nono.wherewerewe.data.PreferenceData;
import com.nono.wherewerewe.data.table.WaypointTable;
import com.nono.wherewerewe.db.DbAdapter;
import com.nono.wherewerewe.db.WaypointTableDb;

public class LocationDetail extends Activity {
	
	private final static int WHEN_DATE_DIALOG_ID = 0;
	private final static int WHEN_TIME_DIALOG_ID = 1;
	private final static String NUMBERS = "0123456789";

	public static final int REQUEST_CODE_PREFERENCES = 1;
	public static final int REQUEST_CODE_ADDRESS_OPTIONS = 2;

	public static final int OPTION_FETCH_ADDRESS = 1;
	public static final int OPTION_FETCH_COORDINATES = 2;

	private final static double FEET_PER_METER = 3.28083989501d;
	private PreferenceData preferenceData = null;

	private DbAdapter dbAdapter = null;
	private WaypointTableDb waypointTableDb;
	private WaypointTable origWaypointTable = null;
	private WaypointTable waypointTable = null;

	private Timer updateTimer = null;
	private String altitudeChanged = null;
	private String latitudeChanged = null;
	private String longitudeChanged = null;
	private EditText addressEdit = null;

	private EditText altitudeEdit = null;
	private EditText latitudeEdit = null;
	private EditText longitudeEdit = null;
	
	private boolean dataUpdated = false;

	private DateFormat dateFormat;
	private DateFormat timeFormat;
	private Button whenDateButton;
	private Button whenTimeButton;
	
	private boolean prefsChanged = false;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_detail);
        dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        waypointTableDb = new WaypointTableDb(dbAdapter);

        dateFormat = android.text.format.DateFormat.getLongDateFormat(getApplicationContext());
        timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());

        Bundle extras = this.getIntent().getExtras();
        IntentWrapper wrapper = (IntentWrapper)extras.getSerializable(WhereWereWe.INTENT_WRAPPER);
        waypointTable = wrapper.getWaypointTable();
        origWaypointTable = (WaypointTable)waypointTable.clone();

        preferenceData = wrapper.getPreferenceData();

        EditText locationNameEdit = (EditText) findViewById(R.id.edit_location_name);
        locationNameEdit.setText(waypointTable.getName());
        locationNameEdit.addTextChangedListener(new MyTextWatcher(MyTextWatcher.NAME_FIELD));
        
        altitudeEdit = (EditText) findViewById(R.id.edit_altitude);
        altitudeEdit.setText(formatAltitude(waypointTable.getAltitude()));
        altitudeEdit.addTextChangedListener(new MyTextWatcher(MyTextWatcher.ALTITUDE_FIELD));

        latitudeEdit = (EditText) findViewById(R.id.edit_latitude);
        latitudeEdit.setText(waypointTable.getLatitudeStr());
        latitudeEdit.addTextChangedListener(new MyTextWatcher(MyTextWatcher.LATITUDE_FIELD));

        longitudeEdit = (EditText) findViewById(R.id.edit_longitude);
        longitudeEdit.setText(waypointTable.getLongitudeStr());
        longitudeEdit.addTextChangedListener(new MyTextWatcher(MyTextWatcher.LONGITUDE_FIELD));

        addressEdit = (EditText) findViewById(R.id.edit_address);
        addressEdit.setText(waypointTable.getAddress());
        addressEdit.addTextChangedListener(new MyTextWatcher(MyTextWatcher.ADDRESS_FIELD));

        whenDateButton = (Button) findViewById(R.id.when_date_button);
        whenDateButton.setText(formatDate(waypointTable.getWhen()));
        whenTimeButton = (Button) findViewById(R.id.when_time_button);
        whenTimeButton.setText(formatTime(waypointTable.getWhen()));
    }

    private String formatDate(long date) {
    	
    	Date displayDate;
    	if (date == 0) {
    		displayDate = new Date();
    	}
    	else {
    		displayDate = new Date(date);
    	}

        return dateFormat.format(displayDate);
    }

    private String formatTime(long datetime) {
    	
    	Date displayTime;
    	if (datetime == 0) {
    		displayTime = new Date();
    	}
    	else {
    		displayTime = new Date(datetime);
    	}

        return timeFormat.format(displayTime);
    }

    public void buttonClickHandler(View view) {

    	view.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING );

		switch (view.getId()) {
		case R.id.address_button:
			showAddressOptions();
			break;
		case R.id.when_date_button:
			showDialog(WHEN_DATE_DIALOG_ID);
			break;
		case R.id.when_time_button:
			showDialog(WHEN_TIME_DIALOG_ID);
			break;
		case R.id.cancel_button:
			restoreAndExit();
			break;
		}
	}

    /**
     * Invoked when the location move button is pressed.
     */
    private void showAddressOptions() {

    	Intent launchIntent = new Intent().setClass(this, IconListDialog.class);
	   	Bundle extras = new Bundle();
	   	IconListBundle bundle = new IconListBundle(getString(R.string.address_options), getString(R.string.cancel));

	   	bundle.add(getString(R.string.fetch_address), R.drawable.address, Integer.toString(OPTION_FETCH_ADDRESS));
	   	bundle.add(getString(R.string.fetch_coordinates), R.drawable.coordinates, Integer.toString(OPTION_FETCH_COORDINATES));
	   	extras.putSerializable(IconListDialog.BUNDLE_ICON_LIST_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, REQUEST_CODE_ADDRESS_OPTIONS);
    }

    /**
     * Process the result of the address options activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onAddressOptionsResult(int requestCode, int resultCode, Intent data) {

        Bundle extras = data.getExtras();
        String selectedStr = (String)extras.getSerializable(IconListDialog.BUNDLE_ICON_LIST_RETURN);
        if (selectedStr != null) {
        	try {
				int selected = Integer.parseInt(selectedStr);
				switch(selected) {
				case OPTION_FETCH_ADDRESS:
		        	Thread reverseGeocoder = new Thread(null, new GeocodeInBackground(true), "reverseGeocoder");
		        	reverseGeocoder.start();
					break;
				case OPTION_FETCH_COORDINATES:
		        	Thread geocoder = new Thread(null, new GeocodeInBackground(false), "geocoder");
		        	geocoder.start();
					break;
				}
			} catch (NumberFormatException e) {
				Toast.makeText(this, "Invalid entry! Expected a number for the Address Options return value but received " + selectedStr, Toast.LENGTH_SHORT).show();
			}
		} 
    }

    private String formatAltitude(double altitude) {
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
     * Update the location on the screen
     * @param currentAltitude
     * @param currentLatitude
     * @param currentLongitude
     */
    private void updateLocationGUI(double currentAltitude, double currentLatitude, double currentLongitude) {
        altitudeEdit.setText(formatAltitude(waypointTable.getAltitude()));
        latitudeEdit.setText(waypointTable.getLatitudeStr());
        longitudeEdit.setText(waypointTable.getLongitudeStr());
    }

    /**
     * Do the reverse Geocode in the background because it is slow.
     */
    private class GeocodeInBackground implements Runnable {
    	
    	private Handler handler = new Handler();
    	private boolean m_fetchAddress = true;

    	GeocodeInBackground(boolean fetchAddress) {
    		m_fetchAddress = fetchAddress;
    	}

	    @Override
		public void run() {
	    	if (m_fetchAddress) {
	    		fetchAddress();
	    	}
	    	else {
	    		fetchCoordinates();
	    	}
		}

		/**
	     * Get the address from the latitude and longitude
	     */
	    private void fetchAddress() {
	    	double latitude = waypointTable.getLatitude();
	    	double longitude = waypointTable.getLongitude();
	    	
	    	if (latitude == 0 && longitude == 0) {
	    		handler.post(new MakeToast(getString(R.string.address_unavailable)));
	    		return;
	    	}
	
	    	List<Address> addresses = null;
	    	Geocoder gc = new Geocoder(LocationDetail.this, Locale.getDefault());
	    	try {
	    		addresses = gc.getFromLocation(latitude, longitude, 1);
	    		if (addresses != null && addresses.size() > 0) {
	    			Address address = addresses.get(0);
	    			if (address != null) {
	    	    		handler.post(new UpdateAddressGUI(formatAddress(address)));
	    			}
	    		}
	    	} catch (Exception e) {
	    		handler.post(new MakeToast(getString(R.string.address_unavailable)));
	    	}
	    }

		/**
	     * Get the address from the latitude and longitude
	     */
	    private void fetchCoordinates() {
	    	String rootAddress = waypointTable.getAddress();
	    	
	    	if (rootAddress == null || rootAddress.length() == 0) {
	    		handler.post(new MakeToast(getString(R.string.no_address_to_lookup)));
	    		return;
	    	}
	
	    	List<Address> addresses = null;
	    	Geocoder gc = new Geocoder(LocationDetail.this, Locale.getDefault());
	    	try {
	    		addresses = gc.getFromLocationName(rootAddress, 1);
	    		if (addresses != null && addresses.size() > 0) {
	    			Address address = addresses.get(0);
	    			if (address != null && address.hasLongitude() && address.hasLatitude()) {
	    	    		handler.post(new UpdateAddressGUI(0, address.getLatitude(), address.getLongitude()));
	    			}
	    			else {
	    	    		handler.post(new MakeToast(getString(R.string.coordinates_unavailable)));
	    			}
	    		}
	    	} catch (Exception e) {
	    		handler.post(new MakeToast(getString(R.string.coordinates_unavailable)));
	    	}
	    }
    }

    private String formatAddress(Address addressIn) {
    	if (addressIn == null) {
    		return "";
    	}

    	StringBuilder sb = new StringBuilder();
    	for (int ndx = 0; ndx < addressIn.getMaxAddressLineIndex(); ndx++) {
    		String line = addressIn.getAddressLine(ndx);
    		if (line != null) {
    			sb.append(line);
    			sb.append('\n');
    		}
		}

    	return sb.toString();
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
            return true;

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
			case REQUEST_CODE_ADDRESS_OPTIONS:
				onAddressOptionsResult(requestCode, resultCode, data);
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
    }

    /**
     * Update the address 
     */
    private class UpdateAddressGUI implements Runnable {
    	public static final int ADDRESS_UPDATE = 1;
    	public static final int COORDINATE_UPDATE = 2;
    	
    	private final int updateType;
    	private String address;
        private double currentAltitude = 0;
        private double currentLatitude = 0;
        private double currentLongitude = 0;

    	UpdateAddressGUI(String address) {
    		this.address = address;
			waypointTable.setAddress(address);
			setUpdateTimer();
			updateType = ADDRESS_UPDATE;
    	}

    	UpdateAddressGUI(double altitude, double latitude, double longitude) {
    		this.currentAltitude = altitude;
    		this.currentLatitude = latitude;
    		this.currentLongitude = longitude;
			waypointTable.setAltitude(altitude);
			waypointTable.setLatitude(latitude);
			waypointTable.setLongitude(longitude);
			setUpdateTimer();
			updateType = COORDINATE_UPDATE;
    	}

	    @Override
		public void run() {
	    	if (updateType == ADDRESS_UPDATE) {
				addressEdit.setText(address);
	    	}
	    	else {
	    		updateLocationGUI(currentAltitude, currentLatitude, currentLongitude);
	    	}
		}
    }

    /**
     * Make a toast message
     */
    private class MakeToast implements Runnable {
    	private final String text;
    	
    	MakeToast(String text) {
    		this.text = text;
    	}

	    @Override
		public void run() {
    		Toast.makeText(LocationDetail.this, text, Toast.LENGTH_LONG).show();
		}
    }

	/**
	 * Update any fields they've changed but have not yet persisted to the database.
	 */
	private void updateChangedFields() {
		boolean doUpdate = false;
    	if (updateTimer != null) {
    		updateTimer.cancel();
    		updateTimer = null;
    		checkNumberUpdates();
    		doUpdate = true;
    	}
    	
    	if (doUpdate) {
    		waypointTableDb.updateWaypoint(waypointTable);
    		dataUpdated = true;
    	}
	}

    @Override
	public void onBackPressed() {
    	updateChangedFields();
    	if (dataUpdated) {
    		// Let the calling activity know something changed
    		setResult(RESULT_OK);
    	}
		super.onBackPressed();
	}

    /**
     * They've pressed cancel, so restore the data to the version that came
     * in and update to make sure it 
     */
    private void restoreAndExit() {
    	if (updateTimer != null) {
    		updateTimer.cancel();
    		updateTimer = null;
    	}

    	if (dataUpdated) {
	    	waypointTable = origWaypointTable;
			waypointTableDb.updateWaypoint(waypointTable);
			altitudeChanged = null;
			latitudeChanged = null;
			longitudeChanged = null;
    	}
    	super.finish();
    }

	@Override
	protected void onStop() {
		updateChangedFields();
		
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

    private void setUpdateTimer() {
    	if (updateTimer != null) {
    		updateTimer.cancel();
    		updateTimer = null;
    	}

    	// Set a timer to update the data in the db 10 seconds after they stop typing
    	updateTimer = new Timer();
    	updateTimer.schedule(new UpdateTimerTask(), 10000);
    }
	
	/**
	 * Handle text updates.
	 */
	private class MyTextWatcher implements TextWatcher {
		public final static int NAME_FIELD = 1;
		public final static int ALTITUDE_FIELD = 3;
		public final static int LATITUDE_FIELD = 4;
		public final static int LONGITUDE_FIELD = 5;
		public final static int ADDRESS_FIELD = 6;
		
		private final int field;
		
		MyTextWatcher(int field) {
			this.field = field;
		}

        public void afterTextChanged(Editable s) {
        	if (updateTimer != null) {
        		updateTimer.cancel();
        		updateTimer = null;
        	}

        	String newVal;
        	if (s != null) {
        		newVal = s.length() == 0 ? null : s.toString();
        	}
        	else {
        		newVal = null;
        	}
        	
        	if (field == NAME_FIELD) {
        		waypointTable.setName(newVal);
        	}
        	else if (field == ALTITUDE_FIELD) {
    			altitudeChanged = newVal;
        	}
        	else if (field == LATITUDE_FIELD) {
    			latitudeChanged = newVal;
        	}
        	else if (field == LONGITUDE_FIELD) {
    			longitudeChanged = newVal;
        	}
        	else {
        		waypointTable.setAddress(newVal);
        	}

        	setUpdateTimer();
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
       }
	}

	/**
	 * Check the number fields to see if they've changed. If so, try to set them
	 * as actual numbers in the database table.
	 * @return True if anything has changed.
	 */
	private boolean checkNumberUpdates() {
		Double doubleVal;
		boolean changed = false;

		if (altitudeChanged != null) {
    		try {
    			altitudeChanged = altitudeChanged.trim();
    			int firstNonNumeric = -1;
    			for (int ndx = 0; ndx < altitudeChanged.length(); ndx++) {
					char c = altitudeChanged.charAt(ndx);
					if (NUMBERS.indexOf(c) == -1) {
						firstNonNumeric = ndx;
						break;
					}
				}
    			if (firstNonNumeric > -1) {
    				altitudeChanged = altitudeChanged.substring(0, firstNonNumeric);
    			}
    			if (altitudeChanged.length() > 0) {
    				doubleVal = Double.parseDouble(altitudeChanged);
    				if (!preferenceData.isMeasurementMetric()) {
    					// They entered the value in feet, we must convert it to meters for storage.
    					doubleVal = doubleVal / FEET_PER_METER;
    				}
    			}
    			else {
    				doubleVal = 0.0;
    			}
				waypointTable.setAltitude(doubleVal);
				changed = true;
			} catch (NumberFormatException e) {
				// Not a valid number, so don't set the field to null. That way the caller
				// can check the field and if needed, retry the update later
			}
			altitudeChanged = null;
		}
		if (latitudeChanged != null) {
    		try {
				doubleVal = Double.parseDouble(latitudeChanged);
				waypointTable.setLatitude(doubleVal);
				changed = true;
			} catch (NumberFormatException e) {
				// Not a valid number, so don't set the field to null. That way the caller
				// can check the field and if needed, retry the update later
			}
			latitudeChanged = null;
		}
		if (longitudeChanged != null) {
    		try {
				doubleVal = Double.parseDouble(longitudeChanged);
				waypointTable.setLongitude(doubleVal);
				changed = true;
			} catch (NumberFormatException e) {
				// Not a valid number, so don't set the field to null. That way the caller
				// can check the field and if needed, retry the update later
			}
			longitudeChanged = null;
		}
		
		return changed;
	}

	private class UpdateTimerTask extends TimerTask {
    	public void run() {
    		checkNumberUpdates();
    		waypointTableDb.updateWaypoint(waypointTable);
    		dataUpdated = true;

    		updateTimer.cancel();
    		updateTimer = null;
    	}
	}


    @Override
    protected Dialog onCreateDialog(int id) {
    	long dbDate = 0;
    	Date displayDate;
		Calendar cal = Calendar.getInstance();
		Dialog dialog = null;

        switch (id) {
        case WHEN_DATE_DIALOG_ID:
        	dbDate = waypointTable.getWhen(); 
            if (dbDate > 0) {
            	displayDate = new Date(dbDate);
            	cal.setTime(displayDate);
            }

            dialog = new DatePickerDialog(this,
                    new MyDateSetListener(),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            break;
        case WHEN_TIME_DIALOG_ID:
        	dbDate = waypointTable.getWhen(); 
            if (dbDate > 0) {
            	displayDate = new Date(dbDate);
            	cal.setTime(displayDate);
            }

            dialog = new TimePickerDialog(this,
                    new MyTimeSetListener(),
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), android.text.format.DateFormat.is24HourFormat(this));
        	break;
        }

        return dialog;
    }

    private class MyDateSetListener implements DatePickerDialog.OnDateSetListener {

		@Override
        public void onDateSet(DatePicker view, int year, 
                int monthOfYear, int dayOfMonth) {

			Date displayDate = new Date(waypointTable.getWhen());

			Calendar cal = Calendar.getInstance();
			cal.setTime(displayDate);
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, monthOfYear);
			cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			
			long dbDate = cal.getTimeInMillis();
            whenDateButton.setText(formatDate(dbDate));
	        waypointTable.setWhen(dbDate);
	        setUpdateTimer();
		}
    }

    private class MyTimeSetListener implements TimePickerDialog.OnTimeSetListener {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			Date displayDate = new Date(waypointTable.getWhen());

			Calendar cal = Calendar.getInstance();
			cal.setTime(displayDate);
			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			cal.set(Calendar.MINUTE, minute);

			long dbDate = cal.getTimeInMillis();
            whenTimeButton.setText(formatTime(dbDate));
	        waypointTable.setWhen(dbDate);
	        setUpdateTimer();
		}
    }
}
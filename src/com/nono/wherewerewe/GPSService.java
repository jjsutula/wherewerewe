package com.nono.wherewerewe;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.nono.wherewerewe.data.SettingsConst;
import com.nono.wherewerewe.data.table.SettingsTable;
import com.nono.wherewerewe.data.table.WaypointTable;
import com.nono.wherewerewe.db.DbAdapter;
import com.nono.wherewerewe.db.SettingsTableDb;
import com.nono.wherewerewe.db.WaypointTableDb;

public class GPSService extends Service {

	private LocationManager locationManager = null;
    private LocationListener locationListener = null;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		updateCurrentLocation();

		// This service exists to get the GPS once and then quit, so don't start sticky
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

    private void updateCurrentLocation() {
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
        	updateLocationGUI(location.getAltitude(), location.getLatitude(), location.getLongitude());
        }
	}

    private class MyLocationListener implements LocationListener 
    {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
        		updateLocationGUI(location.getAltitude(), location.getLatitude(), location.getLongitude());
            }

    		stopSelf();
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, 
            Bundle extras) {}
    }        

    /**
     * Update the location on the screen
     * @param currentAltitude
     * @param currentLatitude
     * @param currentLongitude
     */
    private void updateLocationGUI(double currentAltitude, double currentLatitude, double currentLongitude) {
    	DbAdapter dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        SettingsTableDb settingsTableDb = new SettingsTableDb(dbAdapter);
        WaypointTableDb waypointTableDb = new WaypointTableDb(dbAdapter);
        
        // Retrieve the current waypoint
        SettingsTable currentWaypointSettings = null;
        currentWaypointSettings = settingsTableDb.fetchSetting(SettingsConst.CRUMBS_WAYPOINT);
        if (currentWaypointSettings == null || currentWaypointSettings.getSetting() == null) {
        	return;
    	}
        WaypointTable currentWaypointTable = waypointTableDb.fetchWaypoint(currentWaypointSettings.getSettingLong());
        if (currentWaypointTable == null) {
        	return;
    	}
        
        // Bump the crumb ID in the main location
        int crumbNum = currentWaypointTable.getCrumbNum();
        currentWaypointTable.setCrumbNum(++crumbNum);
    	waypointTableDb.updateWaypoint(currentWaypointTable);

        // Make a new bread crumb, then set the new data into it
        WaypointTable newBreadcrumb = waypointTableDb.createWaypoint(currentWaypointTable.getParentTripId());
        newBreadcrumb.setAltitude(currentAltitude);
        newBreadcrumb.setLatitude(currentLatitude);
        newBreadcrumb.setLongitude(currentLongitude);
        newBreadcrumb.setTrail(currentWaypointTable.getId());
        newBreadcrumb.setCrumbNum(crumbNum);
    	waypointTableDb.updateWaypoint(newBreadcrumb);
    }
}

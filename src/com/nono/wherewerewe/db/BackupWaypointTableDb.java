package com.nono.wherewerewe.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import com.nono.wherewerewe.data.DbConst;
import com.nono.wherewerewe.data.table.WaypointTable;

public class BackupWaypointTableDb {
	
	public static final String TABLE_NAME = DbConst.WAYPOINT_TABLE;

	public static final String CREATE_TABLE_CMD = 
	      "create table " + TABLE_NAME + " (" +
	        	DbConst.KEY_ID + " integer primary key autoincrement, " +
	        	DbConst.TRIP_ID + " integer not null, " +
	        	DbConst.NAME + " text, " +
	        	DbConst.WHEN + " long not null, " +
	        	DbConst.ADDRESS + " text, " +
	        	DbConst.ALTITUDE + " double, " +
	        	DbConst.LATITUDE + " text, " +
	        	DbConst.LONGITUDE + " text, " +
	        	DbConst.COMMENT + " text," +
	        	DbConst.INACTIVE + " integer" +
	      ");";
	public static final String DROP_TABLE_CMD = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

	private final DbAdapter dbAdapter;
	
	public BackupWaypointTableDb(DbAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	/**
     * Insert the waypoint.
     * @param table The waypoint to insert.
     * @param tripId The id of the trip to which the waypoint belongs.
     * @return The inserted waypoint table or null if the insert failed.
     */
    public WaypointTable insertWaypoint(WaypointTable table, long tripId) {
        if (table.getName() == null) {
        	table.setName(createNewName());
        }

    	ContentValues contentValues = table.getContentValues();
    	if (contentValues == null) {
    		contentValues = new ContentValues();
			contentValues.put(DbConst.NAME, createNewName());
    	}
        contentValues.put(DbConst.TRIP_ID, tripId);
    	long id = -1;
		id = dbAdapter.getSqldb().insert(TABLE_NAME, null, contentValues);
        if (id < 0) {
        	return null;
        }

        table.setId(id);
        table.setParentTripId(tripId);
        table.resetContentsChanged();

        return table;
    }

	/**
     * Create a new waypoint.
     * @param tripId The trip id.
     * @return The created waypoint table or null if the insert failed.
     */
    public WaypointTable createWaypoint(long tripId) {
    	return createWaypoint(null, -1, tripId);
	}

	/**
     * Create a new waypoint.
     * @param name The name of the waypoint.
     * @param startDate The start date of the waypoint. If this
     * is -1 then the current system date time will be used.
     * @param tripId The trip id.
     * @return The created waypoint table or null if the insert failed.
     */
    public WaypointTable createWaypoint(String name, long when, long tripId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbConst.TRIP_ID, tripId);
        if (name == null) {
        	name = createNewName();
        }
        contentValues.put(DbConst.NAME, name);
        if (when < 0) {
        	when = System.currentTimeMillis();
        }
        contentValues.put(DbConst.WHEN, when);
        long id = dbAdapter.getSqldb().insert(TABLE_NAME, null, contentValues); 
        if (id < 0) {
        	return null;
        }
        
        WaypointTable table = new WaypointTable();
        table.setId(id);
        table.setParentTripId(tripId);
        table.setName(name);
        table.setWhen(when);
        table.resetContentsChanged();

        return table;
    }

    /**
     * Retrieve the waypoints in the database. Provides partial data from each table for fast display purposes.
     * @param tripId The ID of the trip from which the waypoints should be retrieved.
     * @return A cursor over all returned waypoints.
     */
    public Cursor fetchWaypointDisplayList(long tripId) {
		StringBuilder sb = new StringBuilder();
		sb.append(DbConst.TRIP_ID);
		sb.append("=");
		sb.append(tripId);
		sb.append(" and (");
		sb.append(DbConst.INACTIVE);
		sb.append(" is null or ");
		sb.append(DbConst.INACTIVE);
		sb.append(" == 0)");
		
        return dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.TRIP_ID, DbConst.NAME},
        		sb.toString(), null, null, null, DbConst.WHEN);
    }

    /**
     * Retrieve the waypoints in the database. Provides partial data from each table for fast display purposes.
     * @return A cursor over all returned waypoints.
     */
    public Cursor fetchWaypointDisplayList() {
		StringBuilder sb = new StringBuilder();
		sb.append(DbConst.INACTIVE);
		sb.append(" is null or ");
		sb.append(DbConst.INACTIVE);
		sb.append(" == 0");
		String where = sb.toString();
		sb.setLength(0);
		sb.append("COALESCE(");
		sb.append(DbConst.TRIP_ID);
		sb.append(",");
		sb.append(DbConst.WHEN);
		sb.append(")");
		String orderBy = sb.toString();

        return dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.TRIP_ID, DbConst.NAME},
        		where, null, null, null, orderBy);
    }

    /**
     * Retrieve the waypoints in the database for the specified trip.
     * @param tripId The trip for which to fetch the waypoints
     * @return An array of all returned waypoints.
     */
    public WaypointTable[] getWaypointList(long tripId) {
    	
    	WaypointTable[] list;
		StringBuilder sb = new StringBuilder();
		sb.append(DbConst.TRIP_ID);
		sb.append("=");
		sb.append(tripId);
		sb.append(" and (");
		sb.append(DbConst.INACTIVE);
		sb.append(" is null or ");
		sb.append(DbConst.INACTIVE);
		sb.append(" == 0)");
		Cursor cursor =
        	dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.TRIP_ID, DbConst.NAME,
            		DbConst.WHEN, DbConst.ADDRESS, DbConst.ALTITUDE, DbConst.LATITUDE, DbConst.LONGITUDE, DbConst.COMMENT}, 
            		sb.toString(), null, null, null, DbConst.WHEN);
        if (cursor == null) {
        	return new WaypointTable[0];
        }

        list = new WaypointTable[cursor.getCount()];
        int rowNdx = 0;
        WaypointTable table;
        int columnIndex;
        while (cursor.moveToNext()) {
        	columnIndex = 0;
	        table = new WaypointTable();
	        table.setId(cursor.getLong(columnIndex++));
	        table.setParentTripId(cursor.getLong(columnIndex++));
	        table.setName(cursor.getString(columnIndex++));
	        table.setWhen(cursor.getLong(columnIndex++));
	        table.setAddress(cursor.getString(columnIndex++));
	        table.setAltitude(cursor.getDouble(columnIndex++));
	        table.setLatitude(cursor.getString(columnIndex++));
	        table.setLongitude(cursor.getString(columnIndex++));
	        table.setComment(cursor.getString(columnIndex++));
	        table.resetContentsChanged();
	        
	        list[rowNdx++] = table;
	    }
    
        cursor.close();
    	
        return list;
    }

    /**
     * Return a waypoint.
     * @param keyId The ID of the waypoint to retrieve.
     * @return The found matching waypoint, or null if the waypoint was not found.
     * @throws SQLException if the waypoint could not be found.
     */
    public WaypointTable fetchWaypoint(long keyId) throws SQLException {

        Cursor cursor =
        	dbAdapter.getSqldb().query(true, TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.TRIP_ID, DbConst.NAME,
            		DbConst.WHEN, DbConst.ADDRESS, DbConst.ALTITUDE, DbConst.LATITUDE, DbConst.LONGITUDE, DbConst.COMMENT}, DbConst.KEY_ID + "=" + keyId, null,
                        null, null, null, null);
        if (cursor == null) {
        	return null;
        }

        WaypointTable table;
        if (cursor.moveToFirst()) {
	        int columnIndex = 0;
	        table = new WaypointTable();
	        table.setId(cursor.getLong(columnIndex++));
	        table.setParentTripId(cursor.getLong(columnIndex++));
	        table.setName(cursor.getString(columnIndex++));
	        table.setWhen(cursor.getLong(columnIndex++));
	        table.setAddress(cursor.getString(columnIndex++));
	        table.setAltitude(cursor.getDouble(columnIndex++));
	        table.setLatitude(cursor.getString(columnIndex++));
	        table.setLongitude(cursor.getString(columnIndex++));
	        table.setComment(cursor.getString(columnIndex++));
	        table.resetContentsChanged();
	    }
	    else {
	    	table = null;
	    }
    
        cursor.close();

        return table;
    }

    /**
     * Return the most recent waypoint for the trip.
     * @param tripId The ID of the trip that contains the waypoint to retrieve.
     * @return The found matching waypoint, or null if the waypoint was not found.
     * @throws SQLException if the waypoint could not be found.
     */
    public WaypointTable fetchLatestWaypoint(long tripId) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append(DbConst.TRIP_ID);
		sb.append("=");
		sb.append(tripId);
		sb.append(" and (");
		sb.append(DbConst.INACTIVE);
		sb.append(" is null or ");
		sb.append(DbConst.INACTIVE);
		sb.append(" == 0)");

        Cursor cursor = dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.TRIP_ID, DbConst.NAME,
        		DbConst.WHEN, DbConst.ADDRESS, DbConst.ALTITUDE, DbConst.LATITUDE, DbConst.LONGITUDE, DbConst.COMMENT},
            		sb.toString(), null, null, null, DbConst.WHEN);
        if (cursor == null) {
        	return null;
        }

        WaypointTable table;
        if (cursor.moveToLast()) {	// Only want the last row, since the list is ordered by 'when'
	        int columnIndex = 0;
	        table = new WaypointTable();
	        table.setId(cursor.getLong(columnIndex++));
	        table.setParentTripId(cursor.getLong(columnIndex++));
	        table.setName(cursor.getString(columnIndex++));
	        table.setWhen(cursor.getLong(columnIndex++));
	        table.setAddress(cursor.getString(columnIndex++));
	        table.setAltitude(cursor.getDouble(columnIndex++));
	        table.setLatitude(cursor.getString(columnIndex++));
	        table.setLongitude(cursor.getString(columnIndex++));
	        table.setComment(cursor.getString(columnIndex++));
	        table.resetContentsChanged();
	    }
	    else {
	    	table = null;
	    }
    
        cursor.close();

        return table;
    }

    /**
     * Update the waypoint.
     * @param keyId The ID of the waypoint to update.
     * @param name The value to set.
     * @param body value to set note body to
     * @return false if the waypoint could not be updated.
     */
    public boolean updateWaypoint(WaypointTable table) {
    	ContentValues contentValues = table.getContentValues();
		table.resetContentsChanged();
    	if (contentValues != null) {
    		int numRows = dbAdapter.getSqldb().update(TABLE_NAME, contentValues, DbConst.KEY_ID + "=" + table.getId(), null);
    		if (numRows == 0) { 
    			return false;
    		}
    	}

    	return true;
    }

     /**
      * Delete the item.
      * @param table The item to delete.
      * @return true if deleted, false otherwise.
       */
      public boolean delete(WaypointTable table) {
          return delete(table, false);
      }

      /**
       * Delete the item.
       * @param table The item to delete.
       * @param soft True if the item should only be marked as inactive in the database, and not removed.
       * @return true if deleted, false otherwise.
        */
       public boolean delete(WaypointTable table, boolean soft) {
    	   boolean found = false;

    	   if (soft) {
    		   ContentValues contentValues = new ContentValues();
    		   contentValues.put(DbConst.INACTIVE, DbConst.DELETED);
    		   found = dbAdapter.getSqldb().update(TABLE_NAME, contentValues, DbConst.KEY_ID + "=" + table.getId(), null) > 0;
    	   }
    	   else {
    		   found = dbAdapter.getSqldb().delete(TABLE_NAME, DbConst.KEY_ID + "=" + table.getId(), null) > 0;
    	   }
    	   
    	   return found;
       }
       
    /**
     * Create a unique Waypoint name.
     * @return
     */
    public String createNewName() {
    	
    	Cursor cursor = dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.NAME}, null, null, null, null, DbConst.NAME);
        String name = dbAdapter.createUniqueName("Location", cursor);
        cursor.close();
        return name;
    }
}

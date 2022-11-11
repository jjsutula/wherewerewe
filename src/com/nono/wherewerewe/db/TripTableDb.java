package com.nono.wherewerewe.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import com.nono.wherewerewe.data.DbConst;
import com.nono.wherewerewe.data.table.TripTable;

public class TripTableDb {
	
	public static final String TABLE_NAME = DbConst.TRIP_TABLE;

	public static final String CREATE_TABLE_CMD = 
		"create table " + TABLE_NAME + " (" +
			DbConst.KEY_ID + " integer primary key autoincrement, " +
			DbConst.NAME + " text, " +
			DbConst.TRIP_ID + " integer, " +
			DbConst.START_DATE + " long not null, " +
			DbConst.END_DATE + " long, " +
			DbConst.COMMENT + " text," +
        	DbConst.INACTIVE + " integer" +
		");";
	public static final String DROP_TABLE_CMD = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

	private final DbAdapter dbAdapter;
	
	public TripTableDb(DbAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	/**
     * Insert the trip.
     * @param table The trip to insert.
     * @return The inserted trip table or null if the insert failed.
     */
    public TripTable insertTrip(TripTable table) {
    	String changedName = ensureNameIsUnique(table.getName());
    	if (changedName != null) {
    		table.setName(changedName);
    	}

    	ContentValues contentValues = table.getContentValues();
    	if (contentValues == null) {
    		contentValues = new ContentValues();
			contentValues.put(DbConst.NAME, createNewName());
    	}
    	long id = -1;
		id = dbAdapter.getSqldb().insert(TABLE_NAME, null, contentValues);
        if (id < 0) {
        	return null;
        }

        table.setId(id);
        table.resetContentsChanged();

        return table;
    }

	/**
     * Create a new trip.
     * @return The created trip table or null if the insert failed.
     */
    public TripTable createTrip() {
    	return createTrip(null, -1);
	}

	/**
     * Create a new trip.
     * @param name The name of the trip.
     * @param startDate The start date of the trip. If this
     * is -1 then the current system date time will be used.
     * @return The created trip table or null if the insert failed.
     */
    public TripTable createTrip(String name, long startDate) {
        ContentValues contentValues = new ContentValues();
        if (name == null) {
        	name = createNewName();
        }
        contentValues.put(DbConst.NAME, name);
        if (startDate < 0) {
        	startDate = System.currentTimeMillis();
        }
        contentValues.put(DbConst.START_DATE, startDate);
        long id = dbAdapter.getSqldb().insert(TABLE_NAME, null, contentValues); 
        if (id < 0) {
        	return null;
        }
        
        TripTable table = new TripTable();
        table.setId(id);
        table.setName(name);
        table.setStartDate(startDate);
        table.resetContentsChanged();

        return table;
    }

    /**
     * Retrieve the id and name of the trips in the database.
     * @param includeSubtrips If true, the list will contain all the trips whether or not they have a parent.
     * If false, the returned list will only contain top level trips.
     * @return A cursor over all returned trips.
     */
    public Cursor fetchTripDisplayList(boolean includeSubtrips) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(DbConst.INACTIVE);
		sb.append(" is null or ");
		sb.append(DbConst.INACTIVE);
		sb.append(" == 0)");
    	if (!includeSubtrips) {
    		sb.append(" and (");
    		sb.append(DbConst.TRIP_ID);
    		sb.append(" is null or ");
    		sb.append(DbConst.TRIP_ID);
    		sb.append(" < 0)");
    	}
        return dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.NAME}, sb.toString(), null, null, null, DbConst.START_DATE);
    }

    /**
     * Retrieve the id and name of the subtrips belonging to the input parent trip.
     * @param parentTripID The ID of the parent trip.
     * @return Cursor over all subtrips for the input trip.
     */
    public Cursor fetchSubTripList(int parentTripID) {

		StringBuilder sb = new StringBuilder();
		sb.append(DbConst.TRIP_ID);
		sb.append("=");
		sb.append(parentTripID);
		sb.append(" and (");
		sb.append(DbConst.INACTIVE);
		sb.append(" is null or ");
		sb.append(DbConst.INACTIVE);
		sb.append(" == 0)");
		
        return dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.NAME}, sb.toString(), null, null, null, DbConst.START_DATE);
    }

    /**
     * Retrieve the trips in the database.
     * @param includeSubtrips If true, the list will contain all the trips whether or not they have a parent.
     * If false, the returned list will only contain top level trips.
     * @return An array of all returned trips.
     */
    public TripTable[] getTripList(boolean includeSubtrips) {
    	
    	TripTable[] list;
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(DbConst.INACTIVE);
		sb.append(" is null or ");
		sb.append(DbConst.INACTIVE);
		sb.append(" == 0)");
    	if (!includeSubtrips) {
    		sb.append(" and (");
    		sb.append(DbConst.TRIP_ID);
    		sb.append(" is null or ");
    		sb.append(DbConst.TRIP_ID);
    		sb.append(" < 0)");
    	}
		Cursor cursor =
        	dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.NAME, DbConst.TRIP_ID,
            		DbConst.START_DATE, DbConst.END_DATE, DbConst.COMMENT}, 
            		sb.toString(), null, null, null, DbConst.START_DATE);
        if (cursor == null) {
        	return new TripTable[0];
        }

        list = new TripTable[cursor.getCount()];
        int rowNdx = 0;
        TripTable table;
        int columnIndex;
        while (cursor.moveToNext()) {
        	columnIndex = 0;
	        table = new TripTable();
	        table.setId(cursor.getLong(columnIndex++));
	        table.setName(cursor.getString(columnIndex++));
	        table.setParentTripId(cursor.getLong(columnIndex++));
	        table.setStartDate(cursor.getLong(columnIndex++));
	        table.setEndDate(cursor.getLong(columnIndex++));
	        table.setComment(cursor.getString(columnIndex++));
	        table.resetContentsChanged();

	        list[rowNdx++] = table;
	    }
    
        cursor.close();
    	
        return list;
    }

    /**
     * Return a trip.
     * @param keyId The ID of the trip to retrieve.
     * @return The found matching trip, or null if the trip was not found.
     * @throws SQLException if the trip could not be found.
     */
    public TripTable fetchTrip(long keyId) throws SQLException {

        Cursor cursor =
        	dbAdapter.getSqldb().query(true, TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.NAME, DbConst.TRIP_ID,
            		DbConst.START_DATE, DbConst.END_DATE, DbConst.COMMENT}, DbConst.KEY_ID + "=" + keyId, null,
                        null, null, null, null);
        if (cursor == null) {
        	return null;
        }

        TripTable table;
        if (cursor.moveToFirst()) {
	        int columnIndex = 0;
	        table = new TripTable();
	        table.setId(cursor.getLong(columnIndex++));
	        table.setName(cursor.getString(columnIndex++));
	        table.setParentTripId(cursor.getLong(columnIndex++));
	        table.setStartDate(cursor.getLong(columnIndex++));
	        table.setEndDate(cursor.getLong(columnIndex++));
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
     * Return the most recent trip.
     * @return The found matching trip, or null if the trip was not found.
     * @throws SQLException if the trip could not be found.
     */
    public TripTable fetchLatestTrip() throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(DbConst.INACTIVE);
		sb.append(" is null or ");
		sb.append(DbConst.INACTIVE);
		sb.append(" == 0)");
		
        Cursor cursor =
        	dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.NAME, DbConst.TRIP_ID,
            		DbConst.START_DATE, DbConst.END_DATE, DbConst.COMMENT}, sb.toString(), null, null, null, DbConst.START_DATE);
        if (cursor == null) {
        	return null;
        }

        TripTable table;
        if (cursor.moveToLast()) {	// Only want the last row, since the list is ordered by start date
	        int columnIndex = 0;
	        table = new TripTable();
	        table.setId(cursor.getLong(columnIndex++));
	        table.setName(cursor.getString(columnIndex++));
	        table.setParentTripId(cursor.getLong(columnIndex++));
	        table.setStartDate(cursor.getLong(columnIndex++));
	        table.setEndDate(cursor.getLong(columnIndex++));
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
     * Update the trip.
     * @param keyId The ID of the trip to update.
     * @param name The value to set.
     * @param body value to set note body to
     * @return false if the trip could not be updated.
     */
    public boolean updateTrip(TripTable table) {
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
      public boolean delete(TripTable table) {
          return delete(table, false);
      }
    
      /**
       * Delete the item.
       * @param table The item to delete.
       * @param soft True if the item should only be marked as inactive in the database, and not removed.
       * @return true if deleted, false otherwise.
        */
       public boolean delete(TripTable table, boolean soft) {
    	   boolean found = false;

    	   if (soft) {
    		   ContentValues contentValues = new ContentValues();
    		   contentValues.put(DbConst.INACTIVE, DbConst.DELETED);
    		   dbAdapter.getSqldb().update(DbConst.WAYPOINT_TABLE, contentValues, DbConst.TRIP_ID + "=" + table.getId(), null);
    		   found = dbAdapter.getSqldb().update(TABLE_NAME, contentValues, DbConst.KEY_ID + "=" + table.getId(), null) > 0;
    	   }
    	   else {
    		   dbAdapter.getSqldb().delete(DbConst.WAYPOINT_TABLE, DbConst.TRIP_ID + "=" + table.getId(), null);
    		   found = dbAdapter.getSqldb().delete(TABLE_NAME, DbConst.KEY_ID + "=" + table.getId(), null) > 0;
    	   }
    	   
    	   return found;
       }

       /**
        * Verify that the input name is unique in the database and return a unique
        * name if needed. 
        * @param currentName The name to verify. If this is null then a new name wil be generated and returned.
        * @return A unique name if the input name is null or was already present in the database. Null
        * if the input name is unique.
        */
    public String ensureNameIsUnique(String currentName) {
    	if (currentName == null) {
    		return createNewName();
    	}
    	
    	String newName = currentName.trim();
    	if (newName.length() == 0) {
    		return createNewName();
    	}

    	// See if the input name already exists.
        Cursor cursor;

        cursor = dbAdapter.getSqldb().query(true, TABLE_NAME, new String[] {DbConst.KEY_ID}, DbConst.NAME + "= '" + newName + "'", null,
                        null, null, null, null);
        if (cursor == null) {
        	// Name doesn't already exist in the database, so no new name is needed. Return null to indicate this is the case.
        	return null;
        }

        if (cursor.moveToFirst()) {
        	// The current name exists, find a unique name
            cursor.close();
            cursor = dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.NAME}, null, null, null, null, DbConst.NAME);
        	newName = dbAdapter.createUniqueName(currentName, cursor);
        }
        else {
        	// No results, so the name does not already exist and we already have a unique name
        	// so return null to indicate that nothing should change. But first verify the trim()
        	// operation didn't change anything.
        	if (newName.length() == currentName.length()) {
        		newName = null;
        	}
        	
        }
        cursor.close();
        
        return newName;
    }

    /**
     * Create a unique Trip name.
     * @return
     */
    public String createNewName() {
    	
    	Cursor cursor = dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.NAME}, null, null, null, null, DbConst.NAME);
        String name = dbAdapter.createUniqueName("Trip", cursor);
        cursor.close();
        return name;
    }
}

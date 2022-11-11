package com.nono.wherewerewe.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.nono.util.FileUtilities;
import com.nono.wherewerewe.data.DbConst;
import com.nono.wherewerewe.data.TripData;
import com.nono.wherewerewe.data.WaypointData;
import com.nono.wherewerewe.data.table.TripTable;
import com.nono.wherewerewe.data.table.WaypointTable;

public class DbAdapter {

	private static final long serialVersionUID = 1L;
	
	private static boolean importRequiredDueToUpgrade = false;

	private SQLiteDatabase sqldb;
	private WhereWereWeDbHelper dbHelper = null;

	public DbAdapter(Context context) {
    	dbHelper = new WhereWereWeDbHelper(this, context);
	}
	
    /**
     * Open the database. If it cannot be opened as a writable database, try to
     * open a readonly version of the database. If that doesn't work, give up and
     * throw an exception.
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could not be neither opened.
     */
    public DbAdapter open() throws SQLException {
    	boolean doImport = false;
    	try {
			sqldb = dbHelper.getWritableDatabase();
        	if (importRequiredDueToUpgrade) {
        		doImport = true;
        	}
		} catch (SQLException e) {
			Log.e(DbAdapter.class.getName(), "SQLException occurred while attempting to open the database for writing. Will attempt to open a readonly database.", e);
			sqldb = dbHelper.getReadableDatabase();
		}

		if (doImport) {
	    	try {
            	String backupFilePath = DbAdapter.getBackupFilePath();
            	String xml = FileUtilities.readFile(backupFilePath);
            	if (xml != null) {
                    TripTableDb tripTableDb = new TripTableDb(this);
                    WaypointTableDb waypointTableDb = new WaypointTableDb(this);
                	String[] names = DbAdapter.importTrips(xml, tripTableDb, waypointTableDb);
    				Log.i(DbAdapter.class.getName(), "WhereWereWe database imported " + names.length + " trips for upgrade.");
            	}
            	importRequiredDueToUpgrade = false;
			} catch (SQLException e) {
				Log.e(DbAdapter.class.getName(), "SQLException occurred while attempting to import data into the upgraded database.", e);
				sqldb = dbHelper.getReadableDatabase();
			}
		}

        return this;
    }
    
    /**
     * Only for use upon initialization.
     * @param sqldb The sql db to use for initialization
     */
    protected void setSqldb(SQLiteDatabase sqldb) {
    	this.sqldb = sqldb;
    }

    /**
	 * @return the sqldb
	 */
	public SQLiteDatabase getSqldb() {
		return sqldb;
	}

	public void close() {
    	dbHelper.close();
    }
	
	/**
	 * Create a unique name from the specified currentName
	 * @param currentName A current name or a type such as Trip or Waypoint
	 * @param cursor A cursor pointing to the queried names of the correct type table
	 * @return A name that is not currently in use in the given table.
	 */
    protected String createUniqueName(String currentName, Cursor cursor) {
    	StringBuilder nameSb = new StringBuilder();
    	nameSb.append(currentName);
    	nameSb.append("_");
    	currentName = nameSb.toString().toLowerCase();
    	ArrayList<Boolean> tripIdsUsed = new ArrayList<Boolean>();

        if (cursor == null) {
        	nameSb.append("1");
        }
        else {
            if (cursor.moveToFirst()) {

                String name; 
                String idStr;
                int id;
                int nameColumn = cursor.getColumnIndex(DbConst.NAME); 
            
                do {
                    name = cursor.getString(nameColumn);
                    if (name != null) {
                    	name = name.toLowerCase();
                    	if (name.startsWith(currentName) && name.length() > currentName.length()) {
                    		idStr = name.substring(currentName.length());
                    		try {
								id = Integer.parseInt(idStr);
							} catch (NumberFormatException e) {
								// Not a number, so we don't care
								continue;
							}
							// Make sure there are enough entries in the array to contain the new ID bucket
							if (id >= tripIdsUsed.size()) {
								for (int ndx = tripIdsUsed.size(); ndx <= id; ndx++) {
									tripIdsUsed.add(Boolean.FALSE);
								}
								tripIdsUsed.set(id, Boolean.TRUE);
							}
                    	}
                    }
                    
                } while (cursor.moveToNext());

                if (tripIdsUsed.size() == 0) {
                	nameSb.append("1");
                }
                else {
	                // Search the buckets we created for an unused slot.
	                boolean found = false;
	                for (int ndx = 1; ndx < tripIdsUsed.size(); ndx++) {
						boolean used = tripIdsUsed.get(ndx);
						if (!used) {
							found = true;
				        	nameSb.append(ndx);
							break;
						}
					}
	                // Nothing unused, so add one to the highest used
	                if (!found) {
			        	nameSb.append(tripIdsUsed.size());
	                }
                }
            }
            else {
            	nameSb.append("1");
            }
        }
    	cursor.close();

        return nameSb.toString();
    }

    public static int backupDb(TripTableDb tripTableDb, BackupWaypointTableDb waypointTableDb) throws IOException {
    	WaypointTable[] waypoints;
    	WaypointData waypointData;
    	
    	TripTable[] tripTables = tripTableDb.getTripList(false);
    	TripData[] trips = TripData.convert(tripTables);
    	for (TripData tripData : trips) {
			waypoints = waypointTableDb.getWaypointList(tripData.getTripTable().getId());
			for (WaypointTable waypointTable : waypoints) {
				waypointData = new WaypointData(waypointTable);
				tripData.addWaypoint(waypointData);			}
		}
    	
//    	String xml = TripData.serializeTripsToXml(trips);
//    	Log.i(WhereWereWe.class.getName(),xml);
    	
    	File backupFile = FileUtilities.createFile(getBackupFilePath());
    	if (backupFile == null) {
    		return -1;
    	}
    	else {
    		FileWriter fileWriter;
			fileWriter = new FileWriter(backupFile);
    		TripData.serializeTripsToXmlWriter(fileWriter, trips, false);
    		fileWriter.close();
    	}
    	
    	return trips.length;
    }

    public static String getBackupFilePath() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(Environment.getExternalStorageDirectory());
    	sb.append(DbConst.EXTERNAL_FILE_STORE);
    	sb.append("/");
    	sb.append("wwwdb.xml");
    	
    	return sb.toString();
    }

    public static String[] importTrips(String xml, TripTableDb tripTableDb, WaypointTableDb waypointTableDb) {

    	TripData[] trips = TripData.deserializeXmlTrips(xml);
    	TripTable trip;
    	long tripId;
    	ArrayList<String> tripsAdded = new ArrayList<String>();

    	for (TripData tripData : trips) {
    		trip = tripTableDb.insertTrip(tripData.getTripTable());
    		if (trip != null) {
	    		tripsAdded.add(trip.getName());
	    		tripId = trip.getId();
	    		for (WaypointData waypointData : tripData.getWaypointList()) {
	    			waypointTableDb.insertWaypoint(waypointData.getWaypointTable(), tripId);
				}
    		}
		}
    	
    	String[] tripsAddedNames = new String[tripsAdded.size()];
    	tripsAddedNames = tripsAdded.toArray(tripsAddedNames);
    	return tripsAddedNames;
    }

    private static class WhereWereWeDbHelper extends SQLiteOpenHelper
    	implements DbConst {

    	private final DbAdapter dbAdapter;
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

    	public WhereWereWeDbHelper(DbAdapter dbAdapter, Context context) {
    		super(context, DB_NAME, null, DB_VERSION);
    		this.dbAdapter = dbAdapter;
    	}

        @Override
        public void onCreate(SQLiteDatabase db) {

        	db.execSQL(TripTableDb.CREATE_TABLE_CMD);
        	db.execSQL(SettingsTableDb.CREATE_TABLE_CMD);
        	db.execSQL(WaypointTableDb.CREATE_TABLE_CMD);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DbAdapter.class.getName(), "Upgrading Where Were We database from version " + oldVersion + " to "
                    + newVersion + ". Old data will be migrated to the new format.");

        	dbAdapter.setSqldb(db);

            TripTableDb tripTableDb = new TripTableDb(dbAdapter);
            BackupWaypointTableDb backupWaypointTableDb = new BackupWaypointTableDb(dbAdapter);
			try {
				int numTripsBackedUp = DbAdapter.backupDb(tripTableDb, backupWaypointTableDb);
				if (numTripsBackedUp > 0) {
					importRequiredDueToUpgrade = true;
					Log.i(DbAdapter.class.getName(), "WhereWereWe database successfully backed up " + numTripsBackedUp + " trips for upgrade.");
				}
			} catch (IOException e) {
				Log.e(DbAdapter.class.getName(), "IOException occurred while attempting to serialize the trips for upgrade", e);
			}
            
            db.execSQL("DROP TABLE IF EXISTS excursion;");

            db.execSQL(TripTableDb.DROP_TABLE_CMD);
        	db.execSQL(SettingsTableDb.DROP_TABLE_CMD);
        	db.execSQL(WaypointTableDb.DROP_TABLE_CMD);

            onCreate(db);
        }
   	
    }
}

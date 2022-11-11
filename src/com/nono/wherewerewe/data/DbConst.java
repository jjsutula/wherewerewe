package com.nono.wherewerewe.data;

public interface DbConst {

	public String DB_NAME = "wherewerewe.db";
	public int DB_VERSION = 8;

	public String EXTERNAL_FILE_STORE = "/nonono/WhereWereWe";

	public int SUSPENDED = 1;
	public int DELETED = 2;

	// Table names
	public String TRIP_TABLE = "trip";
	public String WAYPOINT_TABLE = "waypoint";
	public String SETTINGS_TABLE = "settings";
	public String DATASTORE_TABLE = "datastore";
	
	// Column names
	public String ADDRESS = "address";
	public String ALTITUDE = "altitude";
	public String COMMENT = "comment";
	public String CRUMB_NUM = "crumb_num";
	public String DATASTORE_ID = "datastore_id";
	public String DATASTORE_TYPE = "datastore_type";
	public String END_DATE = "end_date";
	public String INACTIVE = "inactive";
	public String KEY_ID = "_id";
	public String LAST_CRUMB = "last_crumb";
	public String LATITUDE = "latitude";
	public String LONGITUDE = "longitude";
	public String NAME = "name";
	public String PATH = "path";
	public String SETTING = "setting";
	public String SETTINGS_ID = "settings_id";
	public String START_DATE = "start_date";
	public String TRAIL = "trail";
	public String TRIP_ID = "trip_id";
	public String TYPE = "type";
	public String WAYPOINT_ID = "waypoint_id";
	public String WHEN = "when_occurred";
}

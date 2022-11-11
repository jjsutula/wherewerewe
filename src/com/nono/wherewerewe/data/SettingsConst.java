package com.nono.wherewerewe.data;

public interface SettingsConst {

	//********************** Keys ***********************
	public int MEASUREMENT_UNITS = 1;
	public int COORDINATE_UNITS = 2;
	public int CURRENT_TRIP = 3;
	public int CURRENT_WAYPOINT = 5;
	public int SHOW_COMPASS = 6;
	public int SHOW_NAVIGATOR = 7;
	public int LAST_MEDIA_STORE_LOCATION = 8;
	public int CRUMBS_WAYPOINT = 9;
	
	//********************* Values **********************
	// Measurement
	public String METRIC = "Metric";
	public String ENGLISH = "English";

	// Coordinates
	public String DECIMAL = "decimal";
	public String DMS = "dms";
	
	// Show things
	public String YES = "yes";
	public String NO = "no";

//
//	// Boolean values
//	public String TRUE = "1";
//	public String FALSE = "0";

}

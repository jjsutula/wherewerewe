package com.nono.wherewerewe.data;

import java.io.Serializable;

import com.nono.wherewerewe.data.table.TripTable;
import com.nono.wherewerewe.data.table.WaypointTable;

public class IntentWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

    private PreferenceData preferenceData = new PreferenceData();
	private TripTable tripTable = null;
	private WaypointTable waypointTable = null;

	/**
	 * @return the preferenceData
	 */
	public PreferenceData getPreferenceData() {
		return preferenceData;
	}
	/**
	 * @param preferenceData the preferenceData to set
	 */
	public void setPreferenceData(PreferenceData preferenceData) {
		this.preferenceData = preferenceData;
	}
	/**
	 * @return the tripTable
	 */
	public TripTable getTripTable() {
		return tripTable;
	}
	/**
	 * @param tripTable the tripTable to set
	 */
	public void setTripTable(TripTable tripTable) {
		this.tripTable = tripTable;
	}
	/**
	 * @return the waypointTable
	 */
	public WaypointTable getWaypointTable() {
		return waypointTable;
	}
	/**
	 * @param waypointTable the waypointTable to set
	 */
	public void setWaypointTable(WaypointTable waypointTable) {
		this.waypointTable = waypointTable;
	}
}

package com.nono.wherewerewe.data;

import java.io.Serializable;

public class PreferenceData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String m_measurement = SettingsConst.METRIC;
	private String m_coordinate = SettingsConst.DECIMAL;
	private boolean m_showCompass = true;
	private boolean m_showNavigator = true;
	private int m_compassSize = 4;
	private int m_navigatorSize = 4;
	
	/**
	 * @return whether the measurement is metric or not
	 */
	public boolean isMeasurementMetric() {
		return m_measurement == null || SettingsConst.METRIC.equalsIgnoreCase(m_measurement);
	}
	/**
	 * @return the measurement
	 */
	public String getMeasurement() {
		return m_measurement;
	}
	/**
	 * @param measurement the measurement to set
	 */
	public void setMeasurement(String measurement) {
		m_measurement = measurement;
	}
	/**
	 * @return whether the coordinate units are in decimal degrees or not
	 */
	public boolean isCoordinateDecimalDegrees() {
		return m_coordinate == null || SettingsConst.DECIMAL.equalsIgnoreCase(m_coordinate);
	}
	/**
	 * @return the coordinate
	 */
	public String getCoordinate() {
		return m_coordinate;
	}
	/**
	 * @param coordinate the coordinate to set
	 */
	public void setCoordinate(String coordinate) {
		m_coordinate = coordinate;
	}
	
	public void setCompassSize(int size) {
		m_compassSize = size;
	}
	
	public int getCompassSize() {
		return m_compassSize;
	}
	
	public void setCompassVisible(boolean visible) {
		m_showCompass = visible;
	}
	
	public boolean isCompassVisible() {
		return m_showCompass;
	}
	
	public void setNavigatorSize(int size) {
		m_navigatorSize = size;
	}
	
	public int getNavigatorSize() {
		return m_navigatorSize;
	}
	
	public void setNavigatorVisible(boolean visible) {
		m_showNavigator = visible;
	}
	
	public boolean isNavigatorVisible() {
		return m_showNavigator;
	}
}

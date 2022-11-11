package com.nono.data;

import com.nono.util.StringUtilities;

public class DbColumnCoordinate extends DbColumn {

	private static final int PRECISION = 6;

	private static final long serialVersionUID = 1L;
	private double nData = -1d;

	public DbColumnCoordinate(String name) {
		super(name);
		nData = -1d;
	}

	/**
	 * Clears all fields.
	 */
	@Override
	public void clear() {
		super.clear();
		nData = -1d;
	}

	/**
	 * Retrieves the data.
	 * @return the data
	 */
	public double getData() {
		return nData;
	}

	/**
	 * Retrieves the data.
	 * @return the data
	 */
	public String getDataStr() {
		return StringUtilities.formatData(nData, PRECISION);
	}

	/**
	 * Sets the data from a String.
	 * @param data the data to set
	 */
	public void setData(String data) {
		
		double f;
		if (data == null) {
			f = -1d;
		}
		else {
			try {
				f = Double.parseDouble(data);
				
			} catch (NumberFormatException e) {
				f = -1d;
			}
		}

		setData(f);
	}

	/**
	 * Sets the data.
	 * @param data the data to set
	 */
	public void setData(double data) {
		nData = data;
		setChanged();
	}

	@Override
	public String toString() {
		return StringUtilities.formatData(nData, PRECISION);
	}	
}

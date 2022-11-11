package com.nono.data;

import com.nono.util.StringUtilities;

public class DbColumnDouble extends DbColumn {

	private static final long serialVersionUID = 1L;
	private double data = -1;
	private static final int PRECISION = 6;

	public DbColumnDouble(String name) {
		super(name);
	}

	/**
	 * Clears all fields.
	 */
	@Override
	public void clear() {
		super.clear();
		data = -1;
	}

	/**
	 * Retrieves the data.
	 * @return the data
	 */
	public double getData() {
		return data;
	}

	/**
	 * Sets the data.
	 * @param data the data to set
	 */
	public void setData(double data) {
		this.data = data;
		setChanged();
	}	

	/**
	 * Sets the data.
	 * @param data the data to set
	 * @return True if the input was valid and the data was set.
	 */
	public boolean setStringData(String data) {
		
		if (data == null) {
			return false;
		}

		try {
			double num = Double.parseDouble(data);
			this.data = num;
			setChanged();
		} catch (NumberFormatException e) {
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		return StringUtilities.formatData(data, PRECISION);
	}	
}

package com.nono.data;

public class DbColumnInteger extends DbColumn {

	private static final long serialVersionUID = 1L;
	private int data = -1;

	public DbColumnInteger(String name) {
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
	public int getData() {
		return data;
	}

	/**
	 * Sets the data.
	 * @param data the data to set
	 */
	public void setData(int data) {
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
			int num = Integer.parseInt(data);
			this.data = num;
			setChanged();
		} catch (NumberFormatException e) {
			return false;
		}
		
		return true;
	}	

	@Override
	public String toString() {
		return Integer.toString(data);
	}	
}

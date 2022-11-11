package com.nono.data;


public class DbColumnLong extends DbColumn {

	private static final long serialVersionUID = 1L;
	private long data = -1;

	public DbColumnLong(String name) {
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
	public long getData() {
		return data;
	}

	/**
	 * Sets the data.
	 * @param data the data to set
	 */
	public void setData(long data) {
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
			long num = Long.parseLong(data);
			this.data = num;
			setChanged();
		} catch (NumberFormatException e) {
			return false;
		}
		
		return true;
	}	

	@Override
	public String toString() {
		return Long.toString(data);
	}	
}

package com.nono.data;

public class DbColumnText extends DbColumn {

	private static final long serialVersionUID = 1L;
	private String data = null;

	public DbColumnText(String name) {
		super(name);
	}

	/**
	 * Clears all fields.
	 */
	@Override
	public void clear() {
		super.clear();
		data = null;
	}

	/**
	 * Retrieves the data.
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	/**
	 * Sets the data.
	 * @param data the data to set
	 */
	public void setData(String data) {
		this.data = data;
		setChanged();
	}	

	@Override
	public String toString() {
		return data;
	}	
}

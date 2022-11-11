package com.nono.data;

import java.io.Serializable;

public abstract class DbColumn implements Cloneable, Serializable{

	private static final long serialVersionUID = 1L;
	private String name = null;
	private boolean changed = false;

	/**
	 * Constructor.
	 * @param name of the column.
	 */
	public DbColumn(String name) {
		this.name = name;
	}

	/**
	 * Standard clone method.
	 */
	public Object clone() {
		try {
			Object clone = super.clone();
			return clone;
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException("DbColumn.clone() exception");
		}
	}

	/**
	 * Retrieves the name of the column..
	 * @return the name of the column.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the column..
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Indicates whether the field has been changed.
	 * @return Whether the field has been changed.
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Indicates the field has been changed.
	 */
	public void setChanged() {
		changed = true;
	}
	
	/**
	 * Indicates the field has not been changed.
	 */
	public void resetChanged() {
		changed = false;
	}
	
	/**
	 * Clears all fields.
	 */
	public void clear() {
		name = null;
		changed = false;
	}
}

package com.nono.wherewerewe.data;

import android.content.ContentValues;

public abstract class DbTable {

	private boolean changed = false;

	/**
	 * Indicates whether the table has been changed.
	 * @return Whether the table has been changed.
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
	 * Clear all elements in the table.
	 */
	public abstract void clear();

	/**
	 * Indicate that all fields match the database values.
	 */
	public abstract void resetContentsChanged();

	/**
	 * Retrieve the content values of the table for update.
	 * @return the content values of the table for update.
	 */
	public abstract ContentValues getContentValues();
}

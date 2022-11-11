package com.nono.data;

import java.io.Serializable;

import com.nono.xml.SAXSerializable;

import android.content.ContentValues;

public abstract class DbTable implements Cloneable, Serializable, SAXSerializable {

	private static final long serialVersionUID = 1L;
	public final static short ACTION_NONE	= 1;
	public final static short ACTION_INSERT	= 2;
	public final static short ACTION_UPDATE	= 3;
	public final static short ACTION_DELETE	= 4;

	private short action = ACTION_NONE;

	/**
	 * Clear the record of any pending actions.
	 */
	public void markNoAction() {
		action = ACTION_NONE;
		setChildStatus(action);
	}

	/**
	 * Mark this record as needing to be inserted.
	 */
	public void markForInsert() {
		if (action == ACTION_NONE) {
			action = ACTION_INSERT;
		}
	}
	
	/**
	 * Mark this record as having updated.
	 */
	public void markForUpdate() {
		switch (action) {
		case ACTION_NONE:
		case ACTION_DELETE:
			action = ACTION_UPDATE;
			break;
		}
	}
	
	/**
	 * Mark this record for deletion.
	 */
	public void markForDelete() {
		switch (action) {
		case ACTION_NONE:
		case ACTION_UPDATE:
			action = ACTION_DELETE;
			setChildStatus(action);
			break;
		case ACTION_INSERT:
			System.out.println("Warning: Attempting to markForDelete() on an object that is currently markedForInsert()");
			action = ACTION_NONE;
			break;
		}
	}

	/**
	 * Is the record dirty. (i.e. is there an action pending).
	 */
	public boolean isDirty() {
		return action != ACTION_NONE;
	}
	
	/**
	 * Is the record marked as clean?
	 */
	public boolean isClean() {
		return action == ACTION_NONE;
	}

	/**
	 * Is the record marked for no action?
	 */
	public boolean isMarkedForNoAction() {
		return action == ACTION_NONE;
	}

	/**
	 * Is the record to be inserted?
	 */
	public boolean isMarkedForInsert() {
		return action == ACTION_INSERT;
	}
	
	/**
	 * Is the record to be updated?
	 */
	public boolean isMarkedForUpdate() {
		return action == ACTION_UPDATE;
	}

	/**
	 * Is the record to be deleted?
	 */
	public boolean isMarkedForDelete() {
		return action == ACTION_DELETE;
	}

	/**
	 * Get the current status. This will be one of the action code constants.
	 */
	public short getStatus() {
		return action;
	}

	/** 
	 * Set the current status code. This must be one of the action code
	 * constants.
	 */
	public void setStatus(short status) {
		action = status;
	}
	
	/**
	 * This method is used to determine if the children are dirty. This
	 * method must be overriden in the instance class if the object
	 * contains children objects. This method must return 'true' if one
	 * or more child is dirty and 'false' otherwise.
	 */
	public boolean isChildrenDirty() {
		return false;
	}
	
	/**
	 * Sets the status of the children of this object. This method must
	 * be overridden in the instance class if the object contains children.
	 */
	public void setChildStatus(short status) {
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
			throw new RuntimeException("DbTable.clone() exception");
		}
	}

	/**
	 * Clear all elements in the table.
	 */
	public abstract void clear();

	/**
	 * Retrieve the content values of the table for update.
	 * @return the content values of the table for update.
	 */
	public abstract ContentValues getContentValues();
}

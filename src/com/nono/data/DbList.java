package com.nono.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class DbList<T extends DbTable> extends ArrayList<DbTable> 
implements Cloneable, Serializable{

	private static final long serialVersionUID = 1L;

	/**
	 * Standard clone method, will perform a deep copy of the objects
	 */
	@SuppressWarnings("unchecked")
	public Object clone() {
		DbList clone = (DbList) super.clone();
		for (ListIterator it = clone.listIterator(); it.hasNext(); ) {
			DbTable item = (DbTable) it.next();
			it.set(item.clone());
		}

		return clone;
	}

	/**
	 * Is there any dirty data in this list?
	 *
	 * @return     Returns 'true' if any objects contained in the
	 * list are dirty.
	 */
	public boolean isDirty() {
		DbTable item = null;
		for (Iterator<DbTable> it = iterator(); it.hasNext(); ) {
			item = it.next();
			// Check the object itself
			if (item.isDirty())
				return true;
			// Now check the children of that object
			if (item.isChildrenDirty())
				return true;
		}
		return false;
	}
	
	/** 
	 * Set the status of each element in the collection. If the new status
	 * code is ACTION_NONE this will cause any items marked for Deletion
	 * to be removed from the list.
	 *
	 * @param status The new status code.
	 */
	public void setStatus(short status) {
	
		DbTable item = null;
		if (status == DbTable.ACTION_NONE) {
			for (Iterator<DbTable> it = iterator(); it.hasNext(); ) {
				item = it.next();
				if (item.isMarkedForDelete()) {
					it.remove();
				}
				else {
					item.markNoAction();
				}
			}
		}
		else {
			for (Iterator<DbTable> it = iterator(); it.hasNext(); ) {
				item = it.next();
				item.setStatus(status);
				item.setChildStatus(status);
			}
		}
	}
}

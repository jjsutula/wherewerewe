package com.nono.gui;

import java.io.Serializable;

public class MultiSelectListReturnRow implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;
	private boolean selected = false; 
	
	public MultiSelectListReturnRow(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean toggleSelected() {
		selected = !selected;
		return selected;
	}
}

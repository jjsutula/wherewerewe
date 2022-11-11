package com.nono.gui;

import java.io.Serializable;

public class MultiSelectListRow implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String text;
	private final int iconId;
	private final String value;
	
	public MultiSelectListRow(String text, int iconId, String value) {
		this.text = text;
		this.iconId = iconId;
		this.value = value;
	}

	public String getText() {
		return text;
	}

	public int getIconId() {
		return iconId;
	}

	public String getValue() {
		return value;
	}
}

package com.nono.gui;

import java.io.Serializable;

public class ConfirmBundle implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String path;
	private final String title;
	private final int type;

	/**
	 * Default constructor.
	 */
	public ConfirmBundle(String title, String path, int type) {
		this.title = title;
		this.path = path;
		this.type = type;
	}

	/**
	 * Retrieve the title of this dialog box.
	 * @return The title of this dialog box.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Retrieve the text.
	 * @return The text.
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Retrieve the id which indicates the type of item to confirm. This will be passed back in the result
	 * for the calling activity to match.
	 * @return The type.
	 */
	public int getType() {
		return type;
	}	
}

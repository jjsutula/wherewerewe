package com.nono.gui;

import java.io.Serializable;

public class ShowMediaPropertiesBundle implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String path;
	private final int type;

	/**
	 * Default constructor.
	 */
	public ShowMediaPropertiesBundle(String path, int type) {
		this.path = path;
		this.type = type;
	}

	/**
	 * Retrieve the path of the media file to play.
	 * @return The path of the media file to play.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Retrieve the type of the media file to play.
	 * @return The type of the media file to play.
	 */
	public int getType() {
		return type;
	}
}

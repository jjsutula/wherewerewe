package com.nono.gui;

import java.io.Serializable;

public class CommentBundle implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String text;
	private final String title;
	private int numLines;

	/**
	 * Default constructor.
	 */
	public CommentBundle(String title, String text, int numLines) {
		this.title = title;
		this.text = text;
		this.numLines = numLines;
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
	public String getText() {
		return text;
	}

	/**
	 * Retrieve the number of lines for the textbox to display.
	 * @return The number of lines for the textbox to display.
	 */
	public int getNumLines() {
		return numLines;
	}
}

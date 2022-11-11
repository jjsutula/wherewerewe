package com.nono.gui;

import java.io.Serializable;
import java.util.ArrayList;

public class IconListBundle implements Serializable {

	private static final long serialVersionUID = 1L;

	private ArrayList<IconListSelectRow> list;
	private final String buttonText;
	private final String title;

	String result;
	
	/**
	 * Default constructor.
	 */
	public IconListBundle(String title, String buttonText) {
		this.title = title;
		this.buttonText = buttonText;
		list = new ArrayList<IconListSelectRow>();
	}

	/**
	 * Add a text element. Use this to build lists with no corresponding icons. 
	 * @param text The text element.
	 * @param value The return value associated with the text element.
	 */
	public void add(String text, String value) {
		IconListSelectRow row = new IconListSelectRow(text, -1, value);
		list.add(row);
	}

	/**
	 * Add a text element along with a corresponding icon id.
	 * @param text The text element.
	 * @param iconID The  corresponding icon id.
	 * @param value The return value associated with the text element.
	 */
	public void add(String text, int iconID, String value) {
		IconListSelectRow row = new IconListSelectRow(text, iconID, value);
		list.add(row);
	}

	/**
	 * Retrieve the list of text and icon elements.
	 * @return the list of text and icon elements.
	 */
	public IconListSelectRow[] getList() {
		IconListSelectRow[] listArray = new IconListSelectRow[list.size()];
		listArray = list.toArray(listArray);

		return listArray;
	}

	/**
	 * Retrieve the title of this dialog box.
	 * @return The title of this dialog box.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Retrieve the text for the button displayed at the bottom of the list. If this is null
	 * no button is displayed.
	 * @return The text for the button displayed at the bottom of the list.
	 */
	public String getButtonText() {
		return buttonText;
	}

	/**
	 * Retrieve the result as selected from the list.
	 * @return The result as selected from the list. This will be null if the cancel button was pressed.
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Set the result when selected from the list.
	 * @param result The result when selected from the list.
	 */
	public void setResult(String result) {
		this.result = result;
	}
}

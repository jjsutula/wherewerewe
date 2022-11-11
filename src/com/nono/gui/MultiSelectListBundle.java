package com.nono.gui;

import java.io.Serializable;
import java.util.ArrayList;

public class MultiSelectListBundle implements Serializable {

	private static final long serialVersionUID = 1L;

	private ArrayList<MultiSelectListRow> list;
	private final String title;
	private final String okButtonText;
	private final int selectedIconID;
	
	/**
	 * Default constructor.
	 */
	public MultiSelectListBundle(String title, int selectedIconID, String okButtonText) {
		this.title = title;
		this.selectedIconID = selectedIconID;
		this.okButtonText = okButtonText;

		list = new ArrayList<MultiSelectListRow>();
	}

	/**
	 * Add a text element. Use this to build lists with no corresponding icons. 
	 * @param text The text element.
	 * @param value The return value associated with the text element.
	 */
	public void add(String text, String value) {
		MultiSelectListRow row = new MultiSelectListRow(text, -1, value);
		list.add(row);
	}

	/**
	 * Add a text element along with a corresponding icon id.
	 * @param text The text element.
	 * @param iconID The  corresponding icon id.
	 * @param value The return value associated with the text element.
	 */
	public void add(String text, int iconID, String value) {
		MultiSelectListRow row = new MultiSelectListRow(text, iconID, value);
		list.add(row);
	}

	/**
	 * Retrieve the list of text and icon elements.
	 * @return the list of text and icon elements.
	 */
	public MultiSelectListRow[] getList() {
		MultiSelectListRow[] listArray = new MultiSelectListRow[list.size()];
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

	public int getSelectedIconID() {
		return selectedIconID;
	}

	/**
	 * Retrieve the okButtonText to display. If null the default will display 'OK'.
	 * @return The okButtonText to display.
	 */
	public String getOkButtonText() {
		return okButtonText;
	}
}

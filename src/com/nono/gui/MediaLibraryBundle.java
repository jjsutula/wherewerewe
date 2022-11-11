package com.nono.gui;

import java.util.ArrayList;

public class MediaLibraryBundle extends MenuListBundle {

	private static final long serialVersionUID = 1L;

	private ArrayList<String> imagePathsNeedingThumbnailList = new ArrayList<String>();

	/**
	 * Not used but needed to support superclass relationship.
	 * @param title
	 * @param buttonText
	 * @param totalNumSubmenuItems
	 */
	public MediaLibraryBundle(String title, String buttonText, int totalNumSubmenuItems) {
		super(title, null, totalNumSubmenuItems);
		submenuItems = new MenuListSelectRow[totalNumSubmenuItems];
	}
	
	/**
	 * Main constructor.
	 * @param title
	 * @param buttonText
	 * @param totalNumSubmenuItems
	 */
	public MediaLibraryBundle(String title, int totalNumSubmenuItems) {
		super(title, null, totalNumSubmenuItems);
		submenuItems = new MenuListSelectRow[totalNumSubmenuItems];
	}

	/**
	 * Add a text element along with a corresponding icon id.
	 * @param text The text element.
	 * @param iconID The  corresponding icon id.
	 * @param value The return value associated with the text element.
	 * @param mediaType The type of media this row represents
	 */
	public void add(String text, int iconID, String value, int mediaType) {
		MediaLibrarySelectRow row = new MediaLibrarySelectRow(text, iconID, value, mediaType);
		addMenuItem(row);
	}

	/**
	 * Add a text element along with a corresponding icon id.
	 * @param text The text element.
	 * @param iconFilePath The path on the file system of the icon to display next to the text.
	 * @param value The return value associated with the text element.
	 * @param mediaType The type of media this row represents
	 */
	public void add(String text, String iconFilePath, String value, int mediaType) {
		MediaLibrarySelectRow row = new MediaLibrarySelectRow(text, iconFilePath, value, mediaType);
		addMenuItem(row);
	}

	public String[] getImagePathsNeedingThumbnail() {
		String[] imagePathsNeedingThumbnail = new String[imagePathsNeedingThumbnailList.size()];
		imagePathsNeedingThumbnail = imagePathsNeedingThumbnailList.toArray(imagePathsNeedingThumbnail);
		return imagePathsNeedingThumbnail;
	}

	public void addImagePathNeedingThumbnail(String imagePath) {
		imagePathsNeedingThumbnailList.add(imagePath);
	}


}

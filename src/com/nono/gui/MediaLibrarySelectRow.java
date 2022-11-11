package com.nono.gui;

public class MediaLibrarySelectRow extends MenuListSelectRow {

	private static final long serialVersionUID = 1L;
	private int mediaType = -1;

	/**
	 * Set up this menu item. This is a constructor to use if this is a leaf level menu,
	 * i.e. one that has no sub menu items.
	 * @param text Main text of the menu item
	 * @param iconId The icon to display next to the text
	 * @param value The value to return if this item is selected by the user
	 * @param mediaType The type of media this row represents
	 */
	public MediaLibrarySelectRow(String text, int iconId, String value, int mediaType) {
		super( text, iconId, value);
		this.mediaType = mediaType;
	}
	
	/**
	 * Set up this menu item. This is a constructor to use if this is a leaf level menu,
	 * i.e. one that has no sub menu items.
	 * @param text Main text of the menu item
	 * @param iconFilePath The path on the file system of the icon to display next to the text
	 * @param value The value to return if this item is selected by the user
	 * @param mediaType The type of media this row represents
	 */
	public MediaLibrarySelectRow(String text, String iconFilePath, String value, int mediaType) {
		super( text, iconFilePath, value);
		this.mediaType = mediaType;
	}

	/**
	 * Set up this menu item. This is the constructor to use if this is a tree level menu,
	 * i.e. one that has sub menu items.
	 * @param text Main text of the menu item
	 * @param totalNumSubmenuItems The number of sub menu items this tree will contain. This can be
	 * just an estimate, it will be used to preallocate space for the trees for performance purposes.
	 */
	public MediaLibrarySelectRow(String text, int totalNumSubmenuItems) {
		super( text, totalNumSubmenuItems);
	}

	public int getMediaType() {
		return mediaType;
	}

}

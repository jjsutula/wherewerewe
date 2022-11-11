package com.nono.gui;

import java.io.Serializable;

public class MenuListBundle implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String buttonText;
	private final String title;
	protected MenuListSelectRow[] submenuItems = null;
	protected int numSubmenuItems = 0;

	String result;

	/**
	 * Build the bundle.
	 * @param title The Dialog title.
	 * @param buttonText The text of the displayed button.
	 * @param totalNumSubmenuItems The number of sub menu items this tree will contain. This can be
	 * just an estimate, it will be used to preallocate space for the trees for performance purposes.
	 */
	public MenuListBundle(String title, String buttonText, int totalNumSubmenuItems) {
		this.title = title;
		this.buttonText = buttonText;
		if (totalNumSubmenuItems > 0) {
			submenuItems = new MenuListSelectRow[totalNumSubmenuItems];
		}
	}

	/**
	 * Retrieve the list of visible rows.
	 * @return
	 */
	MenuListSelectRow[] getVisibleRows() {
		int numVisibleItems = 0;
		// Calculate the number of visible rows
		for (int ndx = 0; ndx < numSubmenuItems; ndx++) {
			MenuListSelectRow row = submenuItems[ndx];
			numVisibleItems += row.getNumVisibleItems();
		}
		// Allocate the space
		MenuListSelectRow[] rows = new MenuListSelectRow[numVisibleItems];
		
		// Load the visible rows
		int position = 0;
		for (int ndx = 0; ndx < numSubmenuItems; ndx++) {
			MenuListSelectRow row = submenuItems[ndx];
			position = row.loadVisibleTable(rows, position);
		}

		return rows;
	}

	/**
	 * Get the item at the specified visible position
	 * @param position The position to retrieve.
	 * @return The item at the position. 
	 */
	public MenuListSelectRow get(int position) {
		int startAt = 0;
		for (int ndx = 0; ndx < numSubmenuItems; ndx++) {
			MenuListSelectRow row = submenuItems[ndx];
			MenuListSelectRow found = row.get(startAt, position);
			if (found == null) {
				startAt += row.getNumVisibleItems();
			}
			else {
				return found;
			}
		}

		return null;
	}

	/**
	 * Add a text element along with a corresponding icon id.
	 * @param text The text element.
	 * @param iconID The  corresponding icon id.
	 * @param value The return value associated with the text element.
	 */
	public void add(String text, int iconID, String value) {
		MenuListSelectRow row = new MenuListSelectRow(text, iconID, value);
		addMenuItem(row);
	}

	/**
	 * Add a text element along with a corresponding icon id.
	 * @param text The text element.
	 * @param iconFilePath The path on the file system of the icon to display next to the text.
	 * @param value The return value associated with the text element.
	 */
	public void add(String text, String iconFilePath, String value) {
		MenuListSelectRow row = new MenuListSelectRow(text, iconFilePath, value);
		addMenuItem(row);
	}

	/**
	 * Add a sub menu item.
	 * @param menuItem The menu item to add.
	 */
	public void addMenuItem(MenuListSelectRow menuItem) {
		int position = numSubmenuItems++;
		if (numSubmenuItems > submenuItems.length) {
			// Need to bump up the size of the sub menu array. Add 2 at a time as a balance
			// between taking up unneeded space and doing this crazy shuffle too often.
			MenuListSelectRow[] tempsubmenuItems = new MenuListSelectRow[submenuItems.length + 2];
			System.arraycopy(submenuItems, 0, tempsubmenuItems, 0, submenuItems.length);
			submenuItems = tempsubmenuItems;
		}
		submenuItems[position] = menuItem;
	}

	/**
	 * Remove a sub menu item.
	 * @param menuItem The menu item to remove.
	 */
	public void removeMenuItem(MenuListSelectRow menuItem) {
		for (int ndx = 0; ndx < numSubmenuItems; ndx++) {
			MenuListSelectRow row = submenuItems[ndx];
			if (row.equals(menuItem)) {
				// Slide every subsequent row back one slot
				for (int rowNdx = ndx; rowNdx < numSubmenuItems - 1; rowNdx++) {
					submenuItems[rowNdx] = submenuItems[rowNdx + 1];
				}
				numSubmenuItems--;
				submenuItems[numSubmenuItems] = null;
				break;
			}
		}
	}
	/**
	 * Retrieve the position of the input row.
	 * @param row The row to search the position for.
	 * @return The position of the row if a match is found, or -1 if
	 * the desired row is not found.
	 */
	public int getPosition(MenuListSelectRow row) {
		int startAt = 0;
		for (int ndx = 0; ndx < numSubmenuItems; ndx++) {
			MenuListSelectRow subMenuRow = submenuItems[ndx];
			int position = subMenuRow.getPosition(startAt, row);
			if (position > -1) {
				return position;
			}
			else {
				startAt += subMenuRow.getNumVisibleItems();
			}
		}

		return -1;
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

package com.nono.gui;

import java.io.Serializable;

import com.nono.wherewerewe.R;

public class MenuListSelectRow implements Serializable, Comparable<MenuListSelectRow> {

	private static final long serialVersionUID = 1L;

	private boolean expanded = false;
	private MenuListSelectRow[] submenuItems;
	private int numSubmenuItems = 0;
	private int numVisibleItems = 1;
	private final String text;
	private int iconId = -1;
	private String iconFilePath = null;
	private final String value;
	private int depth = 0;
	
	/**
	 * Set up this menu item. This is a constructor to use if this is a leaf level menu,
	 * i.e. one that has no sub menu items.
	 * @param text Main text of the menu item
	 * @param iconId The icon to display next to the text
	 * @param value The value to return if this item is selected by the user
	 */
	public MenuListSelectRow(String text, int iconId, String value) {
		this.text = text;
		this.iconId = iconId;
		this.value = value;
		submenuItems = new MenuListSelectRow[0];
	}
	
	/**
	 * Set up this menu item. This is a constructor to use if this is a leaf level menu,
	 * i.e. one that has no sub menu items.
	 * @param text Main text of the menu item
	 * @param iconFilePath The path on the file system of the icon to display next to the text
	 * @param value The value to return if this item is selected by the user
	 */
	public MenuListSelectRow(String text, String iconFilePath, String value) {
		this.text = text;
		this.iconFilePath = iconFilePath;
		this.value = value;
		submenuItems = new MenuListSelectRow[0];
	}

	/**
	 * Set up this menu item. This is the constructor to use if this is a tree level menu,
	 * i.e. one that has sub menu items.
	 * @param text Main text of the menu item
	 * @param totalNumSubmenuItems The number of sub menu items this tree will contain. This can be
	 * just an estimate, it will be used to preallocate space for the trees for performance purposes.
	 */
	public MenuListSelectRow(String text, int totalNumSubmenuItems) {
		this.text = text;
		this.value = null;
		submenuItems = new MenuListSelectRow[totalNumSubmenuItems];
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
		menuItem.setDepth(depth+1);
		calculateNumVisibleItems();
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
				calculateNumVisibleItems();
				break;
			}
		}
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
	 * Retrieve whether or not this menu is a leaf node.
	 * @return True if this menu has no sub menus.
	 */
	public boolean isLeaf() {
		return numSubmenuItems == 0 ? true : false;
	}

	/**
	 * Toggles the expanded state of this menu.
	 */
	public void toggleExpanded() {
		expanded = !expanded;
		calculateNumVisibleItems();
	}
	
	/**
	 * Retrieve the text of this menu item
	 * @return The text of this menu item
	 */
	public String getText() {
		return text;
	}

	/**
	 * Retrieve the icon for this menu item
	 * @return The icon for this menu item
	 */
	public int getIconId() {
		if (isLeaf()) {
			return iconId;
		}
		else if (expanded) {
			return R.drawable.menu_expanded;
		}
		else {
			return R.drawable.menu_collapsed;
		}
	}

	/**
	 * Retrieve the icon for this menu item
	 * @return The icon for this menu item
	 */
	public String getIconFilePath() {
		return iconFilePath;
	}

	/**
	 * Reset the icon for this menu item.
	 * @param filePath The new icon for this menu item.
	 */
	public void setIconFilePath(String filePath) {
		iconFilePath = filePath;
		iconId = -1;
	}

	/**
	 * Retrieve the value of this menu item
	 * @return The value of this menu item
	 */
	public String getValue() {
		return value;
	}

	protected int loadVisibleTable(MenuListSelectRow[] rows, int position) {
		rows[position++] = this;
		if (expanded) {
			for (int ndx = 0; ndx < numSubmenuItems; ndx++) {
				MenuListSelectRow row = submenuItems[ndx];
				position = row.loadVisibleTable(rows, position);
			}
		}
		return position;
	}

	/**
	 * Retrieve the position of the input row.
	 * @param startAt The offset to use when calculating the position. Basically, this is the
	 * number of visible rows before this item in the list.
	 * @param row The row to search the position for.
	 * @return The position of the row if a match is found, or -1 if
	 * the desired row is not this menu row or a submenu row.
	 */
	protected int getPosition(int startAt, MenuListSelectRow row) {
		if (this == row) {
			return startAt;
		}
		else  {
			startAt++;
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
		}

		return -1;
	}

	/**
	 * Get the item at the specified visible position
	 * @param startAt The beginning range to start looking
	 * @param position The position to retrieve.
	 * @return The item at the position, if this menu or its sub menus
	 * lies within the range. Otherwise return null. 
	 */
	protected MenuListSelectRow get(int startAt, int position) {
		if (position == startAt) {
			return this;
		}
		else  {
			if (position < startAt + numVisibleItems) {
				startAt += 1;
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
			}
		}

		return null;
	}

	/**
	 * Inform each row of its depth.
	 * @param depth The depth of this row.
	 */
	protected void setDepth(int depth) {
		this.depth = depth;
		int childDepth = depth + 1;
		for (int ndx = 0; ndx < numSubmenuItems; ndx++) {
			MenuListSelectRow row = submenuItems[ndx];
			row.setDepth(childDepth);
		}
	}

	/**
	 * Retrieve the depth of this row.
	 * @return The depth of this row.
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Retrieve the number of sub menu items that are visible. This does take into account
	 * whether the sub menus are expanded.
	 * @return
	 */
	protected int getNumVisibleItems() {
		return numVisibleItems;
	}

	private void calculateNumVisibleItems() {
		numVisibleItems = 1;
		if (expanded) {
			for (int ndx = 0; ndx < numSubmenuItems; ndx++) {
				MenuListSelectRow row = submenuItems[ndx];
				numVisibleItems += row.getNumVisibleItems();
			}
		}
	}

	@Override
	public String toString() {
		return text;
	}

	@Override
	public int compareTo(MenuListSelectRow another) {
		if (another == null) {
			return 1;
		}
		return text.compareTo(another.getText());
	}
	
}

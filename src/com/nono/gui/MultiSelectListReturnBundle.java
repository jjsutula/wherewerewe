package com.nono.gui;

import java.io.Serializable;

public class MultiSelectListReturnBundle implements Serializable {

	private static final long serialVersionUID = 1L;

	private MultiSelectListReturnRow[] list;

	/**
	 * Default constructor.
	 */
	public MultiSelectListReturnBundle(MultiSelectListBundle inputBundle) {
		if (inputBundle == null) {
			list = new MultiSelectListReturnRow[0];
		}
		else {
			MultiSelectListRow[] inputList = inputBundle.getList();
			list = new MultiSelectListReturnRow[inputList.length];
			for (int ndx = 0; ndx < inputList.length; ndx++) {
				MultiSelectListRow inputRow = inputList[ndx];
				list[ndx] = new MultiSelectListReturnRow(inputRow.getValue());;
			}
		}
	}

	/**
	 * Retrieve the list of text and icon elements.
	 * @return the list of text and icon elements.
	 */
	public MultiSelectListReturnRow[] getList() {

		return list;
	}
}

package com.nono.util;

public interface ThumbnailListener {

	/**
	 * Indicates that a thumbnail was successfully created
	 * @param imagePath
	 * @param thumbnailPath
	 * @param rotated
	 */
	public void thumbnailCreated(String imagePath, String thumbnailPath, int rotated);
}

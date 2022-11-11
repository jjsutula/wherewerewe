package com.nono.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

/**
 * This class contains no public methods. To invoke it use the MediaUtilities class. 
 * @see MediaUtilities
 */
public class ThumbnailCreator {

	private final ThumbnailListener thumbnailListener;
	private static AtomicBoolean busyFlag = new AtomicBoolean();

	private final static int DO_NOT_ROTATE = 0;
	private final static int CHECK_ORIENTATION = -1;

	protected ThumbnailCreator() {
		thumbnailListener = null; 
	}

	protected ThumbnailCreator(ThumbnailListener thumbnailListener) {
		this.thumbnailListener = thumbnailListener;
	}

	protected void createThumbnailInBackground(String imagePath) {
		String[] imagePaths = {imagePath};

		// Loading images takes a lot of memory so only allow one thread to exist at a time.
		// If we get denied, the images will have to be loaded next time the user tries.
		if (busyFlag.compareAndSet(false, true)) {
			BackgroundThread backgroundThread = new BackgroundThread(thumbnailListener, imagePaths, CHECK_ORIENTATION);
			backgroundThread.start();
		}
		else {
			Log.w(ThumbnailCreator.class.getName(), "Attempt to create thumbnails in " + imagePath + " aborted because thread is already busy. Try again next time.");
		}
	}
	
	protected void createThumbnailsInBackground(String[] imagePaths) {
		// Loading images takes a lot of memory so only allow one thread to exist at a time.
		// If we get denied, the images will have to be loaded next time the user tries.
		if (busyFlag.compareAndSet(false, true)) {
			BackgroundThread backgroundThread = new BackgroundThread(thumbnailListener, imagePaths, 0);
			backgroundThread.start();
		}
		else {
			Log.w(ThumbnailCreator.class.getName(), "Attempt to create thumbnails in " + imagePaths + " aborted because thread is already busy. Try again next time.");
		}
	}

	protected void rotateImageAndCreateThumbnail(String imagePath, int rotateDegrees) {
		String[] imagePaths = {imagePath};

		// Loading images takes a lot of memory so only allow one thread to exist at a time.
		// If we get denied, the images will have to be loaded next time the user tries.
		if (busyFlag.compareAndSet(false, true)) {
			BackgroundThread backgroundThread = new BackgroundThread(thumbnailListener, imagePaths, rotateDegrees);
			backgroundThread.start();
		}
		else {
			Log.w(ThumbnailCreator.class.getName(), "Attempt to rotate and create thumbnails in " + imagePath + " aborted because thread is already busy. Try again next time.");
		}
	}
	
	protected static class BackgroundThread extends Thread {
		
		private final String[] imagePaths;
		private String currentPath = null;
		private String currentThumbnailPath = null;
		private int currentImageRotated = 0;
		private final ThumbnailListener thumbnailListener;
		private final int rotateDegrees;

		/**
		 * Create a background thread to generate a set of thumbnails.
		 * @param thumbnailListener A listener to be notified when each thumbnail is created.
		 * @param imagePaths The paths for which to create the thumbnails.
		 * @param rotateDegrees The amount to rotate the original image, or -1 if it should be automatically rotated 
		 * based upon its current orientation.
		 */
		BackgroundThread(ThumbnailListener thumbnailListener, String[] imagePaths, int rotateDegrees) {
			this.thumbnailListener = thumbnailListener;
			this.imagePaths = imagePaths;
			this.rotateDegrees = rotateDegrees;
		}

		@Override
		public void run() {
			
			try {
				for (String imagePath : imagePaths) {
					currentPath = imagePath;
					if (rotateDegrees != 0) {
				    	currentThumbnailPath = MediaUtilities.getThumbnailPath(imagePath);
				    	if (currentThumbnailPath != null) {
							File thumb = new File(currentThumbnailPath);
				    		thumb.delete();
				    		currentThumbnailPath = null;
				    	}
					}
					createThumbnail(currentPath);
					if (thumbnailListener != null) {
						try {
							thumbnailListener.thumbnailCreated(currentPath, currentThumbnailPath, currentImageRotated);
							Log.i(ThumbnailCreator.class.getName(), "Thumbnail successfully created for " + imagePath + ".");
						} catch (Exception e) {} // Try/catch handles the case where calling activity went away. It's OK, in that case they don't need to be notified anyway.
					}
				}
			} catch (IOException e) {
				Log.e(ThumbnailCreator.class.getName(), "IOException occurred while attempting to create a thumbnail for " + currentPath, e);
			} finally {
				busyFlag.set(false);
			}
		}
		
		/**
		 * Create the thumbnail if it does not already exist.
		 * This will also correct the image orientation if needed.
		 * @param imagePath The full path to the image.
		 * @throws IOException
		 */
	    private void createThumbnail(String imagePath)
	    	throws IOException {

	    	currentThumbnailPath = null;
	    	currentImageRotated = 0;
	    	currentThumbnailPath = MediaUtilities.buildThumbnailPath(imagePath, true);
			if (currentThumbnailPath != null) {
				File thumb = new File(currentThumbnailPath);
				if (!thumb.exists()) {
					currentImageRotated = checkOrientation(imagePath, rotateDegrees);
				    BitmapFactory.Options bfOptions=new BitmapFactory.Options();
				    bfOptions.inDither=false;                     //No dithering, baby
				    bfOptions.inPurgeable=true;                   //Tell gc that the Bitmap can be cleared
				    bfOptions.inInputShareable=true;              //Allow the bitmap to share a reference to the input data 
				    bfOptions.inTempStorage=new byte[32 * 1024]; 
		
				    FileInputStream fs = null;
		    		File imageFile = new File(imagePath);
		
				    Bitmap imageBitmap = null;
				    try {
				        fs = new FileInputStream(imageFile);
				        if (fs != null) {
				        	imageBitmap=BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
				        }
				    } finally{ 
				        if (fs != null) {
				            try {
				            	fs.close();
				            } catch (IOException e) {}
				        }
				    }
				    if (imageBitmap != null) {
				    	Proportion proportion = getThumbnailProportion(imageBitmap.getWidth(), imageBitmap.getHeight());
			    		imageBitmap = Bitmap.createScaledBitmap(imageBitmap, proportion.getWidth(), proportion.getHeight(), false);
						FileOutputStream fOut = new FileOutputStream(thumb);
						imageBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fOut);
			            fOut.flush();
			            fOut.close();
						imageBitmap.recycle();
						imageBitmap = null;
				    }
				}
			}
		}

	    /**
		 * Flip the orientation if needed so the picture does not appear sideways or upside down.
		 * @param imagePath The full path to the image file
		 */
		private int checkOrientation(String imagePath, int rotateDegrees) {
		
			// Determine the file type to make sure it's supported
			if (imagePath == null) {
				return 0;
			}
			
			if (rotateDegrees == DO_NOT_ROTATE) {
				return 0;
			}

			// Examine the Exif data to check the orientation
			int rotated = 0;
		    try {
		    	if (rotateDegrees == CHECK_ORIENTATION) {
			    	rotateDegrees = 0;
			    	ExifInterface exif = new ExifInterface(imagePath);
			    	int exifOrientation = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
			    	switch (exifOrientation){
			    	case ExifInterface.ORIENTATION_ROTATE_90:
			    		rotateDegrees = 90; // Rotate this 90 degrees right
			    	    break;
			    	case ExifInterface.ORIENTATION_ROTATE_180:
			    		rotateDegrees = 180; // Rotate this 180 degrees
			    	    break;
			    	case ExifInterface.ORIENTATION_ROTATE_270:
			    		rotateDegrees = 270; // Rotate this 90 degrees left
			    	    break;
			    	}
		    	}
		    	if (rotateDegrees > 0){
		    		rotated = rotateImage(imagePath, rotateDegrees);
		    	}
			} catch (Exception e) {
				Log.e(ThumbnailCreator.class.getName(), "Exception occurred while attempting to correct the orientation for " + imagePath, e);
			}
		
			return rotated;
		}
		
		/**
		 * Rotate an image orientation.
		 * @param imagePath The full path of the image.
		 * @param rotateDegrees The degrees to rotate
		 * @return The degrees that the image was actually rotated. This will be 0 if the operation failed.
		 */
		private static int rotateImage(String imagePath, int rotateDegrees) {
			int TYPE_JPEG = 0;
//			int TYPE_PNG = 1;
			int type = -1;
			int rotated = 0;
			Bitmap imageBitmap = null;

			if (rotateDegrees == 0) {
				return 0;
			}

			int ndx = imagePath.lastIndexOf('.');
			if (ndx > -1 && imagePath.length() > ndx + 1) {
				String suffix = imagePath.substring(ndx + 1).toLowerCase();
				if ("jpg".equals(suffix) || "jpeg".equals(suffix)) {
					type = TYPE_JPEG;
				}
				else if ("png".equals(suffix)) {
					// type = TYPE_PNG;    // At this time ANdroid does not read Exif information for PNGs, so just return
				}
			}
			if (type < 0) {
				return 0;
			}

			try {
				BitmapFactory.Options bfOptions=new BitmapFactory.Options();
			    bfOptions.inDither=false;                     //Still no dithering
			    bfOptions.inPurgeable=true;                   //Tell gc that the Bitmap can be cleared
			    bfOptions.inInputShareable=true;              //Allow the bitmap to share a reference to the input data 
			    bfOptions.inTempStorage=new byte[32 * 1024]; 

			    FileInputStream fs;
	    		File imageFile = new File(imagePath);
		        fs = new FileInputStream(imageFile);
			    try {
			        if (fs != null) {
			        	imageBitmap=BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
			        }
			    } finally{ 
			        if (fs != null) {
			            try {
			            	fs.close();
			            } catch (IOException e) {}
			        }
			    }
			    if (imageBitmap != null) {
					Matrix matrix = new Matrix();
					matrix.postRotate((float)rotateDegrees);
					int width = imageBitmap.getWidth();
					int height = imageBitmap.getHeight();
					imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, width, height, matrix, true);
					File file = new File(imagePath);
					FileOutputStream outStream = new FileOutputStream(file);
					boolean saved = imageBitmap.compress(type == TYPE_JPEG ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG, 100, outStream);
					imageBitmap.recycle();
					imageBitmap = null;
					if (saved) {
						rotated = rotateDegrees;
						Log.i(MediaUtilities.class.getName(), "Image " + imagePath + " rotated " + rotated + " degrees.");
					}
					else {
						Log.e(MediaUtilities.class.getName(), "Attempted to rotate the orientation by " + rotateDegrees + " degrees for " + imagePath + " but the file did not save.");
					}
			    }
			} catch (Exception e) {
				Log.e(MediaUtilities.class.getName(), "Exception occurred while attempting to rotate the orientation by " + rotateDegrees + " degrees for " + imagePath, e);
			}
		    
		    return rotated;
		}

		private Proportion getThumbnailProportion(int imageWidth, int imageHeight) {			
			float ratio;
			
			Proportion proportion;
			if (imageWidth > imageHeight) {
				ratio = (float)((float)imageWidth / (float)imageHeight);
				proportion = new Proportion(MediaUtilities.THUMBNAIL_LONG_EDGE, (int)(MediaUtilities.THUMBNAIL_LONG_EDGE / ratio));
			}
			else {
				ratio = (float)((float)imageHeight / (float)imageWidth);
				proportion = new Proportion((int)(MediaUtilities.THUMBNAIL_LONG_EDGE / ratio), MediaUtilities.THUMBNAIL_LONG_EDGE);
			}
			
			
			return proportion;
		}
	}
}

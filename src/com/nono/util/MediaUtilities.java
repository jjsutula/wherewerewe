package com.nono.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.util.Log;

import com.nono.wherewerewe.WhereWereWe;

public class MediaUtilities {

	public static final int THUMBNAIL_HEIGHT = 48;
	public static final int THUMBNAIL_WIDTH = 66;
	public static final int THUMBNAIL_LONG_EDGE = 66;

	/**
	 * Create a thumbnail in the background.
	 * This will also correct the image orientation if needed.
	 * @param imagePath The image path from which to create the thumbnail.
	 */
	public static void createThumbnailInBackground(String imagePath) {
    	ThumbnailCreator thumbnailCreator = new ThumbnailCreator();
    	thumbnailCreator.createThumbnailInBackground(imagePath);
	}

	/**
	 * Create a thumbnail in the background.
	 * This will also correct the image orientation if needed.
	 * @param imagePaths The image path from which to create the thumbnail.
	 * @param thumbnailListener A listener to be notified when each thumbnail is created.
	 */
	public static void createThumbnailsInBackground(String[] imagePaths, ThumbnailListener thumbnailListener) {
    	ThumbnailCreator thumbnailCreator = new ThumbnailCreator(thumbnailListener);
    	thumbnailCreator.createThumbnailsInBackground(imagePaths);
	}

	/**
	 * Retrieve the thumbnail if it already exists.
	 * @param imagePath The full path to the image.
	 * @return The path of the thumbnail for the image, or null if it does not exist.
	 */
    public static String getThumbnailPath(String imagePath) {

    	String thumbPath = buildThumbnailPath(imagePath, false);
		if (thumbPath != null) {
			File thumb = new File(thumbPath);
			if (!thumb.exists()) {
				thumbPath = null;
			}
		}
		
		return thumbPath;
	}

    /**
     * Rotate the image and create a new thumbnail. If a thumbnail currently exists
     * it will be replaced.
     * @param imagePath The path of the image to rotate.
     * @param rotateDegrees The number of degrees to rotate the image.
	 * @param thumbnailListener A listener to be notified when each thumbnail is created.
     */
	public static void rotateImageAndCreateThumbnail(String imagePath, int rotateDegrees, ThumbnailListener thumbnailListener) {
    	ThumbnailCreator thumbnailCreator = new ThumbnailCreator(thumbnailListener);
    	thumbnailCreator.rotateImageAndCreateThumbnail(imagePath, rotateDegrees);
	}

	/**
	 * Build the thumbnail name.
	 * @param imagePath The full path to the image.
	 * @param createDirsIfMissing Create the directory path they do not exist.
	 * @return The full path name of the thumbnail for the image.
	 */
    public static String buildThumbnailPath(String imagePath, boolean createDirsIfMissing) {
    	String thumbPath = null;
    	if (imagePath != null) {
			int imageNdx = imagePath.lastIndexOf("/");
			if (imageNdx < 0) {
				// No directory info so use the whole string as the file name
				imageNdx = imagePath.length() - 1;
			}

			StringBuilder sb = new StringBuilder();
			sb.append(imagePath.substring(0, imageNdx + 1));
			sb.append("thumbs");
			File thumbdir = new File(sb.toString());
    		if (!thumbdir.exists()) {
    			if (createDirsIfMissing) {
	    			thumbdir.mkdirs();
	    		}
    			else {
    				return null;
    			}
			}
			sb.append("/th_");
			int lastDotNdx = imagePath.lastIndexOf(".");
			if (lastDotNdx < 0) {
				lastDotNdx = imagePath.length();
			}
			sb.append(imagePath.substring(imageNdx + 1, lastDotNdx));
			sb.append(".jpg");
			thumbPath = sb.toString();
    	}
    	
    	return thumbPath;
	}  
	
    public static void removeThumbnail(String imagePath) {
    	String thumbPath = getThumbnailPath(imagePath);
    	if (thumbPath != null) {
    		File thumbFile = new File(thumbPath);
    		if(thumbFile.exists()) {
    			thumbFile.delete();
    		}
    	}
    }

	/**
	 * Create a bitmap from a file.
	 * @param filePath The image path.
	 * @param viewHeight The desired height to display.
	 * @param viewWidth The desired width to display.
	 * @return The prepared bitmap.
	 */
	public static Bitmap prepareBitmap(String filePath, int viewHeight, int viewWidth){

    	Bitmap bm = null;

    	File f = new File(filePath);

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
        	fis = new FileInputStream(f);
        	BitmapFactory.decodeStream(fis, null, o);
        } catch (IOException e) {
			Log.e(MediaUtilities.class.getName(), "IOException occurred during decode scaled bitmap while attempting to display " + f.getAbsolutePath(), e);
        } finally {
            try {
            	if( fis != null) {
            		fis.close();
            	}
            } catch (IOException e) {}
        }

        int scale = 1;
        if (viewHeight > 0 && viewWidth > 0) {
	        if (o.outHeight > viewHeight || o.outWidth > viewWidth) {
	            scale = (int) Math.pow(2, (int) Math.round(Math.log(viewHeight / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
	        }
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = null;
        try {
        	fis = new FileInputStream(f);
        	bm = BitmapFactory.decodeStream(fis, null, o2);
        } catch (IOException e) {
			Log.e(MediaUtilities.class.getName(), "IOException occurred during decode scaled bitmap while attempting to display " + f.getAbsolutePath(), e);
        } finally {
            try {
            	if( fis != null) {
            		fis.close();
            	}
            } catch (IOException e) {}
        }
        
        return bm;
    }
	
    public static String buildExifDisplay(String filePath){
    	StringBuilder exif= new StringBuilder();
    	
//    	Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

    	/* Now we can retrieve all display-related infos */
//    	int width = display.getWidth();
//    	int height = display.getHeight();
//    	int orientation = display.getOrientation();
    	
//    	exif.append("Exif: ");
//    	exif.append(filePath);
//    	exif.append("\nDevice orientation: ");
//    	exif.append(orientation);
    	try {
    		ExifInterface exifInterface = new ExifInterface(filePath);
    
    		appendNonNull(exif, "IMAGE_LENGTH", exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
    		appendNonNull(exif, "IMAGE_WIDTH", exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
    		appendNonNull(exif, "DATETIME", exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
    		appendNonNull(exif, "MAKE", exifInterface.getAttribute(ExifInterface.TAG_MAKE));
    		appendNonNull(exif, "MODEL", exifInterface.getAttribute(ExifInterface.TAG_MODEL));
    		appendNonNull(exif, "ORIENTATION", exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
    		appendNonNull(exif, "WHITE_BALANCE", exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE));
//    		appendNonNull(exif, "FOCAL_LENGTH", exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
    		appendNonNull(exif, "FLASH", exifInterface.getAttribute(ExifInterface.TAG_FLASH));

    		appendNonNull(exif, "GPS_LATITUDE", exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
    		appendNonNull(exif, "GPS_LATITUDE_REF", exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
    		appendNonNull(exif, "GPS_LONGITUDE", exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
    		appendNonNull(exif, "GPS_LONGITUDE_REF", exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
//    		appendNonNull(exif, "GPS_PROCESSING_METHOD", exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD));
    	} catch (IOException e) {
			Log.e(WhereWereWe.class.getName(), "IOException occurred while attempting to read the EXIF data for the image at " + filePath, e);
	    	exif.append("\nIOException occurred while attempting to read the EXIF data for the image. Error was:\n");
	    	exif.append(e);
    	}

//    	displayComments(exif.toString());
    	return exif.toString();
    }    
    
    private static void appendNonNull(StringBuilder sb, String tagName, String attr) {
		if (attr != null && !attr.equals("0")) {
			sb.append("\n");
			sb.append(tagName);
			sb.append(attr);
		}

    }
}

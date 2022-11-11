package com.nono.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.nono.wherewerewe.data.DbConst;

import android.os.Environment;
import android.util.Log;

public class FileUtilities {

	public static final int LOCATION_AUDIO = 0;
	public static final int LOCATION_IMAGE = 1;
	public static final int LOCATION_VIDEO = 2;

	public static final String AUDIO_FOLDER = "/audio";
	public static final String IMAGE_FOLDER = "/image";
	public static final String VIDEO_FOLDER = "/video";
	
	private static final char[] RESERVED_CHARS = {'|','%','\\','?','*','<','\"',':','>','+','[',']','/','\'','*','.','@','#','$','&','\t','\r','\n'};

    public static String getFilePath(int type, String rootLocation) {
    	return getFilePath(type, rootLocation, null);
    }

    public static String getFilePath(int type, String rootLocation, String fileName) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(rootLocation);
    	switch(type) {
    	case LOCATION_AUDIO:
        	sb.append(AUDIO_FOLDER);
    		break;
    	case LOCATION_IMAGE:
        	sb.append(IMAGE_FOLDER);
    		break;
    	case LOCATION_VIDEO:
        	sb.append(VIDEO_FOLDER);
    		break;
    	}

    	if (fileName != null) {
    		sb.append("/");
    		sb.append(fileName);
    	}
    	
    	return sb.toString();
    }

    public static String getTrashPath() {
        StringBuilder sb = new StringBuilder();
    	sb.append(Environment.getExternalStorageDirectory());
    	sb.append(DbConst.EXTERNAL_FILE_STORE);
    	sb.append("/trash");
    	return sb.toString();
    }

    public static String scrubForGoodFileName(long id, String altPrefix, String inName) {
    	StringBuilder sb = new StringBuilder();
    	for (char c : inName.toCharArray()) {
    		boolean match = false;
    		for (char check : RESERVED_CHARS) {
				if (c == check) {
					match = true;
					break;
				}
			}
    		if (!match) {
    			sb.append(c);
    		}
		}

    	// Weird case, but you never know, so allow for it.
    	if (sb.length() == 0) {
    		sb.append(altPrefix);
    		sb.append(id);
    	}

    	return sb.toString();
    }

	public static File createFile(String path) {
		
		int ndx = path.lastIndexOf('/');
		if (ndx > 0) {
			String dirpath = path.substring(0, ndx);
			if (!createDirIfNotExists(dirpath)) {
				return null;
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append(path);
		sb.append(".old");
		String backupPath = sb.toString();
		String methodCall = "createFile()";

		File file = new File(path);

		try {
			if (file.exists()) {
				File backupFile = new File(backupPath);
				if (backupFile.exists()) {
					methodCall = "backupFile.delete()";
					backupFile.delete();
				}
				
				methodCall = "file.renameTo(backupFile)";
				boolean moveOK = file.renameTo(backupFile);
				if (!moveOK) {
					return null;
				}
			}

			methodCall = "createNewFile()";
			file.createNewFile();
		} catch (Exception e) {
			Log.e(FileUtilities.class.getName(), "Exception occurred while attempting " + methodCall + " for " + path, e);
			return null;
		}
		
		return file;
	}

	public static boolean createDirIfNotExists(String path) {
		boolean success = false;
		File file = new File(path);
		if (file.exists()) {
			success = true;
		}
		else {
			try {
				success = file.mkdirs();
			} catch (Exception e) {
				success = false;
				Log.e(FileUtilities.class.getName(), "Exception occurred while calling mkdirs() for " + path, e);
			}
		}

		return success;
		
	}
	/**
	 * @param path
	 * @return The contents of the file, or null if there was an error.
	 */
	public static String readFile(String path)
	{
		StringBuilder content = new StringBuilder();
		String line;
		try {
			FileReader input = new FileReader(path);
			BufferedReader reader = new BufferedReader(input, 4096);

			while((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
			reader.close();
			input.close();
		} catch (Exception e) {
			Log.e(FileUtilities.class.getName(), "Exception occurred while calling readFile() for " + path, e);
			return null;
		}

		return content.toString();
	}
	
	/**
	 * 			
	 * @param srcPath
	 * @param destPath
	 * @return
	 */
	public static boolean mvFile(String srcPath, String destPath) {
		boolean success = false;
		try {
			File srcFile = new File(srcPath);
			if (!srcFile.exists()) {
				return false;
			}
			
			int ndx = destPath.lastIndexOf('/');
			if (ndx == (destPath.length() - 1)) {
				// If the path ends with a slash, then it's a directory, so append the source filename
				StringBuilder sb = new StringBuilder();
				sb.append(destPath);
				if (!createDirIfNotExists(destPath.substring(0, ndx))) {
					return false;
				}
				ndx = srcPath.lastIndexOf('/');
				if (ndx > -1) {
					sb.append(srcPath.substring(ndx + 1));
				}
				else {
					sb.append(srcPath);
				}
				destPath = sb.toString();
			}
			else if (ndx > 0) {
				String dirpath = destPath.substring(0, ndx);
				if (!createDirIfNotExists(dirpath)) {
					return false;
				}
			}

			File destFile = new File(destPath);
			if (destFile.exists()) {
				destFile.delete();
			}
			success = srcFile.renameTo(destFile);
			
		} catch (Exception e) {
			Log.e(FileUtilities.class.getName(), "Exception occurred while calling renameTo() from " + srcPath + " to " + destPath, e);
			return false;
		}
		
		return success;
	}

	public static boolean sendFileToTrash(String path) {
		StringBuilder sb = new StringBuilder();
		sb.append(getTrashPath());
		sb.append('/');
		return mvFile(path, sb.toString());	
	}

	/**
	 * Indicates whether the input file exists.
	 * @param path The path to check.
	 * @return True if a file exists at the path specified.
	 */
	public static boolean doesFileExist(String path) {
		File file = new File(path);
		if (file.exists()) {
			return true;
		}

		return false;
	}
}

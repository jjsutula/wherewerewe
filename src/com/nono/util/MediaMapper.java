package com.nono.util;

import java.util.HashMap;

import com.nono.data.MediaConst;

public class MediaMapper {

	private HashMap<String,Integer> mediaMatchMap = new HashMap<String,Integer>();

	public MediaMapper() {
    	loadMediaMatchMap();
	}

	private void loadMediaMatchMap() {
		mediaMatchMap.put(MediaConst.FILE_PREFIX_AUDIO, MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("3gpp", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("ogg", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("mp3", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("m4a", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("aac", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("flac", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("mkv", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("mkv", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("mid", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("xmf", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("mxmf", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("rtttl", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("rtx", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("ota", MediaConst.TYPE_AUDIO);
		mediaMatchMap.put("imy", MediaConst.TYPE_AUDIO);
		
		mediaMatchMap.put(MediaConst.FILE_PREFIX_IMAGE, MediaConst.TYPE_IMAGE);
		mediaMatchMap.put("jpg", MediaConst.TYPE_IMAGE);
		mediaMatchMap.put("jepg", MediaConst.TYPE_IMAGE);
		mediaMatchMap.put("gif", MediaConst.TYPE_IMAGE);
		mediaMatchMap.put("png", MediaConst.TYPE_IMAGE);
		mediaMatchMap.put("bmp", MediaConst.TYPE_IMAGE);
		mediaMatchMap.put("tif", MediaConst.TYPE_IMAGE);
		mediaMatchMap.put("tiff", MediaConst.TYPE_IMAGE);
		mediaMatchMap.put("webp", MediaConst.TYPE_IMAGE);

		mediaMatchMap.put(MediaConst.FILE_PREFIX_VIDEO, MediaConst.TYPE_VIDEO);
		mediaMatchMap.put("3gp", MediaConst.TYPE_VIDEO);
		mediaMatchMap.put("mp4", MediaConst.TYPE_VIDEO);
		mediaMatchMap.put("webm", MediaConst.TYPE_VIDEO);
	}

	public int get(String candidate) {
		int type;
		if (candidate == null) {
			return -1;
		}

		Integer value = mediaMatchMap.get(candidate.toLowerCase());
		if (value == null) {
			type = -1;
		}
		else {
			type = value.intValue();
		}

		return type;
	}
	

	public int getType (String filename) {
		int type = -1;
		if (filename.length() > 3) {
			type = get(filename.substring(0, 4));
		}
		if (type < 0) {
			// Does not start with one of our standard prefixes, query the suffix.
			int ndx = filename.lastIndexOf('.');
			if (filename.length() > ++ndx) {
				type = get(filename.substring(ndx));
			}
		}

		return type;
	}
}

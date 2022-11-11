package com.nono.wherewerewe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.nono.data.MediaConst;
import com.nono.gui.IconListDialog;
import com.nono.gui.IconListSelectRow;
import com.nono.util.MediaMapper;
import com.nono.wherewerewe.data.DbConst;

public class MediaImportDialog extends IconListDialog {

	private static final int TYPE_FOLDER = 10;
	private static int[] mediaIcons = {R.drawable.audio_small, R.drawable.camera_small, R.drawable.video_small};
	
	private MediaMapper mediaMapper;
	private String currentPath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreateLite(savedInstanceState);
        setTitle(getString(R.string.import_media));

        setContentView(R.layout.menu_list);
        
        StringBuilder sb = new StringBuilder();
    	sb.append(Environment.getExternalStorageDirectory());
    	sb.append(DbConst.EXTERNAL_FILE_STORE);
    	String location = sb.toString();
    	mediaMapper = new MediaMapper();
        loadList(location);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// Get the item that was clicked
		Object o = this.getListAdapter().getItem(position);
		IconListSelectRow row = (IconListSelectRow)o;
		if (row != null) {
			int type;
			String value = null;
			try {
				value = row.getValue();
				type = Integer.parseInt(value);				
			}
			catch (Exception e) {
				Log.e(MediaImportDialog.class.getName(), "An invalid value of " + value + "was returned when the file " + currentPath + "/" + row.getText() + " was selected.", e);
				return;
			}
			
			String name = row.getText();
			String newpath = null;
			switch(type) {
			case TYPE_FOLDER:
				if ("..".equals(name)) {
					int ndx = currentPath.lastIndexOf('/');
					if (ndx > -1) {
						newpath = currentPath.substring(0, ndx);
					}
				}
				if (newpath == null) {
					newpath = currentPath + "/" + name;
				}
				loadList(newpath);
				break;
			case MediaConst.TYPE_AUDIO:
				break;
			case MediaConst.TYPE_VIDEO:
				break;
			case MediaConst.TYPE_IMAGE:
				break;
			}
		}
	}

	private void loadList(String parentDirPath) {
		ArrayList<IconListSelectRow> rowList = new ArrayList<IconListSelectRow>();
		IconListSelectRow row;
		String name;
		int type;
		
    	File dir = new File(parentDirPath);
    	if (dir.exists() && dir.isDirectory()) {
    		try {
				currentPath = dir.getCanonicalPath();
			} catch (IOException e) {
				currentPath = dir.getAbsolutePath();
				Log.e(MediaImportDialog.class.getName(), "Error occurred getting path for " + currentPath, e);
			}

			if (currentPath.length() > 1 && currentPath.substring(1).indexOf('/') > -1) {
				row = new IconListSelectRow("..", R.drawable.folder, Integer.toString(TYPE_FOLDER));
				rowList.add(row);
			}
			
    		File[] fileList = dir.listFiles();
    		for (File file : fileList) {
				if (file.isDirectory()) {
					name = file.getName();
					boolean validDir = true;
					if ("thumbs".equalsIgnoreCase(name) && currentPath.toLowerCase().contains(DbConst.EXTERNAL_FILE_STORE.toLowerCase())) {
						validDir = false;
					}
					if (validDir) {
						row = new IconListSelectRow(name, R.drawable.folder, Integer.toString(TYPE_FOLDER));
						rowList.add(row);
					}
				}
				else if (file.isFile()) {
					type = -1;
					name = file.getName();
					type = mediaMapper.getType(name);
					if (type >= 0 && type < 3) {
						// Only put it in the list if it's a media type we recognize
						row = new IconListSelectRow(name, mediaIcons[type], Integer.toString(type));
						rowList.add(row);						
					}
				}
			}
    	}
		
		IconListSelectRow[] rows = new IconListSelectRow[rowList.size()];
		rows = rowList.toArray(rows);
        MyArrayAdapter myArrayAdapter = new MyArrayAdapter(this, rows);
		this.setListAdapter(myArrayAdapter);
	}
}

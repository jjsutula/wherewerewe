package com.nono.gui;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.nono.data.MediaConst;
import com.nono.util.MediaUtilities;
import com.nono.util.ThumbnailListener;
import com.nono.wherewerewe.R;

public class MediaLibraryDialog extends MenuListDialog {

	public static final String BUNDLE_MEDIA_LIBRARY_DIALOG = "MediaLibraryDialog";
	public static final String BUNDLE_MEDIA_LIBRARY_RETURN = "MediaLibraryReturn";

	public static final int REQUEST_CODE_PREFERENCES = 1;
	public static final int REQUEST_CODE_OPTIONS = 2;
	public static final int REQUEST_CODE_CONFIRM = 3;
	
	public static final int OPTION_DELETE = 0;
	public static final int OPTION_ROTATE_LEFT = 1;
	public static final int OPTION_ROTATE_RIGHT = 2;
	public static final int OPTION_DETAILS = 3;

	private MyArrayAdapter myArrayAdapter = null;
	private MediaLibraryBundle bundle = null;
	private Handler mHandler = new Handler();
	private boolean contextMenuOpen = false;
	private MediaLibrarySelectRow selectedMediaRow = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreateLite(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        bundle = (MediaLibraryBundle)extras.getSerializable(BUNDLE_MEDIA_LIBRARY_DIALOG);

        setContentView(R.layout.menu_list);
        setTitle(bundle.getTitle());
        cancelButton = (Button) findViewById(R.id.cancel_button);
        if (bundle.getButtonText() == null) {
        	cancelButton.setVisibility(View.GONE);
        }
        else {
        	cancelButton.setText(bundle.getButtonText());
        }

        registerForContextMenu(getListView());

        myArrayAdapter = new MyArrayAdapter(this, bundle.getVisibleRows());
		this.setListAdapter(myArrayAdapter);

        String[] imagePathsNeedingThumbnail = bundle.getImagePathsNeedingThumbnail();
        if (imagePathsNeedingThumbnail.length > 0) {
        	MyThumbnailListener thumbnailListener = new MyThumbnailListener();
        	MediaUtilities.createThumbnailsInBackground(imagePathsNeedingThumbnail, thumbnailListener);
        }
	}


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	// Ignore the standard menu and launch our own.
        contextMenuOpen = true;
        
        AdapterView.AdapterContextMenuInfo info =
            (AdapterView.AdapterContextMenuInfo) menuInfo;
    	//selectedWord = ((TextView) info.targetView).getText().toString();
    	int position = (int)info.id;
		Object o = this.getListAdapter().getItem(position);
		MediaLibrarySelectRow row = (MediaLibrarySelectRow)o;
		if (row.isLeaf()) {
			selectedMediaRow = row;
			launchOptionsMenu();
		}
    }

    /**
     * Launch the options menu.
     */
	private void launchOptionsMenu() {
    	Intent launchIntent = new Intent().setClass(this, MenuListDialog.class);
	   	Bundle extras = new Bundle();
	   	MenuListBundle bundle = new MenuListBundle(getString(R.string.options),getString(R.string.cancel), 3);
	   	
   		bundle.add(getString(R.string.delete), R.drawable.trash, Integer.toString(OPTION_DELETE));
   		bundle.add(getString(R.string.details), R.drawable.details, Integer.toString(OPTION_DETAILS));
        if (selectedMediaRow.getMediaType() == MediaConst.TYPE_IMAGE) {
	   		bundle.add(getString(R.string.rotate_right), R.drawable.rotate_right, Integer.toString(OPTION_ROTATE_RIGHT));
	   		bundle.add(getString(R.string.rotate_left), R.drawable.rotate_left, Integer.toString(OPTION_ROTATE_LEFT));
        }
	   	extras.putSerializable(MenuListDialog.BUNDLE_MENU_LIST_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, REQUEST_CODE_OPTIONS);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
//			case REQUEST_CODE_PREFERENCES:
//	        	onPreferencesResult(requestCode, resultCode, data);
//				break;
			case REQUEST_CODE_OPTIONS:
	        	onOptionsResult(data);
				break;
			case REQUEST_CODE_CONFIRM:
	        	onConfirmResult(data);
				break;
			}
		}
	}

    /**
     * Process the result of the main options activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onOptionsResult(Intent data) {
    	MyThumbnailListener thumbnailListener;
        Bundle extras = data.getExtras();
        String selectedStr = (String)extras.getSerializable(MenuListDialog.BUNDLE_MENU_LIST_RETURN);
        if (selectedStr != null) {
        	try {
				int selected = Integer.parseInt(selectedStr);
				String result = null;
				switch(selected) {
				case OPTION_DELETE:
					String path = selectedMediaRow.getValue();
					int type = selectedMediaRow.getMediaType();
					launchConfirmDelete(type, path);
					break;
				case OPTION_DETAILS:
		        	launchMediaProperties(selectedMediaRow.getValue(), selectedMediaRow.getMediaType());
					break;
				case OPTION_ROTATE_LEFT:
		        	thumbnailListener = new MyThumbnailListener();
		    		MediaUtilities.rotateImageAndCreateThumbnail(selectedMediaRow.getValue(), 270, thumbnailListener);
					result = getString(R.string.rotating_image);
					break;
				case OPTION_ROTATE_RIGHT:
		        	thumbnailListener = new MyThumbnailListener();
		    		MediaUtilities.rotateImageAndCreateThumbnail(selectedMediaRow.getValue(), 90, thumbnailListener);
					result = getString(R.string.rotating_image);
					break;
				}
				if (result != null) {
					Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
				}
			} catch (NumberFormatException e) {
				Toast.makeText(this, "Invalid entry! Expected a number but received " + selectedStr, Toast.LENGTH_SHORT).show();
			} 
        }

        contextMenuOpen = false;
    }

    private void launchConfirmDelete(int type, String path) {
    	ConfirmBundle bundle = new ConfirmBundle(getString(R.string.confirm_delete), path, type);
	   	Bundle extras = new Bundle();

    	Intent launchIntent = new Intent().setClass(this, ConfirmDialog.class);
	   	extras.putSerializable(ConfirmDialog.BUNDLE_CONFIRM_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, REQUEST_CODE_CONFIRM);
    }

    /**
     * Process the result of the main options activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onConfirmResult(Intent data) {
        Bundle extras = data.getExtras();
        ConfirmBundle confirmationBundle  = (ConfirmBundle)extras.getSerializable(ConfirmDialog.BUNDLE_CONFIRM_DIALOG_RETURN);
        String path = confirmationBundle.getPath();
        int type = confirmationBundle.getType();
        String result;
        File f = new File(path);
        if (f.delete()) {
        	bundle.removeMenuItem(selectedMediaRow);
        	if (type == MediaConst.TYPE_IMAGE) {
        		MediaUtilities.removeThumbnail(path);
        	}
    		myArrayAdapter = new MyArrayAdapter(MediaLibraryDialog.this, bundle.getVisibleRows());
    		this.setListAdapter(myArrayAdapter);
    		result = getString(R.string.file_deleted);
        }
        else {
        	result = getString(R.string.file_not_deleted);
        }

		Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }

	private void launchMediaPlayer(String path, int type) {
//    	Intent launchIntent = new Intent().setClass(this, MediaPlayDialog.class);
//	   	Bundle extras = new Bundle();
//	   	MediaPlayBundle bundle = new MediaPlayBundle(path, type);
//
//	   	extras.putSerializable(MediaPlayDialog.BUNDLE_MEDIA_PLAY_DIALOG, bundle);
//	   	launchIntent.putExtras(extras);
//
//        startActivity(launchIntent);
		try
		{
			Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
			StringBuilder sb = new StringBuilder();
			sb.append(path);
	        File file = new File(sb.toString()); 
	        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
	        String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
	        myIntent.setDataAndType(Uri.fromFile(file),mimetype);
	        startActivity(myIntent);
//	        System.out.println("Finished.");
		}
		catch (Exception e) 
		{
			String data = e.getMessage();
			System.out.println(data);
		}
	}

	private void launchMediaProperties(String path, int type) {
    	Intent launchIntent = new Intent().setClass(this, ShowMediaPropertiesDialog.class);
	   	Bundle extras = new Bundle();
        ShowMediaPropertiesBundle bundle = new ShowMediaPropertiesBundle(path, type);

	   	extras.putSerializable(ShowMediaPropertiesDialog.BUNDLE_SHOW_MEDIA_PROPERTIES_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        startActivity(launchIntent);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		if (!contextMenuOpen) {
			Object o = this.getListAdapter().getItem(position);
			MediaLibrarySelectRow row = (MediaLibrarySelectRow)o;
			if (row.isLeaf()) {
				// Actual file so play the media. Do not call super class.
				launchMediaPlayer(row.getValue(), row.getMediaType());
			}
			else {
				// Tree node so let the super class handle the display
				super.onListItemClick(l, v, position, id);
			}
		}
		else {
	        Log.e(MediaLibraryDialog.class.getName(), "JJS- Click ignored - context menu is open");
		}
	}

	private void updateUI() {
        try {
			myArrayAdapter = new MyArrayAdapter(MediaLibraryDialog.this, bundle.getVisibleRows());
			this.setListAdapter(myArrayAdapter);
		} catch (Exception e) {}
	}

	private class UIUpdater implements Runnable {
		@Override
		public void run() {
			updateUI();
		}
		
	}

    private class MyThumbnailListener implements ThumbnailListener {

		@Override
		public void thumbnailCreated(String imagePath, String thumbnailPath, int rotated) {
			
			for (MenuListSelectRow row : bundle.getVisibleRows()) {
				if (imagePath.equals(row.getValue())) {
					row.setIconFilePath(thumbnailPath);
					
		            try {
						mHandler.postDelayed(new UIUpdater(), 0);
					} catch (Exception e) {}	
					
					break;
				}
			}
		}
    }
}

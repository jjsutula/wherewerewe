package com.nono.gui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nono.util.MediaUtilities;
import com.nono.wherewerewe.R;

public class MenuListDialog extends ListActivity {

	public static final String BUNDLE_MENU_LIST_DIALOG = "MenuListDialog";
	public static final String BUNDLE_MENU_LIST_RETURN = "MenuListDialogReturn";

	protected Button cancelButton;
	private MyArrayAdapter myArrayAdapter = null;
	private MenuListBundle bundle = null;
	private int positionClicked = -1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        bundle = (MenuListBundle)extras.getSerializable(BUNDLE_MENU_LIST_DIALOG);
        setTitle(bundle.getTitle());

        setContentView(R.layout.menu_list);
        
        cancelButton = (Button) findViewById(R.id.cancel_button);
        if (bundle.getButtonText() == null) {
        	cancelButton.setVisibility(View.GONE);
        }
        else {
        	cancelButton.setText(bundle.getButtonText());
        }

        myArrayAdapter = new MyArrayAdapter(this, bundle.getVisibleRows());
		this.setListAdapter(myArrayAdapter);
	}

    /**
     * Invoked by overriding classes to set up the activity.
     * @param savedInstanceState
     */
    protected void onCreateLite(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Object o = this.getListAdapter().getItem(position);
		MenuListSelectRow row = (MenuListSelectRow)o;
		if (row.isLeaf()) {
	   	 	Intent returnIntent = new Intent().setClass(this, String.class);
			Bundle extras = new Bundle();
			extras.putSerializable(BUNDLE_MENU_LIST_RETURN, row.getValue());
			returnIntent.putExtras(extras);
			setResult(RESULT_OK, returnIntent);
			finish();
		}
		else {
			positionClicked = position;
			row.toggleExpanded();
	        myArrayAdapter = new MyArrayAdapter(this, bundle.getVisibleRows());
			this.setListAdapter(myArrayAdapter);
			int showPosition = bundle.getPosition(row);
			if (showPosition > -1) {
				l.setSelection(showPosition);
			}
		}
	}

    // static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public ImageView imageView;
		public TextView textView;
	}
    
    class MyArrayAdapter extends ArrayAdapter<MenuListSelectRow> {

    	private final Activity context;
    	private final MenuListSelectRow[] rows;

		public MyArrayAdapter(Activity context, MenuListSelectRow[] rows) {
			super(context, R.layout.menu_list_row, rows);
			this.context = context;
			this.rows = rows;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflater = context.getLayoutInflater();
			View rowView = inflater.inflate(R.layout.menu_list_row, null, true);
			MenuListSelectRow row = rows[position];
			int depth = row.getDepth();
			if (depth > 0) {
				LinearLayout layout = (LinearLayout) rowView.findViewById(R.id.layout);
				ImageView blank = new ImageView(MenuListDialog.this);
				Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.blank);
				blank.setImageBitmap(imageBitmap);
				for (int ndx = 0; ndx < depth; ndx++) {
					layout.addView(blank, 0);
				}
				
			}
			TextView textView = (TextView) rowView.findViewById(R.id.label);
			textView.setText(row.getText());
			ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
			
			Bitmap imageBitmap = null;
			int iconId = row.getIconId();
			if (iconId > -1) {
				imageBitmap = BitmapFactory.decodeResource(getResources(), iconId);
			}
			else {
				// No returned icon id, so there must be a file path instead.
	            imageBitmap = MediaUtilities.prepareBitmap(row.getIconFilePath(), MediaUtilities.THUMBNAIL_LONG_EDGE, MediaUtilities.THUMBNAIL_LONG_EDGE);
			}
            imageView.setImageBitmap(imageBitmap);

			if (positionClicked == position) {
				rowView.requestFocus();
			}
			return rowView;
		}
	}

    public void buttonClickHandler(View view) {

    	view.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING );

		switch (view.getId()) {
		case R.id.cancel_button:
			setResult(RESULT_CANCELED, null);
			finish();
			break;
		}
	}

    @Override
	public void onBackPressed() {
		super.onBackPressed();
		setResult(RESULT_CANCELED, null);
		finish();
	}
}

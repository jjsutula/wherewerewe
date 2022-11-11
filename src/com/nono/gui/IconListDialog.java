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
import android.widget.ListView;
import android.widget.TextView;

import com.nono.wherewerewe.R;

public class IconListDialog extends ListActivity {

	public static final String BUNDLE_ICON_LIST_DIALOG = "IconListDialog";
	public static final String BUNDLE_ICON_LIST_RETURN = "IconListDialogReturn";

	private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        IconListBundle bundle = (IconListBundle)extras.getSerializable(BUNDLE_ICON_LIST_DIALOG);
        setTitle(bundle.getTitle());

        setContentView(R.layout.icon_list);
        
        cancelButton = (Button) findViewById(R.id.cancel_button);
        if (bundle.getButtonText() == null) {
        	cancelButton.setVisibility(View.GONE);
        }
        else {
        	cancelButton.setText(bundle.getButtonText());
        }

		this.setListAdapter(new MyArrayAdapter(this, bundle.getList()));
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
		// Get the item that was clicked
		Object o = this.getListAdapter().getItem(position);
		IconListSelectRow row = (IconListSelectRow)o;
   	 	Intent returnIntent = new Intent().setClass(this, String.class);
		Bundle extras = new Bundle();
		extras.putSerializable(BUNDLE_ICON_LIST_RETURN, row.getValue());
		returnIntent.putExtras(extras);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public ImageView imageView;
		public TextView textView;
	}
    
    protected class MyArrayAdapter extends ArrayAdapter<IconListSelectRow> {

    	private final Activity context;
    	private final IconListSelectRow[] rows;

		public MyArrayAdapter(Activity context, IconListSelectRow[] rows) {
			super(context, R.layout.icon_list_row, rows);
			this.context = context;
			this.rows = rows;
		}
	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// ViewHolder will buffer the classes to the individual fields of the row
			// layout
	
			ViewHolder holder;
			// This will save memory and time on Android
			// This only works if the base layout for all classes are the same
			
			View rowView = convertView;			
			if (rowView == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				holder = new ViewHolder();
				rowView = inflater.inflate(R.layout.icon_list_row, null, true);
				holder.textView = (TextView) rowView.findViewById(R.id.label);
				holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
				rowView.setTag(holder);
			} else {
				holder = (ViewHolder) rowView.getTag();
			}
	
			holder.textView.setText(rows[position].getText());
			
			Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), rows[position].getIconId());
			holder.imageView.setImageBitmap(imageBitmap);
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

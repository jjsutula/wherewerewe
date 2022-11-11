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

public class MultiSelectListDialog extends ListActivity {

	public static final String BUNDLE_MULTI_LIST_DIALOG = "MultiListDialog";
	public static final String BUNDLE_MULTI_LIST_RETURN = "MultiListDialogReturn";
	
	private int selectedIconID = 0;
	private MultiSelectListReturnBundle itemList;
	private Button okButton;
	private int selectedCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        MultiSelectListBundle bundle = (MultiSelectListBundle)extras.getSerializable(BUNDLE_MULTI_LIST_DIALOG);
        selectedIconID = bundle.getSelectedIconID();
        setTitle(bundle.getTitle());

		itemList = new MultiSelectListReturnBundle(bundle); 

        setContentView(R.layout.multi_select_list);
        final ListView listView = getListView();

        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
    	okButton = (Button) findViewById(R.id.ok_button);
    	okButton.setEnabled(false);
        if (bundle.getOkButtonText() != null) {
        	okButton.setText(bundle.getOkButtonText());
        }

		this.setListAdapter(new MyArrayAdapter(this, bundle.getList()));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// Get the item that was clicked
		MultiSelectListReturnRow returnRow = itemList.getList()[position];
		if (returnRow.toggleSelected()) {
			selectedCount++;
		}
		else {
			selectedCount--;
		}

		if (selectedCount == 0) {
	    	okButton.setEnabled(false);
		}
		else if (selectedCount == 1) {
	    	okButton.setEnabled(true);
		}
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public ImageView imageView;
		public TextView textView;
	}
    
    private class MyArrayAdapter extends ArrayAdapter<MultiSelectListRow> {

    	private final Activity context;
    	private final MultiSelectListRow[] rows;

		public MyArrayAdapter(Activity context, MultiSelectListRow[] rows) {
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
	
			MultiSelectListRow row = rows[position];
			holder.textView.setText(row.getText());

			MultiSelectListReturnRow returnRow = itemList.getList()[position];
			int iconID;
			if (returnRow.isSelected()) {
				iconID = selectedIconID;
			}
			else {
				iconID = row.getIconId();
			}
			Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), iconID);
			holder.imageView.setImageBitmap(imageBitmap);

			return rowView;
		}
	}

    /**
     * Handle any button clicks that occur.
     * @param view The view that was clicked.
     */
	public void buttonClickHandler(View view) {
    	view.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING );

		switch (view.getId()) {
		case R.id.cancel_button:
			setResult(RESULT_CANCELED, null);
			finish();
			break;
		case R.id.ok_button:
	   	 	Intent returnIntent = new Intent();
			Bundle extras = new Bundle();
			extras.putSerializable(BUNDLE_MULTI_LIST_RETURN, itemList);
			returnIntent.putExtras(extras);
			setResult(RESULT_OK, returnIntent);
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

package com.nono.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.TextView;

import com.nono.wherewerewe.R;

public class ConfirmDialog extends Activity {

	public static final String BUNDLE_CONFIRM_DIALOG = "ConfirmDialog";
	public static final String BUNDLE_CONFIRM_DIALOG_RETURN = "ConfirmDialogReturn";

	private ConfirmBundle bundle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        bundle = (ConfirmBundle)extras.getSerializable(BUNDLE_CONFIRM_DIALOG);
        setTitle(bundle.getTitle());

        setContentView(R.layout.confirm);        
        String path = bundle.getPath();
        String fileName;
        int ndx = path.lastIndexOf('/');
        if (ndx > -1) {
        	fileName = path.substring(ndx + 1);
        	path = path.substring(0, ndx + 1);
        }
        else {
        	fileName = new String(path);
        	path = ".";
        }
        TextView nameTextView = (TextView) findViewById(R.id.text_file_name);
        nameTextView.setText(fileName);
        TextView pathTextView = (TextView) findViewById(R.id.text_path);
        pathTextView.setText(path);
	}

    @Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED, null);
		super.onBackPressed();
	}
    
    public void buttonClickHandler(View view) {

    	view.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING );

		switch (view.getId()) {
		case R.id.no_button:
			setResult(RESULT_CANCELED, null);
			finish();
			break;
		case R.id.yes_button:
	   	 	Intent returnIntent = new Intent().setClass(this, ConfirmBundle.class);
			Bundle extras = new Bundle();
			extras.putSerializable(BUNDLE_CONFIRM_DIALOG_RETURN, bundle);
			returnIntent.putExtras(extras);
			setResult(RESULT_OK, returnIntent);
			finish();
			break;
		}
	}
}

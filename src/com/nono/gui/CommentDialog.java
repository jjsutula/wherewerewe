package com.nono.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.nono.wherewerewe.R;

public class CommentDialog extends Activity {

	public static final String BUNDLE_COMMENT_DIALOG = "CommentDialog";
	public static final String BUNDLE_COMMENT_DIALOG_RETURN = "CommentDialogReturn";

	private EditText editTextView;
	private String text = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        CommentBundle bundle = (CommentBundle)extras.getSerializable(BUNDLE_COMMENT_DIALOG);
        setTitle(bundle.getTitle());

        text = bundle.getText();
        
        setContentView(R.layout.comment_text);        
        editTextView = (EditText) findViewById(R.id.comment);
        editTextView.setLines(bundle.getNumLines());
        editTextView.setText(text);
        editTextView.addTextChangedListener(new MyTextWatcher());
	}

    @Override
	public void onBackPressed() {
   	 	Intent returnIntent = new Intent().setClass(this, String.class);
		Bundle extras = new Bundle();
		if (text != null && text.length() == 0) {
			text = null;
		}
		extras.putSerializable(BUNDLE_COMMENT_DIALOG_RETURN, text);
		returnIntent.putExtras(extras);
		setResult(RESULT_OK, returnIntent);
		super.onBackPressed();
	}

	/**
	 * Handle text updates.
	 */
	private class MyTextWatcher implements TextWatcher {

        public void afterTextChanged(Editable s) {
        	String newVal;
        	if (s != null) {
        		newVal = s.length() == 0 ? null : s.toString();
        	}
        	else {
        		newVal = null;
        	}
        	
        	text = newVal;
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
       }
	}
}

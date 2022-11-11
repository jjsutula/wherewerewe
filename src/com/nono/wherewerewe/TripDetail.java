package com.nono.wherewerewe;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.nono.wherewerewe.data.IntentWrapper;
import com.nono.wherewerewe.data.table.TripTable;
import com.nono.wherewerewe.db.DbAdapter;
import com.nono.wherewerewe.db.TripTableDb;

public class TripDetail extends Activity {
	
	private final static int START_DATE_DIALOG_ID = 0;
	private final static int END_DATE_DIALOG_ID = 1;

	private TripTableDb tripTableDb;
	TripTable tripTable = null;

	private DbAdapter dbAdapter = null;
	Timer updateTimer = null;
	private DateFormat dateFormat;
	private Button startDateButton;
	private Button endDateButton;
	
	private boolean dataUpdated = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_detail);
        dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        tripTableDb = new TripTableDb(dbAdapter);

        dateFormat = android.text.format.DateFormat.getLongDateFormat(getApplicationContext());

        Bundle extras = this.getIntent().getExtras();
        IntentWrapper wrapper = (IntentWrapper)extras.getSerializable(WhereWereWe.INTENT_WRAPPER);
        tripTable = wrapper.getTripTable();

        MyTextWatcher myNameTextWatcher = new MyTextWatcher(MyTextWatcher.NAME_FIELD);
        EditText tripNameEdit = (EditText) findViewById(R.id.edit_trip_name);
        tripNameEdit.setText(tripTable.getName());
        tripNameEdit.addTextChangedListener(myNameTextWatcher);

        startDateButton = (Button) findViewById(R.id.start_date_button);
        startDateButton.setText(formatDate(tripTable.getStartDate()));

        endDateButton = (Button) findViewById(R.id.end_date_button);
        endDateButton.setText(formatDate(tripTable.getEndDate()));
    }

    private String formatDate(long date) {
    	
    	String retval;
    	Date displayDate;
    	if (date <= 0) {
    		retval = null;
    	}
    	else {
    		displayDate = new Date(date);
    		retval = dateFormat.format(displayDate);
    	}

        return retval;
    }

	/**
	 * Update any fields they've changed but have not yet persisted to the database.
	 */
	private void updateChangedFields() {
		boolean doUpdate = false;
    	if (updateTimer != null) {
    		updateTimer.cancel();
    		updateTimer = null;
    		doUpdate = true;
    	}

    	if (doUpdate) {
    		tripTableDb.updateTrip(tripTable);
    		dataUpdated = true;
    	}
	}

    @Override
	public void onBackPressed() {
    	updateChangedFields();
    	if (dataUpdated) {
    		// Let the calling activity know something changed
    		setResult(RESULT_OK);
    	}
		super.onBackPressed();
	}

	@Override
	protected void onStop() {
		updateChangedFields();
    	if (dataUpdated) {
    		// Let the calling activity know something changed
    		setResult(RESULT_OK);
    	}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (dbAdapter != null) {
			dbAdapter.close();
		}
		super.onDestroy();
	}

    private void setUpdateTimer() {
    	if (updateTimer != null) {
    		updateTimer.cancel();
    		updateTimer = null;
    	}

    	// Set a timer to update the data in the db 10 seconds after they stop typing
    	updateTimer = new Timer();
    	updateTimer.schedule(new TimerTask() {
        	public void run() {
        		tripTableDb.updateTrip(tripTable);
        		dataUpdated = true;
        		updateTimer.cancel();
        		updateTimer = null;
        	}
        }, 10000);
    	
    }
 
	public void buttonClickHandler(View view) {

    	view.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING );

		switch (view.getId()) {
		case R.id.start_date_button:
			showDialog(START_DATE_DIALOG_ID);
			break;
		case R.id.end_date_button:
			showDialog(END_DATE_DIALOG_ID);
			break;
		}
	}

    @Override
    protected Dialog onCreateDialog(int id) {
    	int type = 0;
    	long dbDate = 0;
    	Date displayDate;
    	
        switch (id) {
        case START_DATE_DIALOG_ID:
        	type = MyDateSetListener.START_DATE;
        	dbDate = tripTable.getStartDate(); 
        	break;
        case END_DATE_DIALOG_ID:
        	type = MyDateSetListener.END_DATE;
        	dbDate = tripTable.getEndDate(); 
        	break;
        }

		Calendar cal = Calendar.getInstance();
        if (dbDate > 0) {
        	displayDate = new Date(dbDate);
        	cal.setTime(displayDate);
        }

        return new DatePickerDialog(this,
                new MyDateSetListener(type),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    private class MyDateSetListener implements DatePickerDialog.OnDateSetListener {

    	private final static int START_DATE = 0;
    	private final static int END_DATE = 1;
    	
    	private final int type;
    	
    	MyDateSetListener(int type) {
    		this.type = type;
    	}

		@Override
        public void onDateSet(DatePicker view, int year, 
                int monthOfYear, int dayOfMonth) {

			Date displayDate = null;
	        switch (type) {
	        case START_DATE:
				displayDate = new Date(tripTable.getStartDate());
	        	break;
	        case END_DATE:
				displayDate = new Date(tripTable.getEndDate());
	        	break;
	        }

			Calendar cal = Calendar.getInstance();
			cal.setTime(displayDate);
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, monthOfYear);
			cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			
			long dbDate = cal.getTimeInMillis();
	        switch (type) {
	        case START_DATE:
	            startDateButton.setText(formatDate(dbDate));
	        	tripTable.setStartDate(dbDate);
	        	break;
	        case END_DATE:
	            endDateButton.setText(formatDate(dbDate));
	        	tripTable.setEndDate(dbDate);
	        	break;
    	    }
	        setUpdateTimer();
		}
    }

	/**
	 * Handle text updates.
	 */
	private class MyTextWatcher implements TextWatcher {
		
		public final static int NAME_FIELD = 1;
		
		private final int field;
		
		MyTextWatcher(int field) {
			this.field = field;
		}

        public void afterTextChanged(Editable s) {
        	if (updateTimer != null) {
        		updateTimer.cancel();
        		updateTimer = null;
        	}

        	String newVal;
        	if (s != null) {
        		newVal = s.length() == 0 ? null : s.toString();
        	}
        	else {
        		newVal = null;
        	}
        	
        	if (field == NAME_FIELD) {
        		tripTable.setName(newVal);
        	}

        	setUpdateTimer();
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
       }
	}
}
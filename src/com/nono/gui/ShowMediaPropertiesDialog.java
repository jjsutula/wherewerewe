package com.nono.gui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.nono.data.MediaConst;
import com.nono.util.MediaUtilities;
import com.nono.util.StringUtilities;
import com.nono.wherewerewe.R;
import com.nono.wherewerewe.WhereWereWe;
import com.nono.wherewerewe.data.DbConst;

public class ShowMediaPropertiesDialog extends Activity {

	public static final String BUNDLE_SHOW_MEDIA_PROPERTIES_DIALOG = "ShowMediaPropertiesDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        ShowMediaPropertiesBundle bundle = (ShowMediaPropertiesBundle)extras.getSerializable(BUNDLE_SHOW_MEDIA_PROPERTIES_DIALOG);
        setTitle(getString(R.string.properties));

        String path = bundle.getPath();
        int type = bundle.getType();
        
        setContentView(R.layout.show_media_properties);        
        TableLayout tableLayout = (TableLayout) findViewById(R.id.details_table);
        displayMediaProperties(tableLayout, path, type);
	}

    /**
     * Invoked for the media properties screen.
     */
    private void displayMediaProperties(TableLayout detailTableLayout, String path, int type) {
        String fileName;
        String thumbPath = null;
        String directoryPath = null;
        int thumbID = -1;

        int lastSlashNdx = path.lastIndexOf('/');
        if (lastSlashNdx > -1) {
        	lastSlashNdx++;
            int rootNdx = path.indexOf(DbConst.EXTERNAL_FILE_STORE);
            if (rootNdx > -1) {
            	rootNdx+=DbConst.EXTERNAL_FILE_STORE.length() + 1;
            }
            else {
            	rootNdx = 0;
            }
        	fileName = path.substring(lastSlashNdx);
        	directoryPath = path.substring(rootNdx, lastSlashNdx);
        }
        else {
        	fileName = new String(path);
        	directoryPath = "";
        }
        
        TextView textView;
        textView = (TextView)findViewById(R.id.label);
        textView.setText(fileName);
        
        switch(type) {
    	case MediaConst.TYPE_IMAGE:
        	thumbPath = MediaUtilities.getThumbnailPath(path);
        	if (thumbPath == null) {
        		thumbID = R.drawable.camera_small;
        	}
    		break;
    	case MediaConst.TYPE_AUDIO:
    		thumbID = R.drawable.audio_small;
    		break;
    	case MediaConst.TYPE_VIDEO:
    		thumbID = R.drawable.video_small;
    		break;
        }

		ImageView imageView = (ImageView) findViewById(R.id.icon);
		Bitmap imageBitmap = null;
		if (thumbID > -1) {
			imageBitmap = BitmapFactory.decodeResource(getResources(), thumbID);
		}
		else {
			// No returned icon id, so there must be a file path instead.
            imageBitmap = MediaUtilities.prepareBitmap(thumbPath, MediaUtilities.THUMBNAIL_LONG_EDGE, MediaUtilities.THUMBNAIL_LONG_EDGE);
		}
        imageView.setImageBitmap(imageBitmap);

        getFileProperties(detailTableLayout, directoryPath, path);

        TableLayout exifTableLayout = (TableLayout) findViewById(R.id.exif_table);
        textView = (TextView)findViewById(R.id.exif_label);
        if (type == MediaConst.TYPE_IMAGE) {
        	textView.setText(getString(R.string.exif_information));
        	showExifInformation(exifTableLayout, path);
        }
        else {
        	textView.setVisibility(View.GONE);
        	exifTableLayout.setVisibility(View.GONE);
        }
	}

    private void getFileProperties(TableLayout detailTableLayout, String directoryPath, String fullPath) {
        StringBuilder sb = new StringBuilder();
        File file = new File(fullPath);
        if (file.exists()) {
//        	if (f.isDirectory()) {
//        		addTableRow(getString(R.string.prop_type_folder));
//        	}
        	addTableRow(detailTableLayout, getString(R.string.path_label), directoryPath);
        	
            long filesize = file.length();
            if (filesize < 1024) {
            	sb.append(filesize);
            	sb.append(' ');
            	sb.append(getString(R.string.bytes));
            }
            else if (filesize < 1024 * 1024) {
            	filesize = (int)Math.round((double)((double)filesize / (double)(1024)));
            	sb.append(filesize);
            	sb.append(' ');
            	sb.append(getString(R.string.kb));
            }
            else {
            	// Show MB with  a precision of 1 
            	double numMB = (double)((double)filesize / (double)(1024 * 1024));
            	sb.append(StringUtilities.formatData(numMB, 1));
            	sb.append(' ');
            	sb.append(getString(R.string.mb));
            }

        	addTableRow(detailTableLayout, getString(R.string.size_label), sb.toString());
        	
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        	addTableRow(detailTableLayout, getString(R.string.modified_label), df.format(new Date(file.lastModified())));
        }
    }

	private void showExifInformation(TableLayout tableLayout, String path) {
    	try {
    		ExifInterface exifInterface = new ExifInterface(path);

    		appendNonNull(tableLayout, getString(R.string.label_length), exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
    		appendNonNull(tableLayout, getString(R.string.label_width), exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
    		appendNonNull(tableLayout, getString(R.string.label_datetime), exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
    		appendNonNull(tableLayout, getString(R.string.label_make), exifInterface.getAttribute(ExifInterface.TAG_MAKE));
    		appendNonNull(tableLayout, getString(R.string.label_model), exifInterface.getAttribute(ExifInterface.TAG_MODEL));
    		appendNonNull(tableLayout, getString(R.string.label_orientation), exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
    		appendNonNull(tableLayout, getString(R.string.label_white_balance), exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE));
//    		appendNonNull(tableLayout, getString(R.string.label_focal_length), exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
    		appendNonNull(tableLayout, getString(R.string.label_flash), exifInterface.getAttribute(ExifInterface.TAG_FLASH));

    		appendNonNull(tableLayout, getString(R.string.label_latitude), exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
    		appendNonNull(tableLayout, getString(R.string.label_latitude_ref), exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
    		appendNonNull(tableLayout, getString(R.string.label_longitude), exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
    		appendNonNull(tableLayout, getString(R.string.label_longitude_ref), exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
//    		appendNonNull(tableLayout, getString(R.string.label_gps_processing_method), exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD));
    	} catch (IOException e) {
			Log.e(WhereWereWe.class.getName(), "IOException occurred while attempting to read the EXIF data for the image at " + path, e);
    	}
		
	}

    private void appendNonNull(TableLayout tableLayout, String tagName, String attr) {
		if (attr != null && !attr.equals("0") && !attr.equals("-1")) {
        	addTableRow(tableLayout, tagName, attr);
		}
    }

    private void addTableRow(TableLayout tableLayout, String label, String value) {

        TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.FILL_PARENT,
        															LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 0, 0);
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(
        		TableRow.LayoutParams.FILL_PARENT,
        		TableRow.LayoutParams.WRAP_CONTENT));
        TextView row1 = new TextView(this);
        row1.setTextSize(12);
        row1.setText(label);
        tr.addView(row1);

        TextView row2 = new TextView(this);
        row2.setTextSize(12);
        row2.setText(value);
        tr.addView(row2, params);
        tableLayout.addView(tr,new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

//        TableRow tr = new TableRow(this);
//        tr.setLayoutParams(new LayoutParams(
//                       LayoutParams.WRAP_CONTENT,
//                       LayoutParams.WRAP_CONTENT));
//        TextView row1 = new TextView(this);
//        row1.setTextSize(12);
//        row1.setText(label);
//        tr.addView(row1);
//
//        TableLayout.MarginLayoutParams params = new TableLayout.MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        params.setMargins(5, 0, 0, 0);
//        TextView row2 = new TextView(this);
//        row2.setTextSize(12);
//        row2.setText(value);
//        tr.addView(row2);
//        tableLayout.addView(tr,new TableLayout.LayoutParams(
//                LayoutParams.FILL_PARENT,
//                LayoutParams.WRAP_CONTENT));
    	
    	/*
          TableLayout tl = (TableLayout)findViewById(R.id.myTableLayout);
               // Create a new row to be added.
               TableRow tr = new TableRow(this);
               tr.setLayoutParams(new LayoutParams(
                              LayoutParams.FILL_PARENT,
                              LayoutParams.WRAP_CONTENT));
                    // Create a Button to be the row-content.
                    Button b = new Button(this);
                    b.setText("Dynamic Button");
                    b.setLayoutParams(new LayoutParams(
                              LayoutParams.FILL_PARENT,
                              LayoutParams.WRAP_CONTENT));
                    // Add Button to row.
                    tr.addView(b);
          // Add row to TableLayout.
          tl.addView(tr,new TableLayout.LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT));
    	 */
/*
<TableRow>
	<TextView android:id="@+id/label"
	    android:layout_column="1"
	    android:textSize="12dp"
	    android:textStyle = "bold"
	    />
	<TextView android:id="@+id/value"
		android:layout_marginLeft="3dp"
	    android:textSize="12dp"
	   	/>
</TableRow>

 */
    }

    @Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED, null);
		super.onBackPressed();
	}
}

package com.nono.wherewerewe;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.nono.data.MediaConst;
import com.nono.util.FileUtilities;
import com.nono.util.MediaMapper;
import com.nono.util.MediaUtilities;
import com.nono.wherewerewe.data.DbConst;
import com.nono.wherewerewe.data.IntentWrapper;
import com.nono.wherewerewe.data.table.TripTable;
 
public class ImageGalleryView extends Activity implements ViewFactory {

	private TripTable tripTable = null;
	private String[] imagePaths = null;
	private String[] thumbnailPaths = null;
    private Bitmap imageBitmap = null;
    private int viewHeight = 0;
    private int viewWidth = 0;
    private int currentPosition = -1;
 
    private ImageSwitcher imageSwitcher;
 
    @Override    
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view);

        Bundle extras = this.getIntent().getExtras();
        IntentWrapper wrapper = (IntentWrapper)extras.getSerializable(WhereWereWe.INTENT_WRAPPER);
        tripTable = wrapper.getTripTable();

        imageSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
        imageSwitcher.setFactory(this);
        imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in));
        imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out));
 
        StringBuilder sb = new StringBuilder();
    	sb.append(Environment.getExternalStorageDirectory());
    	sb.append(DbConst.EXTERNAL_FILE_STORE);
    	sb.append('/');
    	sb.append(FileUtilities.scrubForGoodFileName(tripTable.getId(), "trip_", tripTable.getName()));
        String tripFilePath = sb.toString();

        buildGallery(tripFilePath);
        
        Gallery gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setAdapter(new ImageAdapter(this));
        gallery.setOnItemClickListener(new OnItemClickListener() 
        {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                if (viewHeight == 0) {
                	viewHeight = imageSwitcher.getHeight();
                	viewWidth = imageSwitcher.getWidth();
                }

                imageBitmap = MediaUtilities.prepareBitmap(imagePaths[position], viewHeight, viewWidth);
            	BitmapDrawable drawable = new BitmapDrawable(getResources(), imageBitmap);
            	imageSwitcher.setImageDrawable(drawable);
            	currentPosition = position;
            }
        });  
    }

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop(){
    	if (imageBitmap != null) {
    		imageBitmap.recycle();
    		imageBitmap = null;
    	}

        super.onStop();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

//    @Override
//	protected void onDestroy() {
//		super.onDestroy();
//	}
//
//	@Override
//	protected void onRestart() {
//		super.onRestart();
//	}
//
//	@Override
//	protected void onStart() {
//		super.onStart();
//	}

	// This is called when the screen rotates.
    // (onCreate is no longer called when screen rotates due to manifest, see: android:configChanges)
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
       	viewHeight = imageSwitcher.getHeight();
       	viewWidth = imageSwitcher.getWidth();

       	imageBitmap = MediaUtilities.prepareBitmap(imagePaths[currentPosition], viewHeight, viewWidth);
    	BitmapDrawable drawable = new BitmapDrawable(getResources(), imageBitmap);
    	imageSwitcher.setImageDrawable(drawable);
	}

	private void buildGallery(String tripFilePath) {
        MediaMapper mediaMapper = new MediaMapper();
		StringBuilder sb = new StringBuilder();
    	String thumbnailPath = null;
    	String name;
    	String path;
    	String mediaDirPath;
    	int type;
    	ArrayList<String> mediaList = new ArrayList<String>();
    	ArrayList<String> thumbnailList = new ArrayList<String>();

		sb.append(tripFilePath);
		sb.append(FileUtilities.IMAGE_FOLDER);
		mediaDirPath = sb.toString();
		File mediaDir = new File(mediaDirPath);
		if (mediaDir.exists()) {
			File[] files= mediaDir.listFiles();
			for (File mediaFile : files) {
				if (mediaFile.isFile()) {
					name = mediaFile.getName();
					sb.setLength(0);
					sb.append(mediaDirPath);
					sb.append('/');
					sb.append(name);
					path = sb.toString();
					type = mediaMapper.getType(name);
					switch (type) {
					case (MediaConst.TYPE_IMAGE):
		    			thumbnailPath = getThumbnailPath(path);
		    			if (thumbnailPath != null) {
		        			mediaList.add(path);
		        			thumbnailList.add(thumbnailPath);
		    			}
						break;
					}
				}
			}
		}
    	imagePaths = new String[mediaList.size()];
    	imagePaths = mediaList.toArray(imagePaths);
    	thumbnailPaths = new String[thumbnailList.size()];
    	thumbnailPaths = thumbnailList.toArray(thumbnailPaths);

    	if (imagePaths.length == 0) {
			Toast.makeText(this, "There are no images to display.", Toast.LENGTH_SHORT).show();
    	}
	}

    private String getThumbnailPath(String imagePath) {
    	String thumbPath = null;
    	thumbPath = MediaUtilities.getThumbnailPath(imagePath);

    	return thumbPath;
    }

    public View makeView() 
    {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundColor(0xFF000000);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new 
                ImageSwitcher.LayoutParams(
                        LayoutParams.FILL_PARENT,
                        LayoutParams.FILL_PARENT));
        return imageView;
    }
 
//    private void dumpCurrentInfo(String title) {
//		StringBuilder sb = new StringBuilder();
//		sb.append("*** ImageGalleryView: ");
//		sb.append(title);
//		sb.append("***\n");
//		sb.append("\nWaypoint:TripID=");
//		sb.append(waypointTable.getParentTripId());
//		sb.append(",ID=");
//		sb.append(waypointTable.getId());
//
//    	String msg = sb.toString();
//    	Log.d(ImageGalleryView.class.getName(),msg);
//
//    	sb.setLength(0);
//    	String comment = waypointTable.getComment();
//    	if (comment != null) {
//    		sb.append(comment);
//    		sb.append('\n');
//    	}
//    	sb.append (new Date());
//		sb.append('\n');
//		sb.append(msg);
//		waypointTable.setComment(sb.toString());
//        waypointTableDb.updateWaypoint(waypointTable);
//    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private int itemBackground;
 
        public ImageAdapter(Context c) {
            context = c;
 
            //---setting the style---                
            TypedArray a = obtainStyledAttributes(R.styleable.Gallery);
            itemBackground = a.getResourceId(
                    R.styleable.Gallery_android_galleryItemBackground, 0);
            a.recycle();                                                    
        }
 
        //---returns the number of images---
        public int getCount() {
            return imagePaths.length;
        }
 
        //---returns the ID of an item--- 
        public Object getItem(int position) {
            return position;
        }
 
        public long getItemId(int position) {
            return position;
        }
 
        //---returns an ImageView view---
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(context);

            imageBitmap = MediaUtilities.prepareBitmap(thumbnailPaths[position], MediaUtilities.THUMBNAIL_LONG_EDGE, MediaUtilities.THUMBNAIL_LONG_EDGE);
        	BitmapDrawable drawable = new BitmapDrawable(getResources(), imageBitmap);
        	imageView.setImageDrawable(drawable);

            
//            imageView.setImageURI(thumbnailUris[position]);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setLayoutParams(new Gallery.LayoutParams(150, 120));
            imageView.setBackgroundResource(itemBackground);
            return imageView;
        }
   }    
}
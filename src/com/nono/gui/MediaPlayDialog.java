package com.nono.gui;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.nono.data.MediaConst;
import com.nono.util.MediaUtilities;
import com.nono.wherewerewe.Preferences;
import com.nono.wherewerewe.R;

public class MediaPlayDialog extends Activity implements OnPreparedListener, MediaController.MediaPlayerControl {

	public static final String BUNDLE_MEDIA_PLAY_DIALOG = "MediaPlayDialog";
	public static final String BUNDLE_MEDIA_PLAY_RETURN = "MediaPlayDialogReturn";

	public static final int REQUEST_CODE_PREFERENCES = 1;
	public static final int REQUEST_CODE_OPTIONS = 2;
	
	public static final int OPTION_DETAILS = 3;

    private View emptyView = null;
    private VideoView videoView = null;
    private ImageView imageView = null;
    private MediaPlayer mediaPlayer = null;
    private MediaController mediaController = null;
    
    private Handler handler = new Handler();
//    private RelativeLayout buttonLayout;
	private String path = null;
	private int type;
	private TextView headerView;
	private TextView filenameView;
//	private Button playButton;
//	private Button pauseButton;
//	private Button stopButton;
    private int viewHeight = 0;
    private int viewWidth = 0;
    private Bitmap imageBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        MediaPlayBundle bundle = (MediaPlayBundle)extras.getSerializable(BUNDLE_MEDIA_PLAY_DIALOG);
        path = bundle.getPath();
        type = bundle.getType();
        
        setContentView(R.layout.media_play);
//        buttonLayout = (RelativeLayout)findViewById(R.id.media_buttons);
    	videoView = (VideoView) findViewById(R.id.video_view);
    	imageView = (ImageView) findViewById(R.id.image_view);
    	emptyView = (View) findViewById(R.id.empty_view);
    	headerView = (TextView)findViewById(R.id.now_playing_header);
    	filenameView = (TextView)findViewById(R.id.now_playing_text);
    	
        if (type == MediaConst.TYPE_IMAGE) {
	        registerForContextMenu(imageView);
	        registerForContextMenu(videoView);
	        registerForContextMenu(emptyView);
        }
        
        // must set android:longClickable="true" for each of these views in media_play.xml to enable the folowing
//        videoView.setOnLongClickListener(new View.OnLongClickListener() {  
//            @Override  
//            public boolean onLongClick(View v) {  
//                Log.e(MediaPlayDialog.class.getName(), "JJS- videoView.setOnLongClickListener");
//                return true;  
//            }  
//        }); 
//        imageView.setOnLongClickListener(new View.OnLongClickListener() {  
//            @Override  
//            public boolean onLongClick(View v) {  
//                Log.e(MediaPlayDialog.class.getName(), "JJS- imageView.setOnLongClickListener");
//                return true;  
//            }  
//        }); 

    	setupView();
    }    

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	// Ignore the standard menu and launch our own.
    	launchOptionsMenu();
//        Log.e(MediaPlayDialog.class.getName(), "JJS- onCreateContextMenu()");
    }

    /**
     * Launch the options menu.
     */
	private void launchOptionsMenu() {
    	Intent launchIntent = new Intent().setClass(this, MenuListDialog.class);
	   	Bundle extras = new Bundle();
	   	MenuListBundle bundle = new MenuListBundle(getString(R.string.options),getString(R.string.cancel), 3);
	   	
        if (type == MediaConst.TYPE_IMAGE) {
	   		bundle.add(getString(R.string.details), R.drawable.details, Integer.toString(OPTION_DETAILS));
        }
	   	extras.putSerializable(MenuListDialog.BUNDLE_MENU_LIST_DIALOG, bundle);
	   	launchIntent.putExtras(extras);

        // Make it a subactivity so we know when it returns
        startActivityForResult(launchIntent, REQUEST_CODE_OPTIONS);
	}

    /**
     * Process the result of the main options activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onOptionsResult(int requestCode, int resultCode, Intent data) {

        Bundle extras = data.getExtras();
        String selectedStr = (String)extras.getSerializable(MenuListDialog.BUNDLE_MENU_LIST_RETURN);
        if (selectedStr != null) {
        	try {
				int selected = Integer.parseInt(selectedStr);
				switch(selected) {
				case OPTION_DETAILS:
			        if (type == MediaConst.TYPE_IMAGE) {
			        	String exif = MediaUtilities.buildExifDisplay(path);
			        	displayComments(exif);
			        }
					break;
				}
			} catch (NumberFormatException e) {
				Toast.makeText(this, "Invalid entry! Expected a number but received " + selectedStr, Toast.LENGTH_SHORT).show();
			} 
        }
    }

	// This is called when the screen rotates.
    // (onCreate is no longer called when screen rotates due to manifest, see: android:configChanges)
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
//	    getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
	}

    private void setupView() {
    	String name;
	  	if (imageBitmap != null) {
			imageBitmap.recycle();
			imageBitmap = null;
		}

	  	int ndx = path.lastIndexOf('/');
    	if (ndx > -1) {
    		name = path.substring(ndx + 1);
    	}
    	else {
    		name = path;
    	}
    	filenameView.setText(name);
        if (type == MediaConst.TYPE_VIDEO) {
        	headerView.setText(getString(R.string.video_header));
//        	buttonLayout.setVisibility(View.GONE);
        	emptyView.setVisibility(View.GONE);
        	imageView.setVisibility(View.GONE);
        	videoView.setVisibility(View.VISIBLE);
            mediaController = new MediaController(this);
        	videoView.setVideoPath(path);
        	videoView.setMediaController(mediaController);
        	videoView.requestFocus();
           	viewHeight = videoView.getHeight();
           	viewWidth = videoView.getWidth();
    		mediaController.show();
        }
        else if (type == MediaConst.TYPE_AUDIO) {
        	headerView.setText(getString(R.string.audio_header));
        	emptyView.setVisibility(View.VISIBLE);
        	videoView.setVisibility(View.GONE);
        	imageView.setVisibility(View.GONE);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaController = new MediaController(this);
           	viewHeight = 0;
           	viewWidth = 0;

            try {
              mediaPlayer.setDataSource(path);
              mediaPlayer.prepare();
              mediaPlayer.start();
            } catch (IOException e) {
              Log.e(MediaPlayDialog.class.getName(), "Could not open file " + path + " for playback.", e);
            }
        }
        else {
        	headerView.setText(getString(R.string.image_header));
//        	buttonLayout.setVisibility(View.GONE);
        	emptyView.setVisibility(View.GONE);
        	videoView.setVisibility(View.GONE);
        	imageView.setVisibility(View.VISIBLE);
            mediaController = null;
            setupImageView();
            imageView.requestFocus();
        }
	}

    private void setupImageView() {
       	viewHeight = imageView.getHeight();
       	viewWidth = imageView.getWidth();
        if (viewHeight == 0 || viewWidth == 0) {
        	Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        	viewWidth = display.getWidth();
        	viewHeight = display.getHeight();

        }
        imageBitmap = MediaUtilities.prepareBitmap(path, viewHeight, viewWidth);
    	BitmapDrawable drawable = new BitmapDrawable(getResources(), imageBitmap);
    	imageView.setImageDrawable(drawable);
    }
//	public void buttonClickHandler(View view) {
//
//		switch (view.getId()) {
//		case R.id.play_button:
//			break;
//		case R.id.pause_button:
//			break;
//		case R.id.stop_button:
//			break;
//		}
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	// Inflate the currently selected menu XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.media_menu, menu);
        
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.preferences:
	            // When the Help button is clicked, launch Preferences as a sub-activity
	            Intent launchPreferencesIntent = new Intent().setClass(this, Preferences.class);
	            
	            // Make it a subactivity so we know when it returns
	            startActivityForResult(launchPreferencesIntent, REQUEST_CODE_PREFERENCES);
	            return true;
        	}
        return true;
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_PREFERENCES:
	        	onPreferencesResult(requestCode, resultCode, data);
				break;
			case REQUEST_CODE_OPTIONS:
	        	onOptionsResult(requestCode, resultCode, data);
				break;
			}
		}
	}

    /**
     * Invoked for display-only comments.
     */
    private void displayComments(String comment) {
	   	CommentBundle bundle = new CommentBundle(null, comment, 12);

    	Intent launchIntent = new Intent().setClass(this, CommentDialog.class);
	   	Bundle extras = new Bundle();
	   	extras.putSerializable(CommentDialog.BUNDLE_COMMENT_DIALOG, bundle);
	   	launchIntent.putExtras(extras);
	   	startActivity(launchIntent);
	}

    /**
     * Process the result of the Preferences activity when it finishes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void onPreferencesResult(int requestCode, int resultCode, Intent data) {
//        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
//        preferenceData.setMeasurement(settings.getString(Preferences.KEY_PREFERENCE_MEASUREMENT, Preferences.DEFAULT_PREFERENCE_MEASUREMENT));
//        preferenceData.setCoordinate(settings.getString(Preferences.KEY_PREFERENCE_COORDINATE, Preferences.DEFAULT_PREFERENCE_COORDINATE));
//        Boolean bool = new Boolean(settings.getString(Preferences.KEY_PREFERENCE_COMPASS_VISIBLE, Preferences.DEFAULT_PREFERENCE_COMPASS_VISIBLE));
//        preferenceData.setCompassVisible(bool.booleanValue());
//        bool = new Boolean(settings.getString(Preferences.KEY_PREFERENCE_NAVIGATOR_VISIBLE, Preferences.DEFAULT_PREFERENCE_NAVIGATOR_VISIBLE));
//        preferenceData.setNavigatorVisible(bool.booleanValue());
//        Integer num = Integer.parseInt(settings.getString(Preferences.KEY_PREFERENCE_COMPASS_SIZE, Preferences.DEFAULT_PREFERENCE_COMPASS_SIZE));
//        preferenceData.setCompassSize(num.intValue());
//        num = Integer.parseInt(settings.getString(Preferences.KEY_PREFERENCE_NAVIGATOR_SIZE, Preferences.DEFAULT_PREFERENCE_NAVIGATOR_SIZE));
//        preferenceData.setNavigatorSize(num.intValue());
//
//        showLocationInfo();
    }

    @Override
	public void onBackPressed() {
   	 	Intent returnIntent = new Intent().setClass(this, String.class);

		setResult(RESULT_OK, returnIntent);
		super.onBackPressed();
	}

    @Override
    protected void onStop() {
    	super.onStop();
    	if (mediaPlayer != null) {
    		mediaPlayer.stop();
    		mediaPlayer.release();
    	}
	  	if (imageBitmap != null) {
			imageBitmap.recycle();
			imageBitmap = null;
		}
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      //the MediaController will hide after 3 seconds - tap the screen to make it appear again
    	if (mediaController != null) {
    		mediaController.show();
    	}

    	return false;
    }

    //--MediaPlayerControl methods----------------------------------------------------
    public void start() {
    	if (mediaPlayer != null) {
    		mediaPlayer.start();
    	}
    }

    public void pause() {
    	if (mediaPlayer != null) {
    		mediaPlayer.pause();
    	}
    }

    public int getDuration() {
    	if (mediaPlayer != null) {
    		return mediaPlayer.getDuration();
    	}
    	
    	return 0;
    }

    public int getCurrentPosition() {
    	if (mediaPlayer != null) {
    		return mediaPlayer.getCurrentPosition();
    	}

    	return 0;
    }

    public void seekTo(int i) {
    	if (mediaPlayer != null) {
    		mediaPlayer.seekTo(i);
    	}
    }

    public boolean isPlaying() {
    	if (mediaPlayer != null) {
    		return mediaPlayer.isPlaying();
    	}
    	return false;
    }

    public int getBufferPercentage() {
    	return 0;
    }

    public boolean canPause() {
    	return true;
    }

    public boolean canSeekBackward() {
    	return true;
    }

    public boolean canSeekForward() {
    	return true;
    }
    //--------------------------------------------------------------------------------

    public void onPrepared(MediaPlayer mediaPlayer) {
    	if (mediaController != null) {
    		mediaController.setMediaPlayer(this);
    		mediaController.setAnchorView(findViewById(R.id.description_layout));

    		handler.post(new Runnable() {
    			public void run() {
    				mediaController.setEnabled(true);
    				mediaController.show();
    			}
    		});
    	}
    }

    /*
     * Audio example:
     public void audioPlayer(String path, String fileName){
        //set up MediaPlayer    
        MediaPlayer mp = new MediaPlayer();
     
        try {
            mp.setDataSource(path+"/"+fileName);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            mp.prepare();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mp.start();
    }
 
 *****************************************************************
 Video Example:
     public void videoPlayer(String path, String fileName, boolean autoplay){
        //get current window information, and set format, set it up differently, if you need some special effects
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        //the VideoView will hold the video
        VideoView videoHolder = new VideoView(this);
        //MediaController is the ui control howering above the video (just like in the default youtube player).
        videoHolder.setMediaController(new MediaController(this));
        //assing a video file to the video holder
        videoHolder.setVideoURI(Uri.parse(path+"/"+fileName));
        //get focus, before playing the video.
        videoHolder.requestFocus();
        if(autoplay){
            videoHolder.start();
        }
     
     }

     */
}

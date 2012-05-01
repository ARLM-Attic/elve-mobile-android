package com.codecoretechnologies.elvemobile;

import com.codecoretechnologies.elvemobile.communication.TouchEventType;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;


public class ElveTouchScreenActivity extends Activity
{
	private ImageView _iv = null;
	
	private static final int NOTIFY_BACKGROUND_ID = 1;
	

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.elvetouchscreen);

        Log.d("TS Activity", "Entered onCreate()");
        
        try
        {	        
	        _iv = (ImageView) findViewById(R.id.ivElveTouchScreenInterface);
	
	        // TODO: Hide the Android status bar.
	        //requestWindowFeature(Window.FEATURE_NO_TITLE);
	        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	
	
	        // Set the screen orientation. This activity is configured (via AndroidManifest.xml) to handle orientation changes so that the activity isn't automatically restarted causing onDestroy() to close the connection.
	        Bitmap bm = CommunicationController.TouchScreenBitmap;
	        if (bm != null)
	        {
	        	int bmWidth = bm.getWidth();
	        	int bmHeight = bm.getHeight();
	        	
		        // Get the imageview's activity.
				//Activity activity = (Activity)(ElveTouchScreenActivity.badStaticImageViewForTesting.getContext());
				// Get screen size.
		        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		        Display display = wm.getDefaultDisplay();
		        Point screenSize = new Point(display.getWidth(), display.getHeight()); // Gets the size of the display, in pixels.
		
		        
				// If the touch screen size is the same size as the screen then do not use a ScrollViewWithTouch since it uses a touch delay that is annoying.
				if (bmWidth == screenSize.x && bmHeight == screenSize.y)
				{
					// The touch screen is the same size as the portrait screen.
					// TODO: turn OFF rotate and zoom support.
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // this is likely uneeded but lets be explicit.
				}
				else if (bmWidth == screenSize.x && bmHeight == screenSize.y) 
				{
					// The touch screen is the same size as the lanscape screen.
					// TODO: turn OFF rotate and zoom support.
					// Show the view in landscape mode.
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
				else
				{
					// TODO: turn ON rotate and zoom support.
					if (bmWidth > bmHeight)
					{
						// Show the view in landscape mode.
						this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					}
					else
					{
						// Show the view in portrait mode.
						this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // this is likely uneeded but lets be explicit.
					}
				}
	        }
	        
	        
	        // Keep the screen on if desired.
	        if (PrefsActivity.getKeepScreenOn(CommunicationController.ParentPrefsActivity) == true)
	        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	
	
	
	        _iv.setOnTouchListener(new OnTouchListener()
			{
	        	private boolean _startedTouchTransaction = false; // used to prevent sending move events when a TouchDown event has not yet been sent. 
	        	
	        	private static final long DOUBLE_TOUCH_INTERVAL = 250; // in millis
	        	private long _lastTouchUpTime;
	        	
	        	private static final long MOVE_SEND_INTERVAL = 125; // in millis
	        	private long _lastSendTouchMoveTime;
	        	
				public boolean onTouch(View v, MotionEvent event)
				{
					// Scale and offset (X,Y) based on imageview zoom and centering.
					// calculate inverse matrix
					Matrix inverse = new Matrix();
					((ImageView)v).getImageMatrix().invert(inverse);
					// map touch point from ImageView to image
					float[] touchPoint = new float[] {event.getX(), event.getY()};
					inverse.mapPoints(touchPoint);
					// touchPoint now contains x and y in image's coordinate system
					int imageX = (int)touchPoint[0];
					int imageY = (int)touchPoint[1];
	
					// Get the size of the displayed image. We call getBounds() but it really returns the size since left and top are always 0. 
					Rect imageSize = ((ImageView)v).getDrawable().getBounds();
					
					// Get current time in milliseconds.
					long actionTime = System.currentTimeMillis();
	
					// Get the event type.
					int motionEvent = event.getAction();
					
					// If the touch point is outside the image bounds then just return.
					if ((imageX < imageSize.left || imageX > imageSize.right || imageY < imageSize.top || imageY > imageSize.bottom)
						|| (motionEvent == MotionEvent.ACTION_CANCEL))
					{
						// Commented out since I believe the server can handle not receiving an up event.
						//if (_touchStartedWithInBounds && motionEvent == MotionEvent.ACTION_UP || motionEvent == MotionEvent.ACTION_CANCEL)
						//{
						//	// the protocol does not support a cancel, so since we have already sent a down event we must complete it with an up event.
						//	// end the touch transaction.
						//	// I guess we don't send the touched/double touched event?
						//	comm.SendTouchEvent(TouchEventType.TouchUp, imageX, imageY);
						//	
						//	_lastTouchUpTime = actionTime;
						//	_touchStartedWithInBounds = false;
						//}
						return true;
					}
					
									
					switch (event.getAction())
					{
						case MotionEvent.ACTION_DOWN:
							_startedTouchTransaction = true;
							CommunicationController.Comm.SendTouchEvent(TouchEventType.TouchDown, imageX, imageY);
							break;
	
						case MotionEvent.ACTION_UP: 
							if (actionTime - _lastTouchUpTime <= DOUBLE_TOUCH_INTERVAL)
							{
								Log.d("TS Activity", "DoubleTouch: " + imageX + "," + imageY);
								CommunicationController.Comm.SendTouchEvent(TouchEventType.DoubleTouch, imageX, imageY);
							}
							else
							{
								Log.d("TS Activity", "Touched: " + imageX + "," + imageY);
								CommunicationController.Comm.SendTouchEvent(TouchEventType.Touched, imageX, imageY);
							}
							
							CommunicationController.Comm.SendTouchEvent(TouchEventType.TouchUp, imageX, imageY);
							
							_lastTouchUpTime = actionTime;
							_startedTouchTransaction = false;
							break;
	
						case MotionEvent.ACTION_MOVE:
							if (_startedTouchTransaction) // don't send move commands if no original touch down was sent (happens when touching down outside the image bounds).
							{
								// Throttle the sent mouse moved commands so we don't overload the server.
								if (actionTime - _lastSendTouchMoveTime > MOVE_SEND_INTERVAL)
								{
									Log.d("TS Activity", "TouchMove: " + imageX + "," + imageY);
									CommunicationController.Comm.SendTouchEvent(TouchEventType.TouchMove, imageX, imageY);
									_lastSendTouchMoveTime = actionTime;
								}
								else
									Log.d("TS Activity", "SKIP_Move: " + imageX + "," + imageY);
							}
							
							break;
					}
	
					return true;
				}
			});
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
    	// This activity is configured (via AndroidManifest.xml) to handle orientation changes so that the activity isn't automatically restarted causing onDestroy() to close the connection.
    	Log.d("TS Activity", "Entered onConfigurationChanged()");
    	
    	super.onConfigurationChanged(newConfig);
    }
    
    @Override
    protected void onStart()
    {
    	Log.d("TS Activity", "Entered onStart()");
    	
    	super.onStart();
    }
    
    @Override
    protected void onResume()
    {
    	Log.d("TS Activity", "Entered onResume()");
    	
    	CommunicationController.TouchScreenActivity = this;

        // Get 1st image
        Bitmap bm = CommunicationController.TouchScreenBitmap;
        if (bm != null)
        	UpdateImageView(bm);
        
        
    	// Clear the Elve is running in the background notification (if it is still there).
        cancelBackgroundNotification();  
        
        
    	super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	// Close the ts activity if MENU or BACK is pressed.
        switch (keyCode)
        {
        	case KeyEvent.KEYCODE_MENU:
        	case KeyEvent.KEYCODE_BACK:
        		 
    			Log.d("TS onKeyDown", "MENU or BACK button presses, Calling CommunicationController.Close()");
    			CommunicationController.Close(); // this will close this activity as well.
        		return true;
        }
        return false;
    }
    
    @Override
    protected void onPause()
    {
    	Log.d("TS onPause", "Entered onPause()");

    	// Determine if the user wants to close communications when the app goes into the background.
    	if (isFinishing() == false)
    	{
	    	if (PrefsActivity.getRunInBackground(CommunicationController.ParentPrefsActivity) == false)
	    	{
	    		finish();
	    		CommunicationController.ReconnectOnAppEnteringForeground = true;
	    	}
	    	else
	    	{
	    		showBackgroundNotification();
	    		CommunicationController.ReconnectOnAppEnteringForeground = false;
	    	}
    	}
    	else
    		CommunicationController.ReconnectOnAppEnteringForeground = false;

    	super.onPause();
    }
    
    @Override
    protected void onStop()
    {
    	Log.d("TS onStop", "Entered onStop()");
    	
    	super.onStop();
    }

    @Override
    protected void onDestroy()
    {
    	Log.d("TS onDestroy", "Entered onDestroy()");
    	 
    	super.onDestroy();
    }


    public void UpdateImageView(final Bitmap bm)
    {
    	Log.d("TS Activity", "Entered UpdateImageView()");
    	if (_iv == null)
    		Log.d("TS Activity", "_iv IS NULL in UpdateImageView()");
    	
    	if (bm != null)
    	{
			_iv.post(new Runnable()
			{
			    public void run()
			    {
			    	try
			    	{
			    		Log.d("TS Activity", "Entered UpdateImageView().Runnable()");
			    		// NOTE: The android Bitmap docs indicates that you do not need to call bitmap.recycle() on the old imageview bitmap.
			    		// The bitmap.recycle() method ... "need not be called, since the normal GC process will free up this memory when there are no more references to this bitmap."
			    		// doesn't work -> HOWEVER I call it anyway since the images can be replaced rapidly and I don't know how fast the android garbage collector will free the bitmap memory.
			    		 
			    		//Drawable drawable = _imageView.getDrawable();
			    		
			    		Log.d("TS Activity", "Updating imageview with new bitmap.");
			    		
			    		// Set new bitmap in imageview.
				        _iv.setImageBitmap(bm);
				    	_iv.invalidate();
				    	
				    	// This causes an EXCEPTION when the 2nd image is added so I commented it out. 
	//			    	// Dispose of the old bitmap.
	//			    	if (drawable instanceof BitmapDrawable) {
	//			    	    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
	//			    	    Bitmap bitmap = bitmapDrawable.getBitmap();
	//			    	    bitmap.recycle();
	//			    	}
				    }
			    	catch (Exception ex)
			    	{
			    		ex.printStackTrace();
			    	}
			    	Log.d("TS Activity", "Exited UpdateImageView().Runnable()");
			    }
			});
    	}
    	Log.d("TS Activity", "Exited UpdateImageView()");
    }
    
    
    void showBackgroundNotification()
    {
    	//http://developer.android.com/guide/topics/ui/notifiers/notifications.html
    	
    	
    	//Get a reference to the NotificationManager
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
    	
    	// Instantiate the Notification:
    	int icon = R.drawable.icon;
    	CharSequence tickerText = "Elve Mobile is running in the background.";
    	long when = System.currentTimeMillis();

    	Notification notification = new Notification(icon, tickerText, when);
    	
    	// Define the notification's message and PendingIntent:
    	Context context = getApplicationContext();
    	CharSequence contentTitle = "Elve Mobile";
    	CharSequence contentText = "Elve Mobile is running in the background.";
    	Intent notificationIntent = new Intent(this, ElveTouchScreenActivity.class);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	// Pass the Notification to the NotificationManager:
    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    	mNotificationManager.notify(NOTIFY_BACKGROUND_ID, notification);
    }
    
    void cancelBackgroundNotification()
    {
    	// Clear the Elve is running in the background notification (if it is still there).
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
    	mNotificationManager.cancel(NOTIFY_BACKGROUND_ID);
    }
}

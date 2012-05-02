package com.codecoretechnologies.elvemobile;


import com.codecoretechnologies.elvemobile.communication.ContinueSessionResultReceivedEventArgs;
import com.codecoretechnologies.elvemobile.communication.ContinueSessionResults;
import com.codecoretechnologies.elvemobile.communication.DrawImageReceivedEventArgs;
import com.codecoretechnologies.elvemobile.communication.TouchEventType;
import com.codecoretechnologies.elvemobile.communication.TouchServiceTcpCommunicationAuthenticationResults;
import com.codecoretechnologies.elvemobile.communication.TouchTcpAuthenticationResultReceivedEventArgs;
import com.codecoretechnologies.elvemobile.communication.TouchTcpClientExceptionEventArgs;
import com.codecoretechnologies.elvemobile.communication.TouchTcpClientStateChangedEventArgs;
import com.codecoretechnologies.elvemobile.communication.TouchTcpClientUnresolvedAddressExceptionEventArgs;
import com.codecoretechnologies.elvemobile.communication.UptimeClient;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings.Secure;
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
	
	private UptimeClient _comm = null;
	private ProgressDialog _connectionProgressDialog = null;
	private EventBus _eventbus = null;
	private temporaryEventHolder _eventHandler = null;
	public  Bitmap _bitmap = null;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.elvetouchscreen);

        Log.d("TS Activity", "Entered onCreate()");
        
        
     // Show connection progress dialog
    	showProgressDialogMessage("Initializing...");
		
        // TODO: Hide the Android status bar.
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        
        
        
        // Keep the screen on if desired.
        if (PrefsActivity.getKeepScreenOn(this) == true)
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
		
        _iv = (ImageView) findViewById(R.id.ivElveTouchScreenInterface);

        
        
		// Get the unique id of this android device.  http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
        String deviceID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        // Get the device screen size.
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point(display.getWidth(), display.getHeight()); // Gets the size of the display, in pixels.

        // Session ID is stored in memory between sessions.
        byte[] sessionID = PrefsActivity.getHiddenSessionID(this);
        
        
        
        
        // Create the event bus. 
        _eventbus = new EventBus();
        // Register the communications object with the event bus (the communications object will trigger events).
        _eventHandler =  new temporaryEventHolder();
        _eventbus.register(_eventHandler);        

        
        // Create the communications object.
        _comm = new UptimeClient(PrefsActivity.getServerAddress(this), PrefsActivity.getServerPort(this), PrefsActivity.getUsername(this), PrefsActivity.getPassword(this), sessionID, deviceID, screenSize, _eventbus);
        // Start trying to connect to server and handle protocol.
        _comm.run();


        

        
        
        
        // Send location change event
        //comm.SendLocationChangeEvent(latitude, longitude, altitude, course, speedMetersPerSecond);
        
        
        
        

	
	

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
						_comm.SendTouchEvent(TouchEventType.TouchDown, imageX, imageY);
						break;

					case MotionEvent.ACTION_UP: 
						if (actionTime - _lastTouchUpTime <= DOUBLE_TOUCH_INTERVAL)
						{
							Log.d("TS Activity", "DoubleTouch: " + imageX + "," + imageY);
							_comm.SendTouchEvent(TouchEventType.DoubleTouch, imageX, imageY);
						}
						else
						{
							Log.d("TS Activity", "Touched: " + imageX + "," + imageY);
							_comm.SendTouchEvent(TouchEventType.Touched, imageX, imageY);
						}
						
						_comm.SendTouchEvent(TouchEventType.TouchUp, imageX, imageY);
						
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
								_comm.SendTouchEvent(TouchEventType.TouchMove, imageX, imageY);
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
    	
        // Get 1st image
        if (_bitmap != null)
        	updateImageView();
        
        
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
        	//case KeyEvent.KEYCODE_BACK:
        		 
    			Log.d("TS onKeyDown", "MENU button presses, Calling CommunicationController.Close()");
    	        
				showPrefsActivity();
    			
        		return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onPause()
    {
    	Log.d("TS onPause", "Entered onPause()");

    	// Determine if the user wants to close communications when the app goes into the background.
    	if (isFinishing() == false)
    	{
	    	if (PrefsActivity.getRunInBackground(this) == false)
	    		finish();
	    	else
	    		showBackgroundNotification();
    	}

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
    	 
    	
    	try
    	{
	    	_eventbus.unregister(_eventHandler);
	    	_eventHandler = null;
	    	_eventbus = null;		
	    	
	        // When done with the communications, close the object.
//		    Thread thread = new Thread(new Runnable() {
//		        public void run() {
//		            try
//					{
//		            	// Must not close on the IO thread or we get: An Executor cannot be shut down from the thread acquired from itself.  Please make sure you are not calling releaseExternalResources() from an I/O worker thread.
						_comm.close();
//					}
//					catch (IOException e)
//					{
//						e.printStackTrace();
//					}
//		            catch (Exception ex)
//			    	{			    		
//			    		ex.printStackTrace();
//			    	}
//		            finally
//			    	{
			    		_comm = null;
//			    	}
//		        }
//		    });
//		    thread.start();
//		    //thread.join(); // sync  - THIS WAS CAUSING THE THREAD TO NEVER EXIT, so I commented it out.
	    	
	    	
	    	if (_connectionProgressDialog != null)
	    	{
	    		_connectionProgressDialog.dismiss();
	    		_connectionProgressDialog = null;
	    	}
	    	
	    	_bitmap = null;
    	}
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
    	}

    	
    	super.onDestroy();
    }


    public void updateImageView()
    {
    	Log.d("TS Activity", "Entered UpdateImageView()");
    	if (_iv == null)
    		Log.d("TS Activity", "_iv IS NULL in UpdateImageView()");
    	
    	final Bitmap bm = _bitmap;
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
    	notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);  // If set, the activity will not be launched if it is already running at the top of the history stack.
    	//notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // this should be unnecessary since the only think on top of the TS activity would be a dialog.  If set, and the activity being launched is already running in the current task, then instead of launching a new instance of that activity, all of the other activities on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
    	notification.flags |= Notification.FLAG_NO_CLEAR; // prevent clearing of the flag
    	
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
    
    
    
    
    
    
	private void showProgressDialogMessage(final String message)
	{
		// TODO: perhaps hide the ImageView?
		
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				if (_connectionProgressDialog == null)
				{
					_connectionProgressDialog = new ProgressDialog(ElveTouchScreenActivity.this);
					_connectionProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					_connectionProgressDialog.setMessage(message);
					_connectionProgressDialog.setCancelable(true); // indicates if the user may cancel with the back button.
					_connectionProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
					{
						public void onDismiss(DialogInterface dialog)
						{
							_connectionProgressDialog = null;
						}
					});
					_connectionProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
					{
						public void onCancel(DialogInterface dialog)
						{
							showPrefsActivity();
						}
					});
					_connectionProgressDialog.show();
				}
				else
					_connectionProgressDialog.setMessage(message);				
			}
		});
	}
	
    void showAlert(final String message)
    {
    	runOnUiThread(new Runnable()
		{
			public void run()
			{
        		AlertDialog alertDialog = new AlertDialog.Builder(ElveTouchScreenActivity.this).create();  
        	    alertDialog.setTitle("Preferences");  
        	    alertDialog.setMessage(message);  
        	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {  
        	      public void onClick(DialogInterface dialog, int which) {  
        	    	  showPrefsActivity();
        	    } });
        	    alertDialog.setIcon(R.drawable.icon);
        	    alertDialog.show();
			}
		});
    }
	
	
    void showPrefsActivity()
    {
    	runOnUiThread(new Runnable()
		{
			public void run()
			{
    			// Show preferences activity
    			Intent intent = new Intent(ElveTouchScreenActivity.this, PrefsActivity.class);
    			startActivity( intent );
    			finish(); 
			}
		});
    }
	
	
	
	
	
	
	
	
	
	
    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        return false;
    }
    
    
    
	
	
	
	
	
	
	
	
	 class temporaryEventHolder // TODO: move the events somewhere that makes sense.
	 {
		private boolean _hasReceivedFirstImage = false;


		@Subscribe
	    public void handleTouchTcpClientUnresolvedAddressExceptionEventArgs(TouchTcpClientUnresolvedAddressExceptionEventArgs eventArgs)
	    {
			// Show the message in a closable alert.
           showAlert("The server address setting is invalid.");
	    }

	    @Subscribe
	    public void handleTouchTcpClientExceptionEventArgs(TouchTcpClientExceptionEventArgs eventArgs)
	    {
	    	// FUTURE TODO: a protocol error occured. There is probably nothing to do here since the communication object will automatically restart the connection.
	    }

	    @Subscribe
	    public void handleTouchTcpClientStateChangedEventArgs(TouchTcpClientStateChangedEventArgs eventArgs)
	    {
	    	_hasReceivedFirstImage = false; // make sure the ts activity will be shown when we receive the 1st image.

	    	String text = "";
	        switch (eventArgs.State)
	        {
	            case AttemptingToConnect:
	                text = "Connecting...";
	                break;
	            case AttemptingToReconnect:
	                text = "Reconnecting...";
	                break;
	            case Authenticating:
	                text = "Logging On...";
	                break;
	            case Authenticated:
	                text = "Retrieving Interface...";
	                break;
	            case FailedAuthentication:
	                //text = "Unsuccessful Authentication."; 
	                //break;
	            	return; // the handleTouchTcpAuthenticationResultReceivedEventArgs() event will handle this situation.
	        }
	
	    	showProgressDialogMessage(text);
	    }
	
	    @Subscribe
	    public void handleContinueSessionResultReceivedEventArgs(ContinueSessionResultReceivedEventArgs eventArgs)
	    {
	    	if (eventArgs.Result == ContinueSessionResults.Success)
	    	{
	    		// Create a graphics canvas of the specified size.
	    		_bitmap = Bitmap.createBitmap(eventArgs.TouchScreenSize.x, eventArgs.TouchScreenSize.y, Bitmap.Config.ARGB_8888);
	    	}
	    }

	    @Subscribe
	    public void handleTouchTcpAuthenticationResultReceivedEventArgs(TouchTcpAuthenticationResultReceivedEventArgs eventArgs)
	    {
			if (eventArgs.Result != TouchServiceTcpCommunicationAuthenticationResults.Success)
	        {
	            String message = "";
	            if (eventArgs.Result == TouchServiceTcpCommunicationAuthenticationResults.Invalid)
	                message = "The username or password is incorrect.";
	            else if (eventArgs.Result == TouchServiceTcpCommunicationAuthenticationResults.Disabled)
	                message = "The user account has been disabled.";
	            else if (eventArgs.Result == TouchServiceTcpCommunicationAuthenticationResults.BlockedByTimeRestrictions)
	                message = "The user account is currently blocked by time restrictions, please try again later.";

	            _connectionProgressDialog.dismiss();
	            
	            // Show the message in a closable alert.
	            showAlert(message);

	            return;
	        }
	    	else
	    	{
	    		// Save the session id.
	    		PrefsActivity.setHiddenSessionID(ElveTouchScreenActivity.this, eventArgs.SessionID);

	    		// Create a graphics canvas of the specified size.
	    		_bitmap = Bitmap.createBitmap(eventArgs.TouchScreenSize.x, eventArgs.TouchScreenSize.y, Bitmap.Config.ARGB_8888);
	    	}
	    }


	
	    @Subscribe
	    public void handleDrawImageReceivedEventArgs(DrawImageReceivedEventArgs eventArgs)
	    {
	    	Log.d("handleDrawImageReceivedEventArgs", "Entered handleDrawImageReceivedEventArgs()");
	    	try
	    	{
		    	//System.err.println("Received image: " + eventArgs.Image.getWidth() + " x " + eventArgs.Image.getHeight());
		    	Log.d("handleDrawImageReceivedEventArgs", "Received image: " + eventArgs.Image.getWidth() + " x " + eventArgs.Image.getHeight());
		
		    	
		    	
		
		    	// FUTURE TODO: eventArgs.SizeMode will always be Normal in Snapshot mode, but if granular command mode support is added then we need to support it!
		    	
		    	// Draw the snapshot.
		    	Canvas canvas = new Canvas(_bitmap);
		    	Paint paint = new Paint();
		    	if (eventArgs.Bounds.width() != eventArgs.Image.getWidth() || eventArgs.Bounds.height() != eventArgs.Image.getHeight())
		    	{
		    		float sx = eventArgs.Bounds.width() / eventArgs.Image.getWidth();
		    		float sy = eventArgs.Bounds.height() / eventArgs.Image.getHeight();
		    		canvas.scale(sx, sy);
		    		paint.setFilterBitmap(true); // use filter to smooth any resizing.
		    	}
		    	if (eventArgs.Opacity != 255)
		    		paint.setAlpha(eventArgs.Opacity); //0-255
		    	if (eventArgs.Bounds.left != 0 || eventArgs.Bounds.top != 0)
		    		canvas.translate(eventArgs.Bounds.left, eventArgs.Bounds.top);
		    	canvas.drawBitmap(eventArgs.Image, new Matrix(), paint);
		    	//canvas.drawBitmap(eventArgs.Image, null, eventArgs.Bounds, paint); // This is simpler than above but I don't know how it works internally and if there is a performance hit if we aren't actually scaling (which will will likely never be doing).
		
		    	
		    	if (_hasReceivedFirstImage == false)
		    	{
		    		_hasReceivedFirstImage = true;
		    	
		    		runOnUiThread(new Runnable()
		    		{
		    			public void run()
		    			{
		    				Log.d("handleDrawImageReceivedEventArgs", "Entered handleDrawImageReceivedEventArgs().Runnable()");
				    		// Close the progress dialog.
			    			_connectionProgressDialog.dismiss();
				    	
			    			
			    			
			    			
			    			
			    			// Set the screen orientation. This activity is configured (via AndroidManifest.xml) to handle orientation changes so that the activity isn't automatically restarted causing onDestroy() to close the connection.
			    	        if (_bitmap != null)
			    	        {
			    	        	int bmWidth = _bitmap.getWidth();
			    	        	int bmHeight = _bitmap.getHeight();
			    	        	
			    		        // Get the imageview's activity.
			    				//Activity activity = (Activity)(ElveTouchScreenActivity.badStaticImageViewForTesting.getContext());
			    				// Get screen size.
			    		        WindowManager wm = (WindowManager) ElveTouchScreenActivity.this.getSystemService(Context.WINDOW_SERVICE);
			    		        Display display = wm.getDefaultDisplay();
			    		        Point screenSize = new Point(display.getWidth(), display.getHeight()); // Gets the size of the display, in pixels.
			    		
			    		        
			    				// If the touch screen size is the same size as the screen then do not use a ScrollViewWithTouch since it uses a touch delay that is annoying.
			    				if (bmWidth == screenSize.x && bmHeight == screenSize.y)
			    				{
			    					// The touch screen is the same size as the portrait screen.
			    					// TODO: turn OFF rotate and zoom support.
			    					ElveTouchScreenActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // this is likely uneeded but lets be explicit.
			    				}
			    				else if (bmWidth == screenSize.x && bmHeight == screenSize.y) 
			    				{
			    					// The touch screen is the same size as the lanscape screen.
			    					// TODO: turn OFF rotate and zoom support.
			    					// Show the view in landscape mode.
			    					ElveTouchScreenActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			    				}
			    				else
			    				{
			    					// TODO: turn ON rotate and zoom support.
			    					if (bmWidth > bmHeight)
			    					{
			    						// Show the view in landscape mode.
			    						ElveTouchScreenActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			    					}
			    					else
			    					{
			    						// Show the view in portrait mode.
			    						ElveTouchScreenActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // this is likely uneeded but lets be explicit.
			    					}
			    				}
			    	        }
		    			}
		    		});
		    	}

		    	Log.d("handleDrawImageReceivedEventArgs", "Updating TS BM");
		    	updateImageView();
	    	}
	    	catch (Exception ex)
	    	{
	    		ex.printStackTrace();
	    	}
	    	Log.d("handleDrawImageReceivedEventArgs", "Exited handleDrawImageReceivedEventArgs()");
	    }
	}
}

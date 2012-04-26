package com.codecoretechnologies.elvemobile;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import com.codecoretechnologies.elvemobile.communication.*;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;



public class CommunicationTest
{
    public static void test(Context context) throws Exception
    {	
		// Get the unique id of this android device.  http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
        String deviceID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

        // Get the device screen size.
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point(display.getWidth(), display.getHeight()); // Gets the size of the display, in pixels.

        // TODO: session ID should be stored in memory somewhere between backgound/foreground switches to continue the prior session.
        byte[] sessionID = null;
        
        // Create the event bus. 
        EventBus eventBus = new EventBus();
        // Create the communications object.
        final UptimeClient comm = new UptimeClient("192.168.1.3", 33907, "admin", "admin", sessionID, deviceID, screenSize, eventBus);
        // Register the communications object with the event bus (the communications object will trigger events).
        eventBus.register(new temporaryEventHolder());        
        // Start trying to connect to server and handle protocol.
        comm.run();


        ElveMobileActivity.badStaticImageViewForTesting.setOnTouchListener(new OnTouchListener()
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
						comm.SendTouchEvent(TouchEventType.TouchDown, imageX, imageY);
						break;

					case MotionEvent.ACTION_UP: 
						if (actionTime - _lastTouchUpTime <= DOUBLE_TOUCH_INTERVAL)
						{
							Log.d("", "DoubleTouch: " + imageX + "," + imageY);
							comm.SendTouchEvent(TouchEventType.DoubleTouch, imageX, imageY);
						}
						else
						{
							Log.d("", "Touched: " + imageX + "," + imageY);
							comm.SendTouchEvent(TouchEventType.Touched, imageX, imageY);
						}
						
						comm.SendTouchEvent(TouchEventType.TouchUp, imageX, imageY);
						
						_lastTouchUpTime = actionTime;
						_startedTouchTransaction = false;
						break;

					case MotionEvent.ACTION_MOVE:
						if (_startedTouchTransaction) // don't send move commands if no original touch down was sent (happens when touching down outside the image bounds).
						{
							// Throttle the sent mouse moved commands so we don't overload the server.
							if (actionTime - _lastSendTouchMoveTime > MOVE_SEND_INTERVAL)
							{
								Log.d("", "TouchMove: " + imageX + "," + imageY);
								comm.SendTouchEvent(TouchEventType.TouchMove, imageX, imageY);
								_lastSendTouchMoveTime = actionTime;
							}
							else
								Log.d("", "SKIP_Move: " + imageX + "," + imageY);
						}
						
						break;
				}

				return true;
			}
		});

        
        
        // Wait for handleTouchTcpClientStateChangedEventArgs to indicate authenticated...
        
        
        // Send location change event
        //comm.SendLocationChangeEvent(latitude, longitude, altitude, course, speedMetersPerSecond);
        
        // When done with the communications, close the object.
        //comm.close();
    }
    
    public static  boolean isNetworkAvailable(Context context)
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
}


    
    
 class temporaryEventHolder // TODO: move the events somewhere that makes sense.
 {
	private Bitmap _touchScreenImage = null;
	private boolean _hasReceivedFirstImage = false;
	 
    @Subscribe
    public void handleTouchTcpClientExceptionEventArgs(TouchTcpClientExceptionEventArgs eventArgs)
    {
    	// FUTURE TODO: a protocol error occured. There is probably nothing to do here since the communication object will automatically restart the connection.
    }

    @Subscribe
    public void handleTouchTcpClientStateChangedEventArgs(TouchTcpClientStateChangedEventArgs eventArgs)
    {
    	String text = "";
        switch (eventArgs.State)
        {
            case AttemptingToConnect:
                text = "Connecting...";
                break;
            case Authenticating:
                text = "Logging On...";
                break;
            case Authenticated:
                text = "Retrieving Interface...";
                break;
            case FailedAuthentication:
                text = "Unsuccessful Authentication.";
                break;
        }

    	// TODO: show status activity with the above text.
    }

    @Subscribe
    public void handleContinueSessionResultReceivedEventArgs(ContinueSessionResultReceivedEventArgs eventArgs)
    {
    	if (eventArgs.Result == ContinueSessionResults.Success)
    	{
    		// Create a graphics canvas of the specified size.
    		_touchScreenImage = Bitmap.createBitmap(eventArgs.TouchScreenSize.x, eventArgs.TouchScreenSize.y, Bitmap.Config.ARGB_8888);
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

            // TODO: show settings activity with above message

            return;
        }
    	else
    	{	    		
    		// Create a graphics canvas of the specified size.
    		_touchScreenImage = Bitmap.createBitmap(eventArgs.TouchScreenSize.x, eventArgs.TouchScreenSize.y, Bitmap.Config.ARGB_8888);
    	}
    }

    @Subscribe
    public void handleDrawImageReceivedEventArgs(DrawImageReceivedEventArgs eventArgs)
    {
    	//System.err.println("Received image: " + eventArgs.Image.getWidth() + " x " + eventArgs.Image.getHeight());
    	Log.d("handleDrawImageReceivedEventArgs", "Received image: " + eventArgs.Image.getWidth() + " x " + eventArgs.Image.getHeight());

    	if (_hasReceivedFirstImage == false)
    	{
    		_hasReceivedFirstImage = true;
    		// TODO: Show the touch screen interface activity after setting the image.
    		
    		// Get the imageview's activity.
    		Activity activity = (Activity)(ElveMobileActivity.badStaticImageViewForTesting.getContext());
    		// Get screen size.
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point screenSize = new Point(display.getWidth(), display.getHeight()); // Gets the size of the display, in pixels.

            
    		// If the touch screen size is the same size as the screen then do not use a ScrollViewWithTouch since it uses a touch delay that is annoying.
			if (_touchScreenImage.getWidth() == screenSize.x && _touchScreenImage.getHeight() == screenSize.y)
			{
				// The touch screen is the same size as the portrait screen.
				// TODO: turn OFF rotate and zoom support.
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // this is likely uneeded but lets be explicit.
			}
			else if (_touchScreenImage.getWidth() == screenSize.x && _touchScreenImage.getHeight() == screenSize.y) 
			{
				// The touch screen is the same size as the lanscape screen.
				// TODO: turn OFF rotate and zoom support.
				// Show the view in landscape mode.
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			else
			{
				// TODO: turn ON rotate and zoom support.
				if (_touchScreenImage.getWidth() > _touchScreenImage.getHeight())
				{
					// Show the view in landscape mode.
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
				else
				{
					// Show the view in portrait mode.
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // this is likely uneeded but lets be explicit.
				}
			}
    	}

    	// FUTURE TODO: eventArgs.SizeMode will always be Normal in Snapshot mode, but if granular command mode support is added then we need to support it!
    	
    	// Draw the snapshot.
    	Canvas canvas = new Canvas(_touchScreenImage);
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

    	// NOTE: There are a variety of ways to update the UI on the UI thread: http://developer.android.com/resources/articles/painless-threading.html
    	// TODO: get imageview from good place
    	final ImageView _imageView = ElveMobileActivity.badStaticImageViewForTesting;
		_imageView.post(new Runnable()
		{
		    public void run()
		    {
		    	try
		    	{
		    		// NOTE: The android Bitmap docs indicates that you do not need to call bitmap.recycle() on the old imageview bitmap.
		    		// The bitmap.recycle() method ... "need not be called, since the normal GC process will free up this memory when there are no more references to this bitmap."
		    		// doesn't work -> HOWEVER I call it anyway since the images can be replaced rapidly and I don't know how fast the android garbage collector will free the bitmap memory.
		    		 
		    		//Drawable drawable = _imageView.getDrawable();
		    		
		    		// Set new bitmap in imageview.
			    	_imageView.setImageBitmap(_touchScreenImage);
			    	_imageView.invalidate();
			    	
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
		    		;
		    	}
		    }
		});

    }
}

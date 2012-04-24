package com.codecoretechnologies.elvemobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

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
        Point screenSize = new Point(display.getWidth(), display.getHeight());

        // TODO: session ID should be stored in memory somewhere between backgound/foreground switches to continue the prior session.
        byte[] sessionID = null;
        
        // Create the event bus. 
        EventBus eventBus = new EventBus();
        // Create the communications object.
        UptimeClient comm = new UptimeClient("192.168.1.3", 33907, "admin", "admin", sessionID, deviceID, screenSize, eventBus);
        // Register the communications object with the event bus (the communications object will trigger events).
        eventBus.register(new temporaryEventHolder());        
        // Start trying to connect to server and handle protocol.
        comm.run();
        
        
        // Wait for handleTouchTcpClientStateChangedEventArgs to indicate authenticated...
        
        
        // Send a touch event
        //comm.SendTouchEvent(eventType, x, y);
        
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
    		// TODO: Switch to the touch screen interface activity after setting the image.
    		// TODO: Compare the elve touchscreen interface size with the screen size:
    		//       If the interface is wide, not tall, then rotate the imageview +/-90 degrees. 
    		//       If they match then do not allow scrolling or zoom.
    		//       If they do not match then support scrolling and zoom and use an initial zoom so the entire touch screen is visible and spans to edge of screen.
    		//       Does android return points or pixels, ie is the reported screen size the actual pixel size or a scaled size?
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

    	// TODO: Update the Image View widget with the new image (is there a refresh() or do we reset it?).
    	// NOTE: There are a variety of ways to update the UI on the UI thread: http://developer.android.com/resources/articles/painless-threading.html
//		_imageView.post(new Runnable() {
//		    public void run() {
//		    	_imageView.setImageBitmap(_touchScreenImage);
//		    	_imageView.invalidate();
//		    }
//		});

    }
}

package com.codecoretechnologies.elvemobile;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
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
import android.os.AsyncTask;
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



public class CommunicationController
{
	// TODO: This currently uses a static controller pattern... which I don't normally ever do. Is there a better recommended design pattern for handling the activities, comm object, and event bus? 
	public static PrefsActivity ParentPrefsActivity;
	public static ProgressDialog ConnectionProgressDialog = null;
	public static Bitmap TouchScreenBitmap = null;
	public static ElveTouchScreenActivity TouchScreenActivity = null;
	public static UptimeClient Comm = null;
	public static boolean ReconnectOnAppEnteringForeground = false; // Indicates if the app was closed when the app went into the background and therefore it should reconnect when the app comes back to the foreground.
	private static boolean _isClosing = false;
	private static EventBus _eventbus = null;
	private static temporaryEventHolder _eventHandler = null;


    public static void Start(PrefsActivity parentActivity) throws Exception
    {
    	ParentPrefsActivity = parentActivity;
    	
    	// Show connection progress dialog
    	showProgressDialogMessage("Initializing...");
		
		
		
		
		// Get the unique id of this android device.  http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
        String deviceID = Secure.getString(parentActivity.getContentResolver(), Secure.ANDROID_ID);

        // Get the device screen size.
        WindowManager wm = (WindowManager) parentActivity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point(display.getWidth(), display.getHeight()); // Gets the size of the display, in pixels.

        // Session ID is stored in memory between sessions.
        byte[] sessionID = PrefsActivity.getHiddenSessionID(parentActivity);
        
        
        
        
        // Create the event bus. 
        _eventbus = new EventBus();
        // Create the communications object.
        //final UptimeClient comm = new UptimeClient("192.168.1.3", 33907, "admin", "admin", sessionID, deviceID, screenSize, eventBus);
        Comm = new UptimeClient(PrefsActivity.getServerAddress(parentActivity), PrefsActivity.getServerPort(parentActivity), PrefsActivity.getUsername(parentActivity), PrefsActivity.getPassword(parentActivity), sessionID, deviceID, screenSize, _eventbus);
        // Register the communications object with the event bus (the communications object will trigger events).
        _eventHandler =  new temporaryEventHolder();
        _eventbus.register(_eventHandler);        
        // Start trying to connect to server and handle protocol.
        Comm.run();


        

        
        
        // Wait for handleTouchTcpClientStateChangedEventArgs to indicate authenticated...
        
        
        // Send location change event
        //comm.SendLocationChangeEvent(latitude, longitude, altitude, course, speedMetersPerSecond);
    }
    
    public static void Close()
    {
    	Log.d("CommunicationController", "entered Close(). (exit won't be not logged)");
    	if (_isClosing)
    		return;
    	
    	try
    	{
	    	_isClosing = true;
	    	

	    	_eventbus.unregister(_eventHandler);
	    	_eventHandler = null;
	    	_eventbus = null;		
	    	
	        // When done with the communications, close the object.
		    Thread thread = new Thread(new Runnable() {
		        public void run() {
		            try
					{
		            	// Must not close on the IO thread or we get: An Executor cannot be shut down from the thread acquired from itself.  Please make sure you are not calling releaseExternalResources() from an I/O worker thread.
						Comm.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
		            catch (Exception ex)
			    	{			    		
			    		ex.printStackTrace();
			    	}
		            finally
			    	{
			    		Comm = null;
			    	}
		        }
		    });
		    thread.start();
		    //thread.join(); // sync  - THIS WAS CAUSING THE THREAD TO NEVER EXIT, so I commented it out.
	    	
	    	
	    	
	    	dismissConnectionProgressDialog();
	    	
	    	finishTouchScreenActivity();
	    	
	    	TouchScreenBitmap = null;

	    	ParentPrefsActivity = null;
    	}
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	finally
    	{
    		_isClosing = false;
    	}
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

	private static void showProgressDialogMessage(final String message)
	{
		// Are we closing?
		if (_isClosing)
			return;
		
		// If we are showing a progress mesasge then close the ts activity.
		finishTouchScreenActivity();
		
		ParentPrefsActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				if (ConnectionProgressDialog == null)
				{
					ConnectionProgressDialog = new ProgressDialog(ParentPrefsActivity);
			    	ConnectionProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			    	ConnectionProgressDialog.setMessage(message);
			    	ConnectionProgressDialog.setCancelable(true); // indicates if the user may cancel with the back button.
			    	ConnectionProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
					{
						public void onDismiss(DialogInterface dialog)
						{
							ConnectionProgressDialog = null;
						}
					});
			    	ConnectionProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
					{
						public void onCancel(DialogInterface dialog)
						{
							CommunicationController.Close();
						}
					});
			    	ConnectionProgressDialog.show();
				}
				else
					ConnectionProgressDialog.setMessage(message);				
			}
		});
	}
	
    
    static void showAlert(final String message, final Activity activity)
    {
    	activity.runOnUiThread(new Runnable()
		{
			public void run()
			{
        		AlertDialog alertDialog = new AlertDialog.Builder(activity).create();  
        	    alertDialog.setTitle("Preferences");  
        	    alertDialog.setMessage(message);  
        	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {  
        	      public void onClick(DialogInterface dialog, int which) {  
        	        return;  
        	    } });
        	    alertDialog.setIcon(R.drawable.icon);
        	    alertDialog.show();
			}
		});
    }
        
    

	private static void finishTouchScreenActivity()
	{
    	final ElveTouchScreenActivity ts = TouchScreenActivity;
    	if (ts != null)
    	{
    		ParentPrefsActivity.runOnUiThread(new Runnable()
    		{
    			public void run()
    			{
		    		ts.finish(); // although we may currently be in the ts activity's onDestroy method, finish seems to run fine and does not invoke onDestroy again.
		    		TouchScreenActivity = null; // the ts activity will already set the TouchScreenActivity variable to null, but lets be explicit.
    			}
    		});
    	}	
	}
	
    
    private static void dismissConnectionProgressDialog()
    {
    	final ProgressDialog pd = ConnectionProgressDialog;
    	if (pd != null)
    	{
    		ParentPrefsActivity.runOnUiThread(new Runnable()
    		{
    			public void run()
    			{
    				ConnectionProgressDialog.dismiss();
    				ConnectionProgressDialog = null;
    			}
    		});
    	}
    }

    
	
	 static class temporaryEventHolder // TODO: move the events somewhere that makes sense.
	 {
		private boolean _hasReceivedFirstImage = false;


		@Subscribe
	    public void handleTouchTcpClientUnresolvedAddressExceptionEventArgs(TouchTcpClientUnresolvedAddressExceptionEventArgs eventArgs)
	    {
            // Show the message in a closable alert.
            showAlert("The server address setting is invalid.", CommunicationController.ParentPrefsActivity);
            
            Close();
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
	    		TouchScreenBitmap = Bitmap.createBitmap(eventArgs.TouchScreenSize.x, eventArgs.TouchScreenSize.y, Bitmap.Config.ARGB_8888);
	    	}
	    }
	    
	    @Subscribe
	    public void handleTouchTcpAuthenticationResultReceivedEventArgs(TouchTcpAuthenticationResultReceivedEventArgs eventArgs)
	    {
			// Are we closing?
			if (_isClosing)
				return;

			if (eventArgs.Result != TouchServiceTcpCommunicationAuthenticationResults.Success)
	        {
	            String message = "";
	            if (eventArgs.Result == TouchServiceTcpCommunicationAuthenticationResults.Invalid)
	                message = "The username or password is incorrect.";
	            else if (eventArgs.Result == TouchServiceTcpCommunicationAuthenticationResults.Disabled)
	                message = "The user account has been disabled.";
	            else if (eventArgs.Result == TouchServiceTcpCommunicationAuthenticationResults.BlockedByTimeRestrictions)
	                message = "The user account is currently blocked by time restrictions, please try again later.";
	

	            // Show the message in a closable alert.
	            showAlert(message, CommunicationController.ParentPrefsActivity);
	            
	            Close();
	            
	            return;
	        }
	    	else
	    	{
	    		// Save the session id.
	    		PrefsActivity.setHiddenSessionID(ParentPrefsActivity, eventArgs.SessionID);

	    		// Create a graphics canvas of the specified size.
	    		TouchScreenBitmap = Bitmap.createBitmap(eventArgs.TouchScreenSize.x, eventArgs.TouchScreenSize.y, Bitmap.Config.ARGB_8888);
	    	}
	    }


	
	    @Subscribe
	    public void handleDrawImageReceivedEventArgs(DrawImageReceivedEventArgs eventArgs)
	    {
	    	Log.d("handleDrawImageReceivedEventArgs", "Entered handleDrawImageReceivedEventArgs()");
	    	try
	    	{
				// Are we closing?
				if (_isClosing)
					return;
	
		    	//System.err.println("Received image: " + eventArgs.Image.getWidth() + " x " + eventArgs.Image.getHeight());
		    	Log.d("handleDrawImageReceivedEventArgs", "Received image: " + eventArgs.Image.getWidth() + " x " + eventArgs.Image.getHeight());
		
		    	
		    	
		
		    	// FUTURE TODO: eventArgs.SizeMode will always be Normal in Snapshot mode, but if granular command mode support is added then we need to support it!
		    	
		    	// Draw the snapshot.
		    	Canvas canvas = new Canvas(TouchScreenBitmap);
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
		    	
		    		ParentPrefsActivity.runOnUiThread(new Runnable()
		    		{
		    			public void run()
		    			{
		    				Log.d("handleDrawImageReceivedEventArgs", "Entered handleDrawImageReceivedEventArgs().Runnable()");
				    		// Close the progress dialog.
			    			ConnectionProgressDialog.dismiss();
				    	
			    			Log.d("handleDrawImageReceivedEventArgs", "Starting TS Activity");
					        Intent myIntent = new Intent(ConnectionProgressDialog.getContext(), ElveTouchScreenActivity.class);
					        ConnectionProgressDialog.getContext().startActivity(myIntent);
					        Log.d("handleDrawImageReceivedEventArgs", "Exiting handleDrawImageReceivedEventArgs().Runnable()");
		    			}
		    		});
		    	}
		    	else
		    	{
		    		Log.d("handleDrawImageReceivedEventArgs", "Updating TS BM");
		    		ElveTouchScreenActivity tsActivity = TouchScreenActivity;
		    		if (tsActivity != null) // Due to multi-threading, the activity may not be ready and the variable not set yet.
		    			tsActivity.UpdateImageView(TouchScreenBitmap);
		    	}
	    	}
	    	catch (Exception ex)
	    	{
	    		ex.printStackTrace();
	    	}
	    	Log.d("handleDrawImageReceivedEventArgs", "Exited handleDrawImageReceivedEventArgs()");
	    }
	}
}

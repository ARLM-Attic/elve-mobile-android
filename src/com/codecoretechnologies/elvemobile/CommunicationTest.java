package com.codecoretechnologies.elvemobile;

import android.graphics.Point;
import android.provider.Settings.Secure;

import com.codecoretechnologies.elvemobile.communication.*;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;



public class CommunicationTest
{
    public void test(String[] args) throws Exception
    {
		// Get the unique id of this android device.  http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
        //String deviceID = Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);
        String deviceID = "android-device-id";


        // Create the event bus. 
        EventBus eventBus = new EventBus();
        // Create the communications object.
        UptimeClient comm = new UptimeClient("192.168.1.3", 33907, "admin", "admin", null, deviceID, new Point(320, 480), eventBus);
        // Register the communications object with the event bus (the communications object will trigger events).
        eventBus.register(new temporaryEventHolder());        
        // Start trying to connect to server and handle protocol.
        comm.run();
        
        // Send a touch event
        //comm.SendTouchEvent(eventType, x, y);
        
        // Send location change event
        //comm.SendLocationChangeEvent(latitude, longitude, altitude, course, speedMetersPerSecond);
        
        // When done with the communications, close the object.
        comm.close();
    }
}


    
    
 class temporaryEventHolder // TODO: move the events somewhere that makes sense.
 {
    @Subscribe
    public void handleTouchTcpClientExceptionEventArgs(TouchTcpClientExceptionEventArgs eventArgs)
    {
    	// TODO: a protocol error occured. There is probably nothing to do here since the communication object will automatically restart the connection.
    }

    @Subscribe
    public void handleTouchTcpClientStateChangedEventArgs(TouchTcpClientStateChangedEventArgs eventArgs)
    {
    	// TODO: show status screen, show current state in message AttemptingToConnect, Authenticating, Authenticated, FailedAuthentication
    }

    @Subscribe
    public void handleContinueSessionResultReceivedEventArgs(ContinueSessionResultReceivedEventArgs eventArgs)
    {
    	// TODO: if it returned successful then create a graphics canvas of the specified size, otherwise do nothing.
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

            // TODO: show settings form with message

            return;
        }
    	else
    	{	    		
    		// TODO: create a graphics canvas of the specified size.
    	}
    }
    
    @Subscribe
    public void handleDrawImageReceivedEventArgs(DrawImageReceivedEventArgs eventArgs)
    {
    	// TODO: paint the received image onto the canvas and update screen
    	System.err.println("Received image: " + eventArgs.Image.getWidth() + " x " + eventArgs.Image.getHeight());
    }
    
    

}

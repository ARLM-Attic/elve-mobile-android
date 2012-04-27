package com.codecoretechnologies.elvemobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

public class PrefsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        setContentView(R.layout.prefs);
        
        
        // Handle Connect button click.
        final Button button = (Button) findViewById(R.id.btnConnect);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            	if (isComplete())
            		connect();
            	else
            	{
            		AlertDialog alertDialog = new AlertDialog.Builder(PrefsActivity.this).create();  
            	    alertDialog.setTitle("Preferences");  
            	    alertDialog.setMessage("You must enter a server, port, and username.");  
            	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {  
            	      public void onClick(DialogInterface dialog, int which) {  
            	        return;  
            	    } });
            	    alertDialog.setIcon(R.drawable.icon);
            	    alertDialog.show();
            	}
            }
        });
        
        
        // If the preferences are already filled out then call connect().
        if (isComplete())
        	connect();
    }
        
    void connect()
    {
    	// TODO: show popup dialog activity with status
    	// TODO: add connection logic here or whereever makes sense
    	try
    	{
	    	
	    	
//	    	ProgressDialog progressDialog = (ProgressDialog) new ProgressDialog.Builder(PrefsActivity.this).create();  
//	    	progressDialog.setTitle("Preferences");  
//	    	progressDialog.setMessage("Connecting.");  
//	    	progressDialog.setButton("OK", new DialogInterface.OnClickListener() {  
//		      public void onClick(DialogInterface dialog, int which) {  
//		        return;  
//		    } });
//	    	progressDialog.setIcon(R.drawable.icon);
//	    	progressDialog.show();
    		
    		ProgressDialog dialog = ProgressDialog.show(PrefsActivity.this, "", "Connecting...", true);
    		dialog.show();
	    	
	    	
	    	
	    	// TODO: don't show the touch screen until we have connected. I just have it here for testing
	        //Intent myIntent = new Intent(PrefsActivity.this, ElveTouchScreenActivity.class);
	        //startActivity(myIntent);
    	}
    	catch (Exception ex)
    	{
    		int i =1;
    	}
    }
    
    boolean isComplete()
    {
    	if (getServerAddress(this) != ""
    			&& getPort(this) != 0
    			&& getUsername(this) != "")
    		return true;
    	
    	return false;
    }

    static public String getServerAddress(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("pref_serverAddress", "");
    }
    
    static public int getPort(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try
        {
        	String strPort = prefs.getString("pref_port", "33907");
        	int port = Integer.parseInt(strPort);
        	return port;
        }
        catch(Exception ex)
        {
        	return 33907;
        }
    }
    
    static public String getUsername(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("pref_username", "");
    }

    static public String getPassword(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("pref_password", "");
    }

}

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
import android.util.Base64;
import android.util.Log;
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
    }


//    @Override
//    protected void onRestart()
//    {
//        // If the preferences are already filled out then call connect().
//    	if (CommunicationController.ReconnectOnAppEnteringForeground == true)
//    	{
//	    	runOnUiThread(new Runnable()
//			{
//				public void run()
//				{
//			        if (isComplete())
//			        	connect();
//				}
//			});
//    	}
//        
//        super.onRestart();
//    }
    
    @Override
    protected void onPause()
    {
    	Log.d("Prefs", "onPause()");
    	super.onPause();
    }
    
    @Override
    protected void onStop()
    {
    	Log.d("Prefs", "onStop()");
    	super.onStop();
    }
    
    @Override
    protected void onDestroy()
    {
    	Log.d("Prefs", "onDestroy()");
    	super.onDestroy();
    }
        
    void connect()
    {
        Intent intent = new Intent(this, ElveTouchScreenActivity.class);
		startActivity( intent );
		finish(); 
    }

    boolean isComplete()
    {
    	return isComplete(this);
    }

    static public boolean isComplete(Context context)
    {
    	if (getServerAddress(context) != ""
    			&& getServerPort(context) != 0
    			&& getUsername(context) != "")
    		return true;
    	
    	return false;
    }

    static public String getServerAddress(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("pref_serverAddress", "");
    }
    
    static public int getServerPort(Context context) {
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
    
    static public boolean getRunInBackground(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pref_runInBackground", true);
    }
    
    static public boolean getKeepScreenOn(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pref_keepScreenOn", true);
    }
    
    
    static public byte[] getHiddenSessionID(Context context) {
    	try
    	{
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	        String strSessionID = prefs.getString("pref_hidden_sessionid", "");
	        
	        if (strSessionID == "")
	        	return null;
	        else
	        	return Base64.decode(strSessionID, Base64.DEFAULT);
    	}
    	catch (Exception ex)
    	{
    		// no big deal, just start a new session.
    		return null;
    	}
    }
    
    static public void setHiddenSessionID(Context context, byte[] sessionID) {
    	try
    	{
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	        SharedPreferences.Editor editor = prefs.edit();
	        
	        String strSessionID;
	        if (sessionID == null || sessionID.length == 0)
	        	strSessionID = "";
	        else
	        	strSessionID = Base64.encodeToString(sessionID, Base64.DEFAULT);
	
	        editor.putString("pref_hidden_sessionid", strSessionID);
	        editor.commit();
    	}
    	catch (Exception ex)
    	{
    		// no big deal
    	}
    }

}

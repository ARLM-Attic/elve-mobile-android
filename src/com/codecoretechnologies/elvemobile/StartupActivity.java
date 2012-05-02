package com.codecoretechnologies.elvemobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartupActivity extends Activity
{
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.????); // there is no layout for this activity since it just opens a different activity.
        
        Intent intent;
        if (PrefsActivity.isComplete(this))
        	intent = new Intent(this, ElveTouchScreenActivity.class);
        else
        	intent = new Intent(this, PrefsActivity.class);
		startActivity( intent );
		finish(); 
    }
}


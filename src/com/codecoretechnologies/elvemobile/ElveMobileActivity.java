package com.codecoretechnologies.elvemobile;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;


public class ElveMobileActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        badStaticImageViewForTesting = (ImageView) findViewById(R.id.ivElveTouchScreenInterface);
        
        // test communications
        try
		{
        	boolean result = CommunicationTest.isNetworkAvailable(this); // I don't know that this really ever needs to be called since the communication object will keep trying to reconnect. need to test.
			CommunicationTest.test(this);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    // TODO: THIS IS ONLY FOR TESTING!!! I assume the imageview will be disposed when the associated activity is removed.
    public static ImageView badStaticImageViewForTesting;
    
    
}

package com.codecoretechnologies.elvemobile;

import android.app.Activity;
import android.os.Bundle;

public class ElveMobileActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        // test communications
        try
		{
			//CommunicationTest.test();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
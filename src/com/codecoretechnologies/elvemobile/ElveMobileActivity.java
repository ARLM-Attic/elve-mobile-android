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
//        	Socket sock = new Socket("192.168.1.3", 33907);
//        	InputStreamReader in = new InputStreamReader(sock.getInputStream());
//        	int b = in.read(); //-1 if nothing
        	
        	boolean result = CommunicationTest.isNetworkAvailable(this);
			CommunicationTest.test(this);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

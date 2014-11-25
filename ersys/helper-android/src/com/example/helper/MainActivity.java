package com.example.helper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;

import com.ibm.mobile.services.cloudcode.IBMCloudCode;
import com.ibm.mobile.services.core.IBMBluemix;
import com.ibm.mobile.services.core.http.IBMHttpResponse;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String APP_ID = "applicationID";
	private static final String APP_SECRET = "applicationSecret";
	private static final String APP_ROUTE = "applicationRoute";
	private static final String PROPS_FILE = "bluelist.properties";

	
	 Button emergency;
	 Tracker gps;
	 String lat,lon;
	 String url="";
	 String responseString = "";
	 IBMCloudCode myCloudCodeService;
	 JSONObject jsonObj;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		// Read from properties file.
		Properties props = new java.util.Properties();
		Context context = getApplicationContext();
		try {
			AssetManager assetManager = context.getAssets();					
			props.load(assetManager.open(PROPS_FILE));
				System.out.println("property file found");			
		} catch (FileNotFoundException e) {
			System.out.println("property file not found");			
		} catch (IOException e) {
			System.out.println("property files could not be read");			
		}
		System.out.println( "Application ID is: " + props.getProperty(APP_ID));

		// Initialize the IBM core backend-as-a-service.
		IBMBluemix.initialize(this, props.getProperty(APP_ID), props.getProperty(APP_SECRET), props.getProperty(APP_ROUTE));
	   
		IBMCloudCode.initializeService();
		myCloudCodeService = IBMCloudCode.getService();
		
		emergency = (Button)findViewById(R.id.emergency);
        emergency.setOnClickListener(new View.OnClickListener() {
             
            @Override
            public void onClick(View arg0) {       
                // create class object
                gps = new Tracker(MainActivity.this);
 
                // check if GPS enabled    
                if(gps.canGetLocation()){
                     
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                     
                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();   
                
                    lat = String.valueOf(latitude);
                    lon = String.valueOf(longitude);
                    
                    
                    jsonObj = new JSONObject();
            		try {
            			jsonObj.put("lat", lat);
            			jsonObj.put("lon", lon);
            		} catch (JSONException e) {
            			e.printStackTrace();
            		}
                    
            		
            		
            		// Async task is used to send notification to server
                    ReadTask downloadTask = new ReadTask();
            		downloadTask.execute(url);
                    
                       

                
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
              
            }
        });
    }
	
	
	private class ReadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			
			
			myCloudCodeService.post("notifyOtherDevices", jsonObj).continueWith(new Continuation<IBMHttpResponse, String>() {

	            @Override
	            public String then(Task<IBMHttpResponse> task) throws Exception {
	                if (task.isCancelled()) {
	    				System.out.println( "Exception : Task" + task.isCancelled() + "was cancelled.");
	                } else if (task.isFaulted()) {
	                	System.out.println( "Exception : " + task.getError().getMessage());
	                } else {
	                    InputStream is = task.getResult().getInputStream();
	                    try {
	                        BufferedReader in = new BufferedReader(new InputStreamReader(is));
	                        String myString = "";
	                        while ((myString = in.readLine()) != null)
	                            responseString += myString;

	                        in.close();
	                        System.out.println( "Response Body: " + responseString);

	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    }

	                    System.out.println("Response Status from notifyOtherDevices: " + task.getResult().getHttpResponseCode());
	                }
	                return responseString;
	            }
	        });
			
		while (responseString == "")
		{
			
		}
			return responseString;          
			
			
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			if (result != null)
			{
                System.out.println("result" + result);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
			}
			else
			{
                System.out.println("result" + result);
	        Toast.makeText(getApplicationContext(), "Server is not reached", Toast.LENGTH_LONG).show();
			}
		}
		
		
		
	}
	
     
}
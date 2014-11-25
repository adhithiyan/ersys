package com.ersys.responder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import bolts.Continuation;
import bolts.Task;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ibm.mobile.services.core.IBMBluemix;
import com.ibm.mobile.services.push.IBMPush;
import com.ibm.mobile.services.push.IBMPushNotificationListener;
import com.ibm.mobile.services.push.IBMSimplePushNotification;

public class ListApplication extends Application {

	public static final int EDIT_ACTIVITY_RC = 1;
	public static IBMPush push = null;
	private Activity mActivity;
	private static final String deviceAlias = "TargetDevice";		
	private static final String consumerID = "MBaaSListApp";	
	private static final String CLASS_NAME = ListApplication.class.getSimpleName();
	private static final String APP_ID = "applicationID";
	private static final String APP_SECRET = "applicationSecret";
	private static final String APP_ROUTE = "applicationRoute";
	private static final String PROPS_FILE = "bluelist.properties";

	private IBMPushNotificationListener notificationListener = null;
	String emer_pos;
	MainActivity cactivity;
	public double lati;
	public double longi;
	LatLng ambulance_position;
    LatLng emergency_position;
	ListApplication blApplication;
	GoogleMap map;
	Tracker gps;
	String value;
	Double amb_lati,amb_longi;

	

	public ListApplication() {
		
		registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(Activity activity,Bundle savedInstanceState) {
				Log.d(CLASS_NAME, "Activity created: " + activity.getLocalClassName());
				mActivity = activity;
				
				// Define IBMPushNotificationListener behavior on push notifications.
				notificationListener = new IBMPushNotificationListener() {
					@Override
					public void onReceive(final IBMSimplePushNotification message) {
						
						
						
						
						mActivity.runOnUiThread(new Runnable(){
							@Override
							public void run() {
								Class<? extends Activity> actClass = mActivity.getClass();
								if (actClass == MainActivity.class) {
									
									Log.e(CLASS_NAME, "Notification message received: " + message.toString());
									// Present the message when sent from Push notification console.
									if(!message.getAlert().contains("ItemList was updated")){								
										mActivity.runOnUiThread(new Runnable() {
											public void run() {
												
											
												
												new AlertDialog.Builder(mActivity)
										        .setTitle("Push notification received")
										        .setMessage(message.getAlert())
										        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
										        	public void onClick(DialogInterface dialog, int whichButton) {
										        		System.out.println("ok is clicked");
										        		if(message.getUrl()!=null)
														{
														getEmerLatLng(message.getUrl());
														}
										            }
										         })
										        .show();
											}
										});
										
									}											
								}
							}
						});
					}					
				};
			}
			@Override
			public void onActivityStarted(Activity activity) {
				mActivity = activity;
				Log.d(CLASS_NAME, "Activity started: " + activity.getLocalClassName());
			}
			@Override
			public void onActivityResumed(Activity activity) {
				mActivity = activity;
				Log.d(CLASS_NAME, "Activity resumed: " + activity.getLocalClassName());
				if (push != null) {
					push.listen(notificationListener);
				}
			}
			@Override
			public void onActivitySaveInstanceState(Activity activity,Bundle outState) {
				Log.d(CLASS_NAME, "Activity saved instance state: " + activity.getLocalClassName());
			}
			@Override
			public void onActivityPaused(Activity activity) {
				if (push != null) {
					push.hold();
				}
				Log.d(CLASS_NAME, "Activity paused: " + activity.getLocalClassName());
				if (activity != null && activity.equals(mActivity))
		    		mActivity = null;
			}
			@Override
			public void onActivityStopped(Activity activity) {
				Log.d(CLASS_NAME, "Activity stopped: " + activity.getLocalClassName());
			}
			@Override
			public void onActivityDestroyed(Activity activity) {
				Log.d(CLASS_NAME, "Activity destroyed: " + activity.getLocalClassName());
			}
		});
	}



	@Override
	public void onCreate() {
		super.onCreate();
		emer_pos=null;

		// Read from properties file.
		Properties props = new java.util.Properties();
		Context context = getApplicationContext();
		try {
			AssetManager assetManager = context.getAssets();					
			props.load(assetManager.open(PROPS_FILE));
			Log.i(CLASS_NAME, "Found configuration file: " + PROPS_FILE);
		} catch (FileNotFoundException e) {
			Log.e(CLASS_NAME, "The bluelist.properties file was not found.", e);
		} catch (IOException e) {
			Log.e(CLASS_NAME, "The bluelist.properties file could not be read properly.", e);
		}
		Log.i(CLASS_NAME, "Application ID is: " + props.getProperty(APP_ID));

		// Initialize the IBM core backend-as-a-service.
		IBMBluemix.initialize(this, props.getProperty(APP_ID), props.getProperty(APP_SECRET), props.getProperty(APP_ROUTE));
	   
		// Initialize IBM Push service.
		IBMPush.initializeService();
		// Retrieve instance of the IBM Push service.
		push = IBMPush.getService();
		// Register the device with the IBM Push service.
		
		
		push.register(deviceAlias, consumerID).continueWith(new Continuation<String, Void>() {

	        @Override
	        public Void then(Task<String> task) throws Exception {
	            if (task.isCancelled()) {
	                Log.e(CLASS_NAME, "Exception : Task " + task.toString() + " was cancelled.");
	            } else if (task.isFaulted()) {
	                Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
	            } else {
	                Log.d(CLASS_NAME, "Device Successfully Registered");
	            }

	            return null;
	        }

	    });
		
		
		
			
	}
	
	
	public void  getEmerLatLng(String value) {
	
		emer_pos = value;
		System.out.println(emer_pos);
		String[] separated = emer_pos.split(",");
		String lat = separated[0];
		String lon =  separated[1];
		lati = Double.parseDouble(lat); 
		longi = Double.parseDouble(lon); 
		emergency_position = new LatLng(lati,longi);
		System.out.println(emergency_position);
		String url = getMapsApiDirectionsUrl();
		System.out.println(url);
		ReadTask downloadTask = new ReadTask();
		downloadTask.execute(url);

	}
	
	
	private String getMapsApiDirectionsUrl() {
		
		String waypoints = "waypoints=optimize:true|"
				+ ambulance_position.latitude + "," + ambulance_position.longitude
				+ "|" + "|" + emergency_position.latitude + ","
				+ emergency_position.longitude ;

		String sensor = "sensor=false";
		String params = waypoints + "&" + sensor;
		String output = "json";
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + params;
		
		return url;
	}
	
	private class ReadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			String data = "";
			System.out.println(url[0]);
			try {
				HttpConnection http = new HttpConnection();
				data = http.readUrl(url[0]);
				System.out.println(data);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			new ParserTask().execute(result);
		}
	}
	
	private class ParserTask extends
	AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

@Override
protected List<List<HashMap<String, String>>> doInBackground(
		String... jsonData) {

	JSONObject jObject;
	List<List<HashMap<String, String>>> routes = null;

	try {
		jObject = new JSONObject(jsonData[0]);
		PathJSONParser parser = new PathJSONParser();
		routes = parser.parse(jObject);
	} catch (Exception e) {
		e.printStackTrace();
	}
	return routes;
}

@Override
protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
	ArrayList<LatLng> points = null;
	PolylineOptions polyLineOptions = null;

	// traversing through routes
	for (int i = 0; i < routes.size(); i++) {
		points = new ArrayList<LatLng>();
		polyLineOptions = new PolylineOptions();
		List<HashMap<String, String>> path = routes.get(i);

		for (int j = 0; j < path.size(); j++) {
			HashMap<String, String> point = path.get(j);

			double lat = Double.parseDouble(point.get("lat"));
			double lng = Double.parseDouble(point.get("lng"));
			LatLng position = new LatLng(lat, lng);

			points.add(position);
		}

		polyLineOptions.addAll(points);
		polyLineOptions.width(2);
		polyLineOptions.color(Color.BLUE);
	}

	map.addPolyline(polyLineOptions);
}
}
	
	
	
	
	
	
	
}

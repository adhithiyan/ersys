package com.ersys.responder;



import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class MainActivity extends FragmentActivity {

	
	ListApplication blApplication;
	MainActivity activity;
	String url = "" ;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("1");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_path_google_map);
		blApplication = (ListApplication) getApplication();
		plotCurrentPosition();
		
		
		
	}
	
	public void plotCurrentPosition()
	{
		blApplication.gps = new Tracker(MainActivity.this);
	       if (blApplication.map == null) {
	    	   blApplication.map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();   	  
	              
	       }
	       
	       blApplication.amb_lati = blApplication.gps.getLatitude();
	       blApplication.amb_longi = blApplication.gps.getLongitude();
	      
	       blApplication.ambulance_position = new LatLng(blApplication.amb_lati, blApplication.amb_longi);
	       
	       if (blApplication.map!=null){
	           Marker current_pos = blApplication.map.addMarker(new MarkerOptions().position(blApplication.ambulance_position)
	               .title("Current Position")
	               );
	           
	           blApplication.map.moveCamera(CameraUpdateFactory.newLatLngZoom(blApplication.ambulance_position, 16));

	          
	         }
		
	}
	
	
	
	
}

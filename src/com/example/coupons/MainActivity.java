package com.example.coupons;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLocation(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void showLocation(Activity act, Location location){
    	double latitude = location.getLatitude();
    	double longitude = location.getLongitude();
    	String displayString = "Latitude: " + latitude + ", Longitude: " + longitude;
    	//https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=38.8942635,-77.301637&radius=500&sensor=false&key=AIzaSyCCtAWPC6sK8DuZaUfhEvYEQMbShr_NWzE
    	String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&sensor=true";
    	
    	 HttpClient httpclient = new DefaultHttpClient();
    	 	String responseString = "";
    	    HttpResponse response;
			try {
				response = httpclient.execute(new HttpGet(url));

	    	    StatusLine statusLine = response.getStatusLine();
	    	    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	    	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	    	        response.getEntity().writeTo(out);
	    	        out.close();
	    	        responseString = out.toString();
	    	        //..more logic
	    	    } else{
	    	        //Closes the connection.
	    	        response.getEntity().getContent().close();
	    	        throw new IOException(statusLine.getReasonPhrase());
	    	    }
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();	
			}

    	
    	new AlertDialog.Builder(act).setTitle("Location").setMessage(displayString).setNeutralButton("Close", null).show();  
    }
    
    public void getLocation(final Activity act){
    	// Acquire a reference to the system Location Manager
    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    	// Define a listener that responds to location updates
    	LocationListener locationListener = new LocationListener() {
    	    public void onLocationChanged(Location location) {
    	      // Called when a new location is found by the network location provider.
    	      makeUseOfNewLocation(location);
    	      getFourSquareLocations(location);
    	      /*
    	      try {
				searchVenues(locationToString(location));
				} catch (FoursquareApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
    	    }

    	    private void makeUseOfNewLocation(Location location) {
				// TODO Auto-generated method stub
    	    	showLocation(act, location);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

    	    public void onProviderEnabled(String provider) {}

    	    public void onProviderDisabled(String provider) {}
    	  };

    	// Register the listener with the Location Manager to receive location updates
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }
    
    public String locationToString(Location loc){
    	return loc.getLatitude() + "," + loc.getLongitude();
    }
    
    /*
    public void searchVenues(String ll) throws FoursquareApiException {
        // 
    	
    	First we need a initialize FoursquareApi. 
    	String ClientID = "XJH5PYPZC435R5YIDMLELKTTDCB2VZPQZA0TPL0IBKA1SCEQ";
    	String ClientSecret = "4H35GNJLBR2PFVQTFK5G5E22UHBKXVZXEUY415VKKAOSDWPA";
    	String CallbackURL = "http://mcr22.zymichost.com";
        FoursquareApi foursquareApi = new FoursquareApi(ClientID, ClientSecret, CallbackURL);
        
        // After client has been initialized we can make queries.
        Result<VenuesSearchResult> result = foursquareApi.venuesSearch(ll, null, null, null, null, null, null, null, null, null, null);
        
        if (result.getMeta().getCode() == 200) {
          // if query was ok we can finally we do something with the data
          for (CompactVenue venue : result.getResult().getVenues()) {
            // TODO: Do something we the data
            System.out.println(venue.getName());
          }
        } else {
          // TODO: Proper error handling
          System.out.println("Error occured: ");
          System.out.println("  code: " + result.getMeta().getCode());
          System.out.println("  type: " + result.getMeta().getErrorType());
          System.out.println("  detail: " + result.getMeta().getErrorDetail()); 
        }
        
      }
    */
    public static void getFourSquareLocations(Location location){
    	String fourSquareUrl = "https://api.foursquare.com/v2/venues/search?ll=" + location.getLatitude() + "," + location.getLongitude() + "&oauth_token=A4L23TVBZAT3423U1GN3WXRNAG3ZHT4QXTQQYCQYBOYWMWF0&v=20121127";
    	String response = getWebPage(fourSquareUrl);
    	System.out.println(response);
    	
    	JsonParser jsonParser = new JsonParser();
    	JsonElement json = jsonParser.parse(response);
    	if(json.isJsonObject()){
    		JsonObject jsonResponseObject = json.getAsJsonObject();
    		JsonElement jResponse = jsonResponseObject.get("response");
    		if(jResponse.isJsonObject()){
    			JsonObject jResponseObject = jResponse.getAsJsonObject();
    			JsonElement jVenues = jResponseObject.get("venues");
    			if(jVenues.isJsonArray()){  
    				JsonArray jVenuesArray = jVenues.getAsJsonArray();
	    			if(jVenues != null){
	    				for(JsonElement j : jVenuesArray){
	    					// Each j is an individual venue, ranked by proximity
	    					
	    				}
	    			}
    			}
    		}
    	}
    	
    }
    
   public static String getWebPage(String url) {
		URL myURL;
		StringBuilder response = new StringBuilder();
		String line;

		try {
			myURL = new URL(url);
			URLConnection myURLConnection = myURL.openConnection();
			myURLConnection.connect();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					myURLConnection.getInputStream()));

			while ((line = rd.readLine()) != null) {
				response.append(line + "\n");
			}
			rd.close();

			return response.toString();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response.toString();
	}
}

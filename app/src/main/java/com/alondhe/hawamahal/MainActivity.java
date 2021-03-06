package com.alondhe.hawamahal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Retrieve the location and temperature which can be used by the next activity.
 */
public class MainActivity extends AppCompatActivity implements LocationListener{

    private static double DEFAULT_LAT = 40.1092;
    private static double DEFAULT_LONG = -83.1403;
    public static String CURRENT_TEMP;
    public static String TEMP_MARGIN = "2";

    private Location mLocation;
    private Map<String,String> getTemperatureResult = new HashMap<String,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        ImageView imageView = (ImageView)findViewById(R.id.idAppImage);
        AnimationDrawable animationDrawable = new AnimationDrawable();
        animationDrawable.addFrame(getApplicationContext().getResources().getDrawable(R.drawable.resizedimage_0),700);
        animationDrawable.addFrame(getApplicationContext().getResources().getDrawable(R.drawable.resizedimage_1),700);
        animationDrawable.addFrame(getApplicationContext().getResources().getDrawable(R.drawable.resizedimage_2),700);
        animationDrawable.addFrame(getApplicationContext().getResources().getDrawable(R.drawable.resizedimage_3),700);
        animationDrawable.addFrame(getApplicationContext().getResources().getDrawable(R.drawable.resizedimage_4), 700);
        imageView.setBackgroundDrawable(animationDrawable);
        animationDrawable.start();


        try {
            // Acquire a reference to the system Location Manager
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1,1,this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
            mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(mLocation == null) {
                mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
//            String message = (mLocation == null) ? "NULL LOCATION":"Location - "+mLocation.toString();
//            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
//            toast.show();
        } catch(SecurityException e) {
            Log.d("SecurityException",e.getMessage());
            Toast toast = Toast.makeText(getApplicationContext(), "Application Cannot access LocationManager.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
        }
        if(mLocation != null) {
            Log.d("mLocation is not null", mLocation.toString());
            StringBuilder locationMsg = new StringBuilder();
            locationMsg.append("{Got Location \n mLocation.LAT:"+mLocation.getLatitude());
            locationMsg.append("\n,mLocation.LONG:"+mLocation.getLongitude());
            locationMsg.append("}");
            Toast toast = Toast.makeText(getApplicationContext(), locationMsg, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
        } else {

        }

        new GetTemperature().execute();

        final Button button = (Button)findViewById(R.id.idGetSongs);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Debugging toast message
                StringBuilder toastMessage = new StringBuilder();
                toastMessage.append("Retrieving Songs for {");
                for(Map.Entry<String,String> entry: getTemperatureResult.entrySet()) {
                    toastMessage.append(entry.getKey() + ":" + entry.getValue()+",");
                    toastMessage.append("\n");
                }
                if(mLocation != null) {
                    toastMessage.append("mLocation.LAT:"+mLocation.getLatitude());
                    toastMessage.append("\n,mLocation.LONG:"+mLocation.getLongitude());
                }
                toastMessage.append("\n mLocation:"+ ((mLocation == null)?"null":"notnull" + "\n"));
                toastMessage.append("}");
                Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                toast.show();

                Intent songListIntent = new Intent(MainActivity.this, SongListActivity.class);
                songListIntent.putExtra("com.alondhe.hawamahal.TEMP", getTemperatureResult.get("TEMP"));
                startActivity(songListIntent);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("1", "OnLocationChanged");
        Log.d("Location", location.toString());
        mLocation = location;
        new GetTemperature().execute();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("onStatusChanged",provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("onProviderEnabled",provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("onProviderDisabled", provider);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private class GetTemperature extends AsyncTask<Void,Void,Map<String,String>> {

        @Override
        protected Map<String,String> doInBackground(Void... params) {
            Map<String,String> retMap = new HashMap<String,String>();
            String temp = "0";
            String country = "";
            String city = "";
            String responseString = "";
            HttpURLConnection conn = null;
            InputStream is = null;
            BufferedReader reader = null;
            try {
                double latitude = DEFAULT_LAT;
                double longitude = DEFAULT_LONG;
                //URL url = new URL("http://api.openweathermap.org/data/2.5/weather?zip=43016,us&units=metric&appid=c376566d4fbb0ed7077f116c75dcb742");
                if(mLocation != null) {
                    latitude = mLocation.getLatitude();
                    longitude = mLocation.getLongitude();
                } else {
                    retMap.put("COUNTRY", "Could not obtain location.");
                    retMap.put("CITY","Co");
                    retMap.put("TEMP","Not Available");
                    return retMap;
                }

                String locationMessage = "Latitude:"+latitude+", Longitude:"+longitude;

                retMap.put("LATITUDE",String.valueOf(latitude));
                retMap.put("LONGITUDE",String.valueOf(longitude));

                String weatherLocation = "lat="+latitude+"&lon="+longitude;

                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?"+weatherLocation+"&units=metric&appid=c376566d4fbb0ed7077f116c75dcb742");
                Log.d("Weather URL",url.toString());
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                int response = conn.getResponseCode();
                //Log.d("Response code is :- ", "" + response);
                if(response == 200) {
                    is = conn.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(is));
                    while ((temp = reader.readLine()) != null) {
                        responseString = temp + responseString;
                        Log.d("Weather response", responseString);
                    }
                    // Parse the JSON
                    JSONObject weatherjson = new JSONObject(responseString);
                    JSONObject mainWeatherData = weatherjson.getJSONObject("main");
                    temp = mainWeatherData.getString("temp");

                    JSONObject sysWeatherData = weatherjson.getJSONObject("sys");
                    country = sysWeatherData.optString("country", "Not Available");
                    city = weatherjson.optString("name", "Not Available");
                } else {
                    // TO DO
                }
            } catch(Exception e) {
                e.printStackTrace();
                temp = "error";
            } finally {
                if(is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
            //Log.d("TEMP returned", temp);
            retMap.put("COUNTRY", country);
            retMap.put("CITY",city);
            retMap.put("TEMP",temp);
            return retMap;
        }

        @Override
        protected void onPostExecute(Map<String, String> resultMap) {

            TextView city = (TextView)findViewById(R.id.idCity);
            TextView temperature = (TextView)findViewById(R.id.idTemperature);

            CURRENT_TEMP = resultMap.get("TEMP");
            if(CURRENT_TEMP != null && "error".equals(CURRENT_TEMP)) {
                CURRENT_TEMP = "4";
                Toast toast = Toast.makeText(getApplicationContext(), "Using Default Temperature.", Toast.LENGTH_LONG);
                toast.show();
            }
            double tempInC = 0;
            double tempInF = 0;
            if(CURRENT_TEMP != null && !CURRENT_TEMP.equals("Not Available")) {
                tempInC = Double.parseDouble(CURRENT_TEMP);
                tempInF = Math.round((1.8 * tempInC + 32));
            }
            String text = resultMap.get("CITY") + ", " + resultMap.get("COUNTRY");
            city.setText(text);
            text  =  "" + tempInC + " °C / " + tempInF + " °F";
            temperature.setText(text);
            getTemperatureResult = resultMap;
        }
    }
}

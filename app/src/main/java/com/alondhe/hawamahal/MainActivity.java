package com.alondhe.hawamahal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

    private static Location mLocation;
    private Map<String,String> getTemperatureResult = new HashMap<String,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        try {
            // Acquire a reference to the system Location Manager
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1,1,this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
        } catch(SecurityException e) {
            e.printStackTrace();
        }

        new GetTemperature().execute();

        final Button button = (Button)findViewById(R.id.idGetSongs);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent songListIntent = new Intent(MainActivity.this, SongListActivity.class);
                songListIntent.putExtra("com.alondhe.hawamahal.TEMP", getTemperatureResult.get("TEMP"));
                startActivity(songListIntent);            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("1", "OnLocationChanged");
        Log.d("Location", location.toString());
        mLocation = location;
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
                }

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
//                        Log.d("Weather response", responseString);
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
            if(CURRENT_TEMP != null && "".equals(CURRENT_TEMP)) {
                CURRENT_TEMP = "4";
            }
            double tempInC = 0;
            double tempInF = 0;
            if(CURRENT_TEMP != null) {
                tempInC = Double.parseDouble(CURRENT_TEMP);
                tempInF = Math.round((tempInC - 30)/1.8);
            }
            String text = resultMap.get("CITY") + ", " + resultMap.get("COUNTRY");
            city.setText(text);
            text  =  "" + tempInC + " °C / " + tempInF + " °F";
            temperature.setText(text);
            getTemperatureResult = resultMap;
        }
    }
}

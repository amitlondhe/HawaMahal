package com.alondhe.hawamahal;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONObject;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.DLPayload;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.storage.object.SwiftAccount;
import org.openstack4j.model.storage.object.SwiftContainer;
import org.openstack4j.model.storage.object.SwiftObject;
import org.openstack4j.openstack.OSFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.net.URL;
import java.util.Set;

/**
 * A list fragment representing a list of Songs. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link SongDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class SongListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private Songs recoSongs = new Songs();

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(Song s);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Song s) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongListFragment() {
    }

    public Songs getRecoSongs(){
        return this.recoSongs;
    }

    public Songs getMockRecoSongs(){
        Songs s = new Songs();
        s.addSong(new Song("dummy1"));
        s.addSong(new Song("dummy2"));
        return s;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("1","On Create of SongListFragment");

        if(getMockRecoSongs().getRecommendedSongs().size() > 0) {
            Log.d("2","Reusing the available songs");
            setListAdapter(new ArrayAdapter<Song>(
                    getActivity(),
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1,
                    getMockRecoSongs().getRecommendedSongs()
            ));
        } else {
            Log.d("2", "Requesting recommendations");
            recoSongs.clearRecommendations();
            new DownloadWebpageTask().execute();
        }

    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... urls) {
            String temp = getCurrentTemp("43016");
            List<String> songsForThisWeather = getSongsForTemp(temp);
            return songsForThisWeather;
        }

        @Override
        protected void onPostExecute(List<String> songs) {
            super.onPostExecute(songs);
            Log.d("Recommendations",songs.toString());
            for(String song: songs) {
                recoSongs.addSong(new Song(song.trim()));
            }
            setListAdapter(new ArrayAdapter<Song>(
                    getActivity(),
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1,
                    recoSongs.getRecommendedSongs()
            ));
        }

        public String getCurrentTemp(String zipcode){
            String temp = "0";
            String responseString = "";
            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?zip=43016,us&units=metric&appid=c376566d4fbb0ed7077f116c75dcb742");
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                int response = conn.getResponseCode();
                //Log.d("Response code is :- ", "" + response);
                if(response == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    while ((temp = reader.readLine()) != null) {
                        responseString = temp + responseString;
//                        Log.d("Weather response", responseString);
                    }
                    // Parse the JSON
                    JSONObject weatherjson = new JSONObject(responseString);
                    JSONObject mainWeatherData = weatherjson.getJSONObject("main");
                    temp = mainWeatherData.getString("temp");
                } else {
                    // TO DO
                }
            } catch(Exception e) {
                e.printStackTrace();
                temp = "error";
            }
            Log.d("TEMP returned", temp);
            return temp;
        }

        public List<String> getSongsForTemp(String temperature) {
            if("error".equals(temperature)) {
                // TODO do not return null
                return null;
            }
            OSClient os = null;
            Set<String> songsForWeather = new HashSet<String>();
            try {
                Log.i("Authenticating","Authenticating Now");
                Identifier domainIdentifierById = Identifier.byId("7f5153b58a974c2d88a4a54f76808d68");
                Identifier projectIdentifierById = Identifier.byId("0f3a78b543784790ae3c4f1cd6fbd787");

                os = OSFactory.builderV3()
                        .endpoint("https://identity.open.softlayer.com/v3")
                        .scopeToProject(projectIdentifierById, domainIdentifierById)
                        .credentials("041fde0237f4493cae211f31169fd392", "e^0YD8F2*!hjbPZT")
                        .authenticate();

                Log.i("Authenticated","Authenticated successfully");
                SwiftAccount account = os.objectStorage().account().get();

                List<? extends SwiftContainer> containers = os.objectStorage().containers().list();
                int index = 0;
                for (SwiftContainer container : containers) {
                    if (container.getName().equals("output")) {
//                        Log.d("Found output at - ",""+ index);
                        break;
                    }
                    index = index + 1;
                }

                List<? extends SwiftObject> objs = os.objectStorage().objects().list(containers.get(index).getName());
                if(objs.isEmpty()) {
                    // TO DO return error response later
                }
                // Find out the temp. range which is used to recommend the songs.
                double currentTemp = Math.round(Double.parseDouble(temperature)) * 10;
                double upperLimit = currentTemp + 2;
                double lowerLimit = currentTemp - 2;
                Log.d("Temps",currentTemp + "," + upperLimit + "," + lowerLimit);

                for (SwiftObject obj : objs) {
                    DLPayload dlpayload = obj.download();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(dlpayload.getInputStream()));
                    String str = null;
                    do {
                        str = reader.readLine();
                        if(str != null) {
                            Log.d("Line", str);
                            str = str.replace("(", "");
                            str = str.replace(")", "");
                            str = str.replace("List","");
                            List<String> elements = Arrays.asList(str.split(","));
                            // Extract the temperature
                            double tempInC = Double.parseDouble(elements.get(0));
                            Log.d("TempInC",""+tempInC);
                            // Collect songs in this record
                            if((tempInC <= upperLimit) && (tempInC >= lowerLimit)) {
                                Log.d("Songs selected", elements.toString());
                                songsForWeather.addAll(elements.subList(1,elements.size()));
                            }
                        }
                    } while (str != null);
//                    Log.d("Final Recommendation", songsForWeather.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //???
            }
            return new ArrayList<>(songsForWeather);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        //mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
        mCallbacks.onItemSelected((Song)getListView().getItemAtPosition(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}

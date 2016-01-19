package com.alondhe.hawamahal;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alondhe.hawamahal.dummy.DummyContent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * A fragment representing a single Song detail screen.
 * This fragment is either contained in a {@link SongListActivity}
 * in two-pane mode (on tablets) or a {@link SongDetailActivity}
 * on handsets.
 */
public class SongDetailFragment extends Fragment {

    private Song song;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        song = (Song) this.getArguments().getSerializable("com.alondhe.hawamahal.Song");
        if(song == null) {
            song = new Song("N/A");
        }
        Log.d("Retrieved Song", song.toString());
        if (song != null) {
            Activity activity = this.getActivity();
/*            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                //appBarLayout.setTitle(mItem.content);
                appBarLayout.setTitle(song.getSongName());
            }*/
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_song_detail, container, false);

        if (song != null) {
            Log.d("Parsed", Uri.parse("https://i.scdn.co/image/9f875be726d4d249caa9ed5ec7d772fd5e78d8ca").toString());
            ((TextView) rootView.findViewById(R.id.song_name)).setText(song.getSongName());
//            ((TextView) rootView.findViewById(R.id.album_name)).setText(song.getAlbumnName());
            ImageView imageView = ((ImageView) rootView.findViewById(R.id.imageView));
            new DownloadImageTask().execute(imageView);
        }

        return rootView;
    }

    private class DownloadImageTask extends AsyncTask<View, Void, BitmapDrawable> {

        ImageView imageView = null;

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if(imageView != null) {
                Log.d("Imageview is not null",imageView.toString());
                AnimationDrawable songAnimation = (AnimationDrawable) imageView.getBackground();
                songAnimation.stop();
            } else {
                Log.d("ImageView is null","***");
            }
        }

        @Override
        protected BitmapDrawable doInBackground(View... params) {
            for(View view:params) {
                imageView = (ImageView) view;
            }
            Bitmap bitmap = null;
            BitmapDrawable bitmapDrawable = null;
            try {
                // Get the image URL
                String imageURL = getImageURL(song.getAlbumURL());
//                Log.d("imageURL",imageURL);
                //InputStream is = new URL("https://i.scdn.co/image/9f875be726d4d249caa9ed5ec7d772fd5e78d8ca").openStream();
                InputStream is = new URL(imageURL).openStream();
                bitmap = BitmapFactory.decodeStream(is);
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        (int) (bitmap.getWidth() ), (int) (bitmap.getHeight() ), false);
                bitmapDrawable = new BitmapDrawable(getResources(), bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return bitmapDrawable;
            }
        }

        @Override
        protected void onPostExecute(BitmapDrawable bitmapDrawable) {
            super.onPostExecute(bitmapDrawable);
            // Do some random work
            imageView.setMinimumHeight(LinearLayout.LayoutParams.MATCH_PARENT);
            imageView.setMaxHeight(LinearLayout.LayoutParams.MATCH_PARENT);

            imageView.setImageDrawable(bitmapDrawable);
//            Log.d("image", "done setting the image");
        }

        private String getImageURL(String albumURL) {
            String responseString = "";
            String temp = "";
            try {
//                Log.d("Album URL",albumURL);
                URL url = new URL(albumURL);
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
                        responseString =   responseString +  temp;
                    }
//                    Log.d("albumurlresponse",responseString);
                    // Parse the JSON
                    JSONObject albumDetails = new JSONObject(responseString);
//                    Log.d("albumDetails",albumDetails.toString());
                    JSONArray images = albumDetails.optJSONArray("images");
//                    Log.d("images",images.toString());
                    if(images.length() > 0) {
                        JSONObject image = images.getJSONObject(0);
                        temp = image.getString("url");
                    }
                } else {
                    // TO DO
                }
            } catch(Exception e) {
                e.printStackTrace();
                temp = "error";
            } finally {
                Log.d("Returning imageurl",temp);
                return temp;
            }

        }
    }
}




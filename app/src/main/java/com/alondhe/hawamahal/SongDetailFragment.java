package com.alondhe.hawamahal;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;

import com.alondhe.hawamahal.dummy.DummyContent;

import java.io.InputStream;
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
        Log.d("Retrieved Song", song.toString());
        if (song != null) {
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                //appBarLayout.setTitle(mItem.content);
                appBarLayout.setTitle(song.getSongName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_song_detail, container, false);

        if (song != null) {
            Log.d("Parsed", Uri.parse("https://i.scdn.co/image/9f875be726d4d249caa9ed5ec7d772fd5e78d8ca").toString());
            ((TextView) rootView.findViewById(R.id.song_detail)).setText(song.getAlbumnName() + ":" + song.getSongName());
            ImageView imageView = ((ImageView) rootView.findViewById(R.id.imageView));
            new DownloadImageTask().execute(imageView);
        }

        return rootView;
    }

    private class DownloadImageTask extends AsyncTask<View, Void, BitmapDrawable> {

        ImageView imageView = null;

        @Override
        protected BitmapDrawable doInBackground(View... params) {
            for(View view:params) {
                imageView = (ImageView) view;
            }
            Bitmap bitmap = null;
            BitmapDrawable bitmapDrawable = null;
            try {
                InputStream is = new URL("https://i.scdn.co/image/9f875be726d4d249caa9ed5ec7d772fd5e78d8ca").openStream();
                bitmap = BitmapFactory.decodeStream(is);
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        (int) (bitmap.getWidth() * 0.5), (int) (bitmap.getHeight() * 0.5), false);
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
            imageView.setImageDrawable(bitmapDrawable);
            Log.d("image", "done setting the image");
        }
    }
}




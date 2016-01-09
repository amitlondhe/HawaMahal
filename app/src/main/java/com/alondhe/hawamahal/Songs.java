package com.alondhe.hawamahal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by alondhe on 1/6/2016.
 */
public class Songs {
    private Map<String,Song> recommendations = new HashMap<String,Song>();

    public void addSong(Song s) {
        recommendations.put(s.getSongName(), s);
    }

    public List<Song> getRecommendedSongs(){
       return new ArrayList<Song>(recommendations.values());
    }

    public Song getSong(String songName){
        return recommendations.get(songName);
    }

    public void clearRecommendations(){
        recommendations.clear();
    }
}

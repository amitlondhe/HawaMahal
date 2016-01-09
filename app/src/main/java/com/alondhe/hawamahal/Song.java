package com.alondhe.hawamahal;

/**
 * Created by alondhe on 1/5/2016.
 */
public class Song implements java.io.Serializable {
    private String spotifyid;
    private String songName;
    private String songURL;
    private String albumnName;
    private String albumURL;

    private static final String NA = "N/A";

    Song(String songName) {
        this(songName,NA,NA,NA,NA);
    }

    Song(String songName,String spotifyid,String songURL,String albumnName,String albumURL) {
        this.songName = songName;
        this.spotifyid = spotifyid;
        this.songURL = songURL;
        this.albumnName = albumnName;
        this.albumURL = albumURL;
    }

    public String getSpotifyid() {
        return spotifyid;
    }

    public void setSpotifyid(String spotifyid) {
        this.spotifyid = spotifyid;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongURL() {
        return songURL;
    }

    public void setSongURL(String songURL) {
        this.songURL = songURL;
    }

    public String getAlbumnName() {
        return albumnName;
    }

    public void setAlbumnName(String albumnName) {
        this.albumnName = albumnName;
    }

    public String getAlbumURL() {
        return albumURL;
    }

    public void setAlbumURL(String albumURL) {
        this.albumURL = albumURL;
    }

    @Override
    public String toString() {
        return songName;
//        return "Song{" +
//                "spotifyid='" + spotifyid + '\'' +
//                ", songName='" + songName + '\'' +
//                ", songURL='" + songURL + '\'' +
//                ", albumnName='" + albumnName + '\'' +
//                ", albumURL='" + albumURL + '\'' +
//                '}';
    }
}

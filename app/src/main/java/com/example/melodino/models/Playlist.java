package com.example.melodino.models;

public class Playlist {
    private long id;
    private String title;
    private int nb_tracks;
    private String picture_medium;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getNbTracks() {
        return nb_tracks;
    }

    public String getPictureMedium() {
        return picture_medium;
    }
}

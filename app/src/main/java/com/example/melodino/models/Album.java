package com.example.melodino.models;

import com.google.gson.annotations.SerializedName;

public class Album {
    @SerializedName("cover_medium")
    private String coverMedium;

    public String getCoverMedium() {
        return coverMedium;
    }
}

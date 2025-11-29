package com.example.melodino.utils;

import com.example.melodino.models.DeezerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface DeezerApiService {
    @GET
    Call<DeezerResponse> getPlaylistTracks(@Url String url);
}

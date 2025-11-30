package com.example.melodino.api;

import com.example.melodino.models.Artist;
import com.example.melodino.models.DeezerResponse;
import com.example.melodino.models.Genre;
import com.example.melodino.models.Playlist;
import com.example.melodino.models.Track;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface DeezerApiService {
    @GET("search/artist")
    Call<DeezerResponse<Artist>> searchArtists(@Query("q") String query);

    @GET("radio/genres")
    Call<DeezerResponse<Genre>> getGenres();

    @GET("search/playlist")
    Call<DeezerResponse<Playlist>> searchPlaylists(@Query("q") String query);

    @GET("chart/0/playlists")
    Call<DeezerResponse<Playlist>> getChartPlaylists();

    @GET("chart/0/artists")
    Call<DeezerResponse<Artist>> getChartArtists();

    @GET
    Call<DeezerResponse<Track>> getPlaylistTracks(@Url String url);
}

package com.example.melodino;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.widget.Toast;
import com.example.melodino.models.DeezerResponse;
import com.example.melodino.models.Track;
import com.example.melodino.api.DeezerApiService;
import com.example.melodino.utils.RetrofitClient;
import java.util.List;
import java.util.Random;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        ImageView outerRing = findViewById(R.id.loading_ring_outer);
        ImageView innerRing = findViewById(R.id.loading_ring_inner);

        Animation spinSlow = AnimationUtils.loadAnimation(this, R.anim.spin_slow);
        Animation spinFast = AnimationUtils.loadAnimation(this, R.anim.spin);

        outerRing.startAnimation(spinSlow);
        innerRing.startAnimation(spinFast);

        String apiUrl = getIntent().getStringExtra("api_url");
        if (apiUrl != null) {
            fetchSongs(apiUrl);
        } else {
            // Fallback or error handling
            Toast.makeText(this, "No API URL provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchSongs(String url) {
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());

        executor.execute(() -> {
            DeezerApiService service = RetrofitClient.getClient().create(DeezerApiService.class);
            Call<DeezerResponse<Track>> call = service.getPlaylistTracks(url);

            try {
                Response<DeezerResponse<Track>> response = call.execute();
                handler.post(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Track> tracks = response.body().getData();
                        if (tracks != null && !tracks.isEmpty()) {
                            // Filter tracks with preview URL
                            List<Track> validTracks = new java.util.ArrayList<>();
                            for (Track track : tracks) {
                                if (track.getPreview() != null && !track.getPreview().isEmpty()) {
                                    validTracks.add(track);
                                }
                            }

                            if (validTracks.size() < 5) {
                                Toast.makeText(LoadingActivity.this, "Not enough playable songs in this playlist",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }

                            String gameMode = getIntent().getStringExtra("GAME_MODE");
                            if (gameMode == null)
                                gameMode = "SURVIVAL"; // Default

                            if (gameMode.equals("CLASSIC")) {
                                // Classic Mode: Pick 1 random song
                                java.util.Collections.shuffle(validTracks);
                                Track selectedTrack = validTracks.get(0);

                                // Collect all songs for autocomplete
                                java.util.ArrayList<String> allSongs = new java.util.ArrayList<>();
                                for (Track track : tracks) {
                                    allSongs.add(track.getTitle() + " - " + track.getArtist().getName());
                                }

                                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                                intent.putExtra("song_url", selectedTrack.getPreview());
                                intent.putExtra("correct_answer",
                                        selectedTrack.getTitle() + " - " + selectedTrack.getArtist().getName());
                                intent.putExtra("cover_url", selectedTrack.getAlbum().getCoverMedium());
                                intent.putStringArrayListExtra("song_list", allSongs);
                                intent.putExtra("api_url", url);
                                startActivity(intent);
                                finish();

                            } else {
                                // Survival Mode: Pick 5 songs
                                java.util.Collections.shuffle(validTracks);
                                List<Track> selectedTracks = validTracks.subList(0, 5);

                                java.util.ArrayList<String> songUrls = new java.util.ArrayList<>();
                                java.util.ArrayList<String> correctAnswers = new java.util.ArrayList<>();
                                java.util.ArrayList<String> coverUrls = new java.util.ArrayList<>();
                                java.util.ArrayList<String> allSongs = new java.util.ArrayList<>();

                                for (Track track : selectedTracks) {
                                    songUrls.add(track.getPreview());
                                    correctAnswers.add(track.getTitle() + " - " + track.getArtist().getName());
                                    coverUrls.add(track.getAlbum().getCoverMedium());
                                }

                                for (Track track : tracks) {
                                    allSongs.add(track.getTitle() + " - " + track.getArtist().getName());
                                }

                                Intent intent = new Intent(LoadingActivity.this, GameActivity.class);
                                intent.putStringArrayListExtra("song_urls", songUrls);
                                intent.putStringArrayListExtra("correct_answers", correctAnswers);
                                intent.putStringArrayListExtra("cover_urls", coverUrls);
                                intent.putStringArrayListExtra("all_songs", allSongs);
                                intent.putExtra("api_url", url);
                                intent.putExtra("challenge_type", getIntent().getStringExtra("challenge_type"));
                                intent.putExtra("challenge_subtitle", getIntent().getStringExtra("challenge_subtitle"));

                                startActivity(intent);
                                finish();
                            }

                        } else {
                            Toast.makeText(LoadingActivity.this, "No tracks found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(LoadingActivity.this, "API Error", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } catch (java.io.IOException e) {
                handler.post(() -> {
                    Toast.makeText(LoadingActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                handler.post(() -> {
                    Toast.makeText(LoadingActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
}

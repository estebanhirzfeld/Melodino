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
import com.example.melodino.utils.DeezerApiService;
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
            Call<DeezerResponse> call = service.getPlaylistTracks(url);

            try {
                Response<DeezerResponse> response = call.execute();
                handler.post(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Track> tracks = response.body().getData();
                        if (tracks != null && !tracks.isEmpty()) {
                            // Pick random song
                            Random random = new Random();
                            Track randomTrack = tracks.get(random.nextInt(tracks.size()));

                            // Collect all songs for autocomplete
                            java.util.ArrayList<String> allSongs = new java.util.ArrayList<>();
                            for (Track track : tracks) {
                                allSongs.add(track.getTitle() + " - " + track.getArtist().getName());
                            }

                            // Navigate to MainActivity
                            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                            intent.putExtra("song_url", randomTrack.getPreview());
                            intent.putExtra("correct_answer",
                                    randomTrack.getTitle() + " - " + randomTrack.getArtist().getName());
                            intent.putExtra("cover_url", randomTrack.getAlbum().getCoverMedium());
                            intent.putExtra("api_url", url); // Pass API URL for "Play Again"
                            intent.putStringArrayListExtra("song_list", allSongs); // Pass full list
                            startActivity(intent);
                            finish();
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

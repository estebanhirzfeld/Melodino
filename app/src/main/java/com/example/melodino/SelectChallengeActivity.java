package com.example.melodino;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.melodino.api.DeezerApiService;
import com.example.melodino.models.Artist;
import com.example.melodino.models.DeezerResponse;
import com.example.melodino.models.Genre;
import com.example.melodino.models.Playlist;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectChallengeActivity extends AppCompatActivity {

    private ImageButton backButton;
    private EditText searchInput;
    private ScrollView challengesScrollView;
    private ScrollView searchResultsContainer;
    private LinearLayout searchResultsContent;

    // Challenges
    private CardView challengeTop50;
    private CardView challengeArtist;
    private CardView challengePlaylist; // Renamed from challengeGenre
    private CardView challengeRock;
    private CardView challengePop;
    private CardView challengeEdm;
    private CardView challengeHiphop;
    private CardView challenge80s;

    private DeezerApiService apiService;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private List<Genre> allGenres = new ArrayList<>();

    private enum SearchMode {
        ALL, ARTIST, PLAYLIST // Renamed GENRE to PLAYLIST
    }

    private SearchMode currentSearchMode = SearchMode.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_challenge);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        searchInput = findViewById(R.id.search_input);
        challengesScrollView = findViewById(R.id.challenges_scroll_view);
        searchResultsContainer = findViewById(R.id.search_results_container);
        searchResultsContent = findViewById(R.id.search_results_content);

        challengeTop50 = findViewById(R.id.challenge_top50);
        challengeArtist = findViewById(R.id.challenge_artist);
        challengePlaylist = findViewById(R.id.challenge_playlist); // Updated ID
        challengeRock = findViewById(R.id.challenge_rock);
        challengePop = findViewById(R.id.challenge_pop);
        challengeEdm = findViewById(R.id.challenge_edm);
        challengeHiphop = findViewById(R.id.challenge_hiphop);
        challenge80s = findViewById(R.id.challenge_80s);

        setupRetrofit();
        fetchAllGenres();

        // Back button
        backButton.setOnClickListener(v -> {
            if (currentSearchMode != SearchMode.ALL) {
                resetSearchMode();
            } else {
                Intent intent = new Intent(SelectChallengeActivity.this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    if (currentSearchMode == SearchMode.ALL) {
                        showChallenges();
                    } else {
                        // In specific mode, empty query shows empty results but stays in search view
                        searchResultsContent.removeAllViews();
                    }
                } else {
                    searchRunnable = () -> performSearch(query);
                    searchHandler.postDelayed(searchRunnable, 500); // 500ms debounce
                }
            }
        });

        setupChallengeClickListeners();
    }

    private void setupRetrofit() {
        apiService = com.example.melodino.utils.RetrofitClient.getClient().create(DeezerApiService.class);
    }

    private void fetchAllGenres() {
        apiService.getGenres().enqueue(new Callback<DeezerResponse<Genre>>() {
            @Override
            public void onResponse(Call<DeezerResponse<Genre>> call, Response<DeezerResponse<Genre>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allGenres = response.body().getData();
                }
            }

            @Override
            public void onFailure(Call<DeezerResponse<Genre>> call, Throwable t) {
                // Ignore failure for now
            }
        });
    }

    private java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();

    private void performSearch(String query) {
        showSearchResults();
        searchResultsContent.removeAllViews();

        executor.execute(() -> {
            try {
                List<Artist> artists = null;
                List<Genre> filteredGenres = new ArrayList<>();
                List<Playlist> playlists = null;

                // Determine limit based on mode
                int limit = (currentSearchMode == SearchMode.ALL) ? 3 : 10;

                // 1. Search Artists (if mode is ALL or ARTIST)
                if (currentSearchMode == SearchMode.ALL || currentSearchMode == SearchMode.ARTIST) {
                    Response<DeezerResponse<Artist>> artistResponse = apiService.searchArtists(query).execute();
                    if (artistResponse.isSuccessful() && artistResponse.body() != null) {
                        artists = artistResponse.body().getData();
                    }
                }

                // 2. Filter Genres (only if mode is ALL) - Removed GENRE specific search as
                // requested
                if (currentSearchMode == SearchMode.ALL) {
                    for (Genre genre : allGenres) {
                        if (genre.getName() != null && genre.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredGenres.add(genre);
                        }
                    }
                }

                // 3. Search Playlists (if mode is ALL or PLAYLIST)
                if (currentSearchMode == SearchMode.ALL || currentSearchMode == SearchMode.PLAYLIST) {
                    Response<DeezerResponse<Playlist>> playlistResponse = apiService.searchPlaylists(query).execute();
                    if (playlistResponse.isSuccessful() && playlistResponse.body() != null) {
                        playlists = playlistResponse.body().getData();
                    }
                }

                // 4. Update UI on Main Thread
                List<Artist> finalArtists = artists;
                List<Playlist> finalPlaylists = playlists;
                int finalLimit = limit;

                searchHandler.post(() -> {
                    if (isDestroyed() || isFinishing())
                        return;

                    // Add Artists
                    if (finalArtists != null && !finalArtists.isEmpty()) {
                        addHeader("Artists");
                        for (int i = 0; i < Math.min(finalArtists.size(), finalLimit); i++) {
                            addArtistItem(finalArtists.get(i));
                        }
                    }

                    // Add Genres (Only in ALL mode)
                    if (!filteredGenres.isEmpty()) {
                        addHeader("Genres");
                        for (int i = 0; i < Math.min(filteredGenres.size(), 3); i++) { // Keep genre limit small in ALL
                                                                                       // mode
                            addGenreItem(filteredGenres.get(i));
                        }
                    }

                    // Add Playlists
                    if (finalPlaylists != null && !finalPlaylists.isEmpty()) {
                        addHeader("Playlists");
                        for (int i = 0; i < Math.min(finalPlaylists.size(), finalLimit); i++) {
                            addPlaylistItem(finalPlaylists.get(i));
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showChallenges() {
        challengesScrollView.setVisibility(View.VISIBLE);
        searchResultsContainer.setVisibility(View.GONE);
    }

    private void showSearchResults() {
        challengesScrollView.setVisibility(View.GONE);
        searchResultsContainer.setVisibility(View.VISIBLE);
    }

    private void enableSearchMode(SearchMode mode) {
        currentSearchMode = mode;
        showSearchResults();
        searchResultsContent.removeAllViews(); // Clear previous results

        if (mode == SearchMode.ARTIST) {
            searchInput.setHint("Search for an artist...");
        } else if (mode == SearchMode.PLAYLIST) {
            searchInput.setHint("Search for a playlist...");
        } else {
            searchInput.setHint("Artist, Genre, Playlists...");
        }

        searchInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void resetSearchMode() {
        currentSearchMode = SearchMode.ALL;
        searchInput.setText("");
        searchInput.setHint("Artist, Genre, Playlists...");
        searchInput.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        showChallenges();
    }

    private void addHeader(String title) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_search_header, searchResultsContent, false);
        TextView textView = view.findViewById(R.id.header_title);
        textView.setText(title);
        searchResultsContent.addView(view);
    }

    private void addArtistItem(Artist artist) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_search_artist, searchResultsContent, false);
        TextView name = view.findViewById(R.id.artist_name);
        ImageView image = view.findViewById(R.id.artist_image);

        name.setText(artist.getName());
        Glide.with(this).load(artist.getPictureMedium()).into(image);

        view.setOnClickListener(v -> startChallenge(artist.getName(),
                "https://api.deezer.com/artist/" + artist.getId() + "/top?limit=50"));
        searchResultsContent.addView(view);
    }

    private void addGenreItem(Genre genre) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_search_genre, searchResultsContent, false);
        TextView name = view.findViewById(R.id.genre_name);

        name.setText(genre.getName());
        if (genre.getPictureMedium() != null && !genre.getPictureMedium().isEmpty()) {
            ImageView icon = view.findViewById(R.id.genre_icon);
            Glide.with(this).load(genre.getPictureMedium()).into(icon);
        }

        // Genre items are not clickable to start a challenge directly from search
        // results in this mode
        searchResultsContent.addView(view);
    }

    private void addPlaylistItem(Playlist playlist) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_search_playlist, searchResultsContent, false);
        TextView title = view.findViewById(R.id.playlist_title);
        TextView details = view.findViewById(R.id.playlist_details);
        ImageView image = view.findViewById(R.id.playlist_image);

        title.setText(playlist.getTitle());
        details.setText("Playlist • " + playlist.getNbTracks() + " songs");
        Glide.with(this)
                .load(playlist.getPictureMedium())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(16))) // 8dp approx
                .into(image);

        view.setOnClickListener(v -> startChallenge(playlist.getTitle(),
                "https://api.deezer.com/playlist/" + playlist.getId() + "/tracks"));
        searchResultsContent.addView(view);
    }

    private void setupChallengeClickListeners() {
        challengeTop50.setOnClickListener(v -> startChallenge("Top 50", TOP_50_URL));

        // Modified listeners for Artist and Playlist
        challengeArtist.setOnClickListener(v -> enableSearchMode(SearchMode.ARTIST));
        challengePlaylist.setOnClickListener(v -> enableSearchMode(SearchMode.PLAYLIST)); // Updated listener

        challengeRock
                .setOnClickListener(v -> startChallenge("Rock", "https://api.deezer.com/playlist/938813531/tracks"));
        challengePop
                .setOnClickListener(v -> startChallenge("Pop", "https://api.deezer.com/playlist/1290316405/tracks"));
        challengeEdm
                .setOnClickListener(v -> startChallenge("EDM", "https://api.deezer.com/playlist/1996494362/tracks"));
        challengeHiphop.setOnClickListener(
                v -> startChallenge("Hip-Hop", "https://api.deezer.com/playlist/1386279365/tracks"));
        challenge80s
                .setOnClickListener(v -> startChallenge("'80s", "https://api.deezer.com/playlist/1431604065/tracks"));
    }

    private static final String TOP_50_URL = "https://api.deezer.com/playlist/1111142221/tracks";

    private void startChallenge(String challengeName, String apiUrl) {
        Intent intent = new Intent(SelectChallengeActivity.this, LoadingActivity.class);
        intent.putExtra("challenge_type", challengeName);
        if (apiUrl != null) {
            intent.putExtra("api_url", apiUrl);
        }
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (currentSearchMode != SearchMode.ALL) {
            resetSearchMode();
        } else {
            Intent intent = new Intent(SelectChallengeActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}
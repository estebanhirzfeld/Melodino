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
import com.example.melodino.utils.RecentlyPlayedManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectChallengeActivity extends AppCompatActivity {

    private ImageButton backButton;
    private EditText searchInput;
    private View challengesContent;
    private LinearLayout searchResultsContainer;
    private LinearLayout searchResultsContent;

    // New Containers
    private LinearLayout recentlyPlayedList;
    private LinearLayout popularPlaylistsContainer;
    private LinearLayout trendingArtistsContainer;
    private LinearLayout genreSectionsContainer;

    private DeezerApiService apiService;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private List<Genre> allGenres = new ArrayList<>();
    private RecentlyPlayedManager recentlyPlayedManager;

    private enum SearchMode {
        ALL, ARTIST, PLAYLIST
    }

    private SearchMode currentSearchMode = SearchMode.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_challenge);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        searchInput = findViewById(R.id.search_input);
        challengesContent = findViewById(R.id.challenges_content);
        searchResultsContainer = findViewById(R.id.search_results_container);
        searchResultsContent = findViewById(R.id.search_results_content);

        recentlyPlayedList = findViewById(R.id.recently_played_list);
        popularPlaylistsContainer = findViewById(R.id.popular_playlists_container);
        trendingArtistsContainer = findViewById(R.id.trending_artists_container);
        genreSectionsContainer = findViewById(R.id.genre_sections_container);

        recentlyPlayedManager = new RecentlyPlayedManager(this);

        setupRetrofit();
        fetchAllGenres();

        // Populate UI Sections
        populateRecentlyPlayed();
        populatePopularPlaylists();
        populateTrendingArtists();
        populateGenreSections();

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
                        searchResultsContent.removeAllViews();
                    }
                } else {
                    searchRunnable = () -> performSearch(query);
                    searchHandler.postDelayed(searchRunnable, 500); // 500ms debounce
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh recently played when returning to this screen
        populateRecentlyPlayed();
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

    private void populateRecentlyPlayed() {
        recentlyPlayedList.removeAllViews();
        List<RecentlyPlayedManager.RecentlyPlayedItem> items = recentlyPlayedManager.getRecentlyPlayed();

        if (items.isEmpty()) {
            // Hide section if empty, or show a placeholder?
            // For now, let's hide the section header if we could, but since it's in XML,
            // we might just leave it empty or show a "No recent plays" text.
            // Ideally we'd toggle visibility of the TextView header too, but I don't have a
            // reference to it.
            // Let's just leave it empty for now.
        } else {
            for (RecentlyPlayedManager.RecentlyPlayedItem item : items) {
                addRecentlyPlayedItem(item.getTitle(), item.getSubtitle(), item.getApiUrl(), item.getImageUrl());
            }
        }
    }

    private void addRecentlyPlayedItem(String title, String subtitle, String apiUrl, String imageUrl) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_recently_played, recentlyPlayedList, false);
        TextView titleView = view.findViewById(R.id.item_title);
        TextView subtitleView = view.findViewById(R.id.item_subtitle);
        ImageView imageView = view.findViewById(R.id.item_image);

        titleView.setText(title);
        subtitleView.setText(subtitle);
        Glide.with(this).load(imageUrl).into(imageView);

        view.setOnClickListener(v -> startChallenge(title, apiUrl, subtitle, imageUrl));
        recentlyPlayedList.addView(view);
    }

    private void populatePopularPlaylists() {
        apiService.getChartPlaylists().enqueue(new Callback<DeezerResponse<Playlist>>() {
            @Override
            public void onResponse(Call<DeezerResponse<Playlist>> call, Response<DeezerResponse<Playlist>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    popularPlaylistsContainer.removeAllViews();
                    for (Playlist playlist : response.body().getData()) {
                        addPopularPlaylistItem(playlist.getTitle(), playlist.getPictureMedium(),
                                "https://api.deezer.com/playlist/" + playlist.getId() + "/tracks",
                                popularPlaylistsContainer);
                    }
                }
            }

            @Override
            public void onFailure(Call<DeezerResponse<Playlist>> call, Throwable t) {
                // Fallback or error handling
            }
        });
    }

    private void addPopularPlaylistItem(String title, String imageUrl, String apiUrl, LinearLayout container) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_popular_playlist_card, container,
                false);
        TextView titleView = view.findViewById(R.id.card_title);
        ImageView imageView = view.findViewById(R.id.card_image);

        titleView.setText(title);
        Glide.with(this).load(imageUrl).into(imageView);

        view.setOnClickListener(v -> startChallenge(title, apiUrl, "Playlist", imageUrl));
        container.addView(view);
    }

    private void populateTrendingArtists() {
        apiService.getChartArtists().enqueue(new Callback<DeezerResponse<Artist>>() {
            @Override
            public void onResponse(Call<DeezerResponse<Artist>> call, Response<DeezerResponse<Artist>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    trendingArtistsContainer.removeAllViews();
                    for (Artist artist : response.body().getData()) {
                        addTrendingArtistItem(artist.getName(), artist.getPictureMedium(),
                                "https://api.deezer.com/artist/" + artist.getId() + "/top?limit=50");
                    }
                }
            }

            @Override
            public void onFailure(Call<DeezerResponse<Artist>> call, Throwable t) {
                // Fallback
            }
        });
    }

    private void addTrendingArtistItem(String name, String imageUrl, String apiUrl) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_trending_artist_circle, trendingArtistsContainer,
                false);
        TextView nameView = view.findViewById(R.id.artist_name);
        ImageView imageView = view.findViewById(R.id.artist_image);

        nameView.setText(name);
        Glide.with(this).load(imageUrl).into(imageView);

        view.setOnClickListener(v -> startChallenge(name, apiUrl, "Artist", imageUrl));
        trendingArtistsContainer.addView(view);
    }

    private void populateGenreSections() {
        String[] genres = {
                "Current Hits", // The Billboard Hot 100 style
                "2000s", // The sweet spot for 25-35 year olds
                "90s", // Golden era nostalgia
                "80s", // Synth-pop and classic rock
                "Movie Themes", // Very distinct audio clips, fun to guess
                "Latin Party", // Specifically high-energy songs
        };
        for (String genre : genres) {
            addGenreSection(genre);
        }
    }

    private void addGenreSection(String genreName) {
        View sectionView = LayoutInflater.from(this).inflate(R.layout.item_genre_section, genreSectionsContainer,
                false);
        TextView titleView = sectionView.findViewById(R.id.section_title);
        LinearLayout contentContainer = sectionView.findViewById(R.id.section_content);

        titleView.setText(genreName + " Playlists");
        genreSectionsContainer.addView(sectionView);

        apiService.searchPlaylists(genreName).enqueue(new Callback<DeezerResponse<Playlist>>() {
            @Override
            public void onResponse(Call<DeezerResponse<Playlist>> call, Response<DeezerResponse<Playlist>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    for (Playlist playlist : response.body().getData()) {
                        addPopularPlaylistItem(playlist.getTitle(), playlist.getPictureMedium(),
                                "https://api.deezer.com/playlist/" + playlist.getId() + "/tracks", contentContainer);
                    }
                }
            }

            @Override
            public void onFailure(Call<DeezerResponse<Playlist>> call, Throwable t) {
                // Ignore
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

                int limit = (currentSearchMode == SearchMode.ALL) ? 3 : 10;

                if (currentSearchMode == SearchMode.ALL || currentSearchMode == SearchMode.ARTIST) {
                    Response<DeezerResponse<Artist>> artistResponse = apiService.searchArtists(query).execute();
                    if (artistResponse.isSuccessful() && artistResponse.body() != null) {
                        artists = artistResponse.body().getData();
                    }
                }

                if (currentSearchMode == SearchMode.ALL) {
                    for (Genre genre : allGenres) {
                        if (genre.getName() != null && genre.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredGenres.add(genre);
                        }
                    }
                }

                if (currentSearchMode == SearchMode.ALL || currentSearchMode == SearchMode.PLAYLIST) {
                    Response<DeezerResponse<Playlist>> playlistResponse = apiService.searchPlaylists(query).execute();
                    if (playlistResponse.isSuccessful() && playlistResponse.body() != null) {
                        playlists = playlistResponse.body().getData();
                    }
                }

                List<Artist> finalArtists = artists;
                List<Playlist> finalPlaylists = playlists;
                int finalLimit = limit;

                searchHandler.post(() -> {
                    if (isDestroyed() || isFinishing())
                        return;

                    if (finalArtists != null && !finalArtists.isEmpty()) {
                        addHeader("Artists");
                        for (int i = 0; i < Math.min(finalArtists.size(), finalLimit); i++) {
                            addArtistItem(finalArtists.get(i));
                        }
                    }

                    if (!filteredGenres.isEmpty()) {
                        addHeader("Genres");
                        for (int i = 0; i < Math.min(filteredGenres.size(), 3); i++) {
                            addGenreItem(filteredGenres.get(i));
                        }
                    }

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
        challengesContent.setVisibility(View.VISIBLE);
        searchResultsContainer.setVisibility(View.GONE);
    }

    private void showSearchResults() {
        challengesContent.setVisibility(View.GONE);
        searchResultsContainer.setVisibility(View.VISIBLE);
    }

    private void enableSearchMode(SearchMode mode) {
        currentSearchMode = mode;
        showSearchResults();
        searchResultsContent.removeAllViews();

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
                "https://api.deezer.com/artist/" + artist.getId() + "/top?limit=50", "Artist",
                artist.getPictureMedium()));
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
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                .into(image);

        view.setOnClickListener(v -> startChallenge(playlist.getTitle(),
                "https://api.deezer.com/playlist/" + playlist.getId() + "/tracks", "Playlist",
                playlist.getPictureMedium()));
        searchResultsContent.addView(view);
    }

    private void startChallenge(String challengeName, String apiUrl, String subtitle, String imageUrl) {
        // Save to recently played
        recentlyPlayedManager.addRecentlyPlayed(
                new RecentlyPlayedManager.RecentlyPlayedItem(challengeName, subtitle, apiUrl, imageUrl));

        Intent intent = new Intent(SelectChallengeActivity.this, LoadingActivity.class);
        intent.putExtra("challenge_type", challengeName);
        intent.putExtra("challenge_subtitle", subtitle);
        if (apiUrl != null) {
            intent.putExtra("api_url", apiUrl);
        }
        startActivity(intent);
    }

    // Overload for backward compatibility if needed, though I updated all calls
    private void startChallenge(String challengeName, String apiUrl) {
        startChallenge(challengeName, apiUrl, "Unknown", "");
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
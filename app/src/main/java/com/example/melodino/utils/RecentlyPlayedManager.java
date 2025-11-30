package com.example.melodino.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RecentlyPlayedManager {
    private static final String PREF_NAME = "MelodinoPrefs";
    private static final String KEY_RECENTLY_PLAYED = "recently_played";
    private static final int MAX_ITEMS = 5;

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public RecentlyPlayedManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addRecentlyPlayed(RecentlyPlayedItem item) {
        List<RecentlyPlayedItem> items = getRecentlyPlayed();

        // Remove duplicates
        items.removeIf(existing -> existing.getTitle().equals(item.getTitle()));

        // Add to top
        items.add(0, item);

        // Limit size
        if (items.size() > MAX_ITEMS) {
            items = items.subList(0, MAX_ITEMS);
        }

        saveRecentlyPlayed(items);
    }

    public List<RecentlyPlayedItem> getRecentlyPlayed() {
        String json = sharedPreferences.getString(KEY_RECENTLY_PLAYED, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<RecentlyPlayedItem>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    private void saveRecentlyPlayed(List<RecentlyPlayedItem> items) {
        String json = gson.toJson(items);
        sharedPreferences.edit().putString(KEY_RECENTLY_PLAYED, json).apply();
    }

    public static class RecentlyPlayedItem {
        private String title;
        private String subtitle;
        private String apiUrl;
        private String imageUrl;

        public RecentlyPlayedItem(String title, String subtitle, String apiUrl, String imageUrl) {
            this.title = title;
            this.subtitle = subtitle;
            this.apiUrl = apiUrl;
            this.imageUrl = imageUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}

package com.example.melodino;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.melodino.adapters.SuggestionsAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.melodino.utils.AudioPlayer;
import com.example.melodino.utils.Levenshtein;

import java.util.Objects;
import java.util.Random;

import android.os.Handler;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {

    // AutoComplete HELP List
    // AutoComplete HELP List
    // Removed hardcoded SONGS array as per user request

    // SETTINGS
    private static final int MAX_ATTEMPTS = 5;
    // Hardcoded durations for each attempt: 2s, 8s, 16s, 30s, 30s
    private static final int[] DURATIONS = { 2000, 8000, 16000, 30000, 30000 };
    private static final int TOTAL_SONG_DURATION = 30000; // 30 seconds total
    private static final int MAX_POINTS = 500; // Base max points
    private static final int MIN_POINTS = 100;

    private AudioPlayer audioPlayer;
    private ImageButton playButton;
    private TextView titleText;
    private TextView[] answerTextViews;
    private EditText answerInput;
    private Button submitButton;
    private Button skipButton;
    private TextView timeText;
    private TextView pointsText;
    private View progressBar;

    private String correctAnswer = "";
    private String[] attempts = new String[MAX_ATTEMPTS];
    private int currentAttempt = 0;
    private int playbackDuration = DURATIONS[0];
    private long playbackStartTime = 0;

    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;
    private long levelStartTime;

    private RecyclerView suggestionsRecyclerView;
    private SuggestionsAdapter suggestionsAdapter;
    private List<String> allSongsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get song list from intent
        ArrayList<String> receivedSongs = getIntent().getStringArrayListExtra("song_list");
        if (receivedSongs != null) {
            allSongsList = receivedSongs;
        }

        // Initialize views
        answerInput = findViewById(R.id.answer_input);
        suggestionsRecyclerView = findViewById(R.id.suggestions_recycler_view);

        // Setup RecyclerView
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        suggestionsAdapter = new SuggestionsAdapter();
        suggestionsRecyclerView.setAdapter(suggestionsAdapter);

        // Handle item clicks
        suggestionsAdapter.setOnItemClickListener(suggestion -> {
            answerInput.setText(suggestion);
            answerInput.setSelection(suggestion.length()); // Move cursor to end
            suggestionsRecyclerView.setVisibility(View.GONE);
        });

        // TextWatcher for filtering
        answerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSuggestions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Initialize views
        playButton = findViewById(R.id.play_button);
        titleText = findViewById(R.id.title_text);
        submitButton = findViewById(R.id.submit_button);
        skipButton = findViewById(R.id.skip_button);
        timeText = findViewById(R.id.time_text);
        pointsText = findViewById(R.id.points_text);
        progressBar = findViewById(R.id.progress_bar);

        answerTextViews = new TextView[] {
                findViewById(R.id.answer_1_text),
                findViewById(R.id.answer_2_text),
                findViewById(R.id.answer_3_text),
                findViewById(R.id.answer_4_text),
                findViewById(R.id.answer_5_text)
        };

        // Add click listeners to focus input
        View.OnClickListener focusInputListener = v -> {
            answerInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(answerInput, InputMethodManager.SHOW_IMPLICIT);
            }
        };

        for (TextView tv : answerTextViews) {
            tv.setOnClickListener(focusInputListener);
        }

        // Initialize AudioPlayer
        // audioPlayer = new AudioPlayer(this, R.raw.song);
        // ***********************************************

        // Initialize AudioPlayer w random song
        setupRandomSong();

        levelStartTime = System.currentTimeMillis();

        // Update progress bar and points initially
        updateProgressAndPoints();

        // Set up listener to change icon
        audioPlayer.setOnPlaybackListener(new AudioPlayer.OnPlaybackListener() {
            @Override
            public void onPlaybackStarted() {
                playButton.setEnabled(false);
                playButton.setAlpha(0.5f);
                playbackStartTime = System.currentTimeMillis();
                startProgressUpdater();
            }

            @Override
            public void onPlaybackStopped() {
                playButton.setEnabled(true);
                playButton.setAlpha(1.0f);
                stopProgressUpdater();
                updateProgressAndPoints(); // Reset to current duration state
            }

            @Override
            public void onPlaybackStateChanged(boolean isPlaying) {
                if (isPlaying) {
                    playButton.setImageResource(R.drawable.ic_pause);
                } else {
                    playButton.setImageResource(R.drawable.ic_play);
                }
            }

            @Override
            public void onAudioReady() {
                playButton.setEnabled(true);
                playButton.setAlpha(1.0f);
            }

            @Override
            public void onAudioError(String message) {
                Toast.makeText(MainActivity.this, "Error playing song: " + message, Toast.LENGTH_SHORT).show();
                playButton.setEnabled(false);
            }
        });

        // Play button - uses current playback duration
        playButton.setOnClickListener(v -> {
            audioPlayer.playFragment(playbackDuration);
        });

        // Skip button
        skipButton.setOnClickListener(v -> {
            answerInput.setText("");
            submitButton.performClick();
        });

        // Submit button
        submitButton.setOnClickListener(v -> {
            if (currentAttempt >= MAX_ATTEMPTS) {
                return; // Maximum attempts reached
            }

            // Stop playback immediately
            if (audioPlayer != null) {
                audioPlayer.stopPlayback();
            }

            String userAnswer = answerInput.getText().toString().trim().replace(",", "").replace("\"", "");
            if (userAnswer.isEmpty()) {
                userAnswer = "Skipped"; // skipped
            }

            attempts[currentAttempt] = userAnswer;
            TextView currentTextView = answerTextViews[currentAttempt];

            // Check if answer is correct
            // boolean isCorrect = correctAnswer.equalsIgnoreCase(userAnswer);
            // boolean isCorrect =
            // correctAnswer.toLowerCase().contains(userAnswer.toLowerCase());

            // Levenshtein Algorithm
            String correctAnswerTitle = correctAnswer.split(" - ")[0];
            // Calc Levenshtein Distance
            int distance = Levenshtein.computeLevenshteinDistance(correctAnswerTitle, userAnswer);
            boolean isCorrect = distance <= 2 || correctAnswer.equalsIgnoreCase(userAnswer);

            // Update corresponding TextView
            if (userAnswer != null) {
                currentTextView.setText(userAnswer);

                // Add strikethrough if incorrect
                if (!isCorrect) {
                    currentTextView.setPaintFlags(
                            currentTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
            } else {
                currentTextView.setText("Skipped");
                currentTextView.setPaintFlags(
                        currentTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            currentAttempt++;
            answerInput.setText("");

            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(answerInput.getWindowToken(), 0);
            }

            // Update title
            if (isCorrect) {
                titleText.setText("Correct!\nSong: " + correctAnswer);
                playButton.setEnabled(false); // Disable play button on correct answer
                submitButton.setEnabled(false); // Disable submit button
                // add points to the intent
                // WinActivity
                Intent intent = new Intent(MainActivity.this, WinActivity.class);

                int baseScore = (MAX_ATTEMPTS - currentAttempt) * 100;

                long actualTimePlayed = 0;
                if (playbackStartTime > 0) {
                    actualTimePlayed = System.currentTimeMillis() - playbackStartTime;
                    // Cap at total duration
                    actualTimePlayed = Math.min(actualTimePlayed, TOTAL_SONG_DURATION);
                }

                int songTimeBonus = (int) ((TOTAL_SONG_DURATION - actualTimePlayed) / 1000 * 10);

                // Level completion bonus (if under 2 minutes)
                long levelDuration = System.currentTimeMillis() - levelStartTime;
                int levelTimeBonus = levelDuration < 120000 ? 500 : 0;

                int totalScore = Math.max(MIN_POINTS, baseScore + songTimeBonus + levelTimeBonus);

                intent.putExtra("EXTRA_SCORE", totalScore);
                intent.putExtra("EXTRA_POINTS_BONUS", levelTimeBonus);
                intent.putExtra("EXTRA_TIME_BONUS", (int) ((TOTAL_SONG_DURATION - actualTimePlayed) / 1000)); // Seconds
                                                                                                              // saved

                intent.putExtra("cover_url", getIntent().getStringExtra("cover_url"));
                intent.putExtra("api_url", getIntent().getStringExtra("api_url"));
                startActivity(intent);

            } else {
                // Increment playback duration for next attempt
                if (currentAttempt < MAX_ATTEMPTS) {
                    playbackDuration = DURATIONS[currentAttempt];
                }

                // Update progress bar and points for next attempt
                updateProgressAndPoints();

                if (currentAttempt < MAX_ATTEMPTS) {
                    titleText.setText("Incorrect! Attempt " + (currentAttempt + 1) + "/" + MAX_ATTEMPTS);
                } else {
                    titleText.setText("Game over!\nCorrect song: " + correctAnswer);
                    playButton.setEnabled(false);
                    submitButton.setEnabled(false);

                    Intent intent = new Intent(MainActivity.this, GameOverActivity.class);
                    intent.putExtra("correctAnswer", correctAnswer);
                    intent.putExtra("cover_url", getIntent().getStringExtra("cover_url"));
                    intent.putExtra("api_url", getIntent().getStringExtra("api_url"));
                    startActivity(intent);
                }
            }
        });
    }

    private void updateProgressAndPoints() {
        // Calculate progress percentage
        float progressPercent = Math.min((float) playbackDuration / TOTAL_SONG_DURATION, 1.0f);

        // Update time text (convert milliseconds to seconds)
        int currentSeconds = playbackDuration / 1000;
        int totalSeconds = TOTAL_SONG_DURATION / 1000;
        timeText.setText(String.format("0:%02d / 0:%02d", currentSeconds, totalSeconds));

        // Calculate points (more time = less points)
        int points = calculatePoints();
        pointsText.setText("+ " + points + "pts");

        // Update progress bar width using post to ensure parent is measured
        progressBar.post(() -> {
            android.view.View parent = (android.view.View) progressBar.getParent();
            if (parent != null) {
                int parentWidth = parent.getWidth();
                android.view.ViewGroup.LayoutParams params = progressBar.getLayoutParams();
                params.width = (int) (parentWidth * progressPercent);
                progressBar.setLayoutParams(params);
            }
        });
    }

    private int calculatePoints() {
        int basePoints = (MAX_ATTEMPTS - currentAttempt) * 100;
        // Bonus points for time saved: 10 points per second saved
        int timeBonus = (TOTAL_SONG_DURATION - playbackDuration) / 1000 * 10;
        return Math.max(MIN_POINTS, basePoints + timeBonus);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            audioPlayer.release();
        }
        stopProgressUpdater();
    }

    private void startProgressUpdater() {
        final long startTime = System.currentTimeMillis();
        final int duration = playbackDuration;

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min((float) elapsed / TOTAL_SONG_DURATION, 1.0f);

                // Update progress bar
                int parentWidth = ((android.view.View) progressBar.getParent()).getWidth();
                android.view.ViewGroup.LayoutParams params = progressBar.getLayoutParams();
                params.width = (int) (parentWidth * progress);
                progressBar.setLayoutParams(params);

                // Update time text
                int currentSeconds = (int) (elapsed / 1000);
                int totalSeconds = TOTAL_SONG_DURATION / 1000;
                timeText.setText(String.format("0:%02d / 0:%02d", currentSeconds, totalSeconds));

                if (elapsed < duration) {
                    progressHandler.postDelayed(this, 50);
                }
            }
        };
        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdater() {
        if (progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
        }
    }

    private void setupRandomSong() {
        String songUrl = getIntent().getStringExtra("song_url");
        String songTitle = getIntent().getStringExtra("correct_answer");

        if (songUrl != null && songTitle != null) {
            android.util.Log.d("Melodino", "MainActivity Received Song URL: " + songUrl);
            android.util.Log.d("Melodino", "MainActivity Received Title: " + songTitle);
            correctAnswer = songTitle;
            playButton.setEnabled(false); // Disable until ready
            playButton.setAlpha(0.5f);
            audioPlayer = new AudioPlayer(this, songUrl);
        } else {
            Toast.makeText(this, "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterSuggestions(String query) {
        if (query.isEmpty()) {
            suggestionsRecyclerView.setVisibility(View.GONE);
            return;
        }

        List<String> matches = new ArrayList<>();
        for (String song : allSongsList) {
            if (song.toLowerCase().contains(query.toLowerCase())) {
                matches.add(song);
            }
        }

        if (matches.isEmpty()) {
            suggestionsRecyclerView.setVisibility(View.GONE);
            return;
        }

        // Dynamic Suggestion Reduction
        // Attempt 0 (5 left): 6 + 12 = 18
        // Attempt 4 (1 left): 6 + 0 = 6
        int remainingAttempts = MAX_ATTEMPTS - currentAttempt;
        int maxSuggestions = 6 + (remainingAttempts - 1) * 3;

        List<String> finalSuggestions;

        if (matches.size() <= maxSuggestions) {
            finalSuggestions = matches;
        } else {
            finalSuggestions = new ArrayList<>();
            boolean correctIncluded = false;

            // 1. Always include correct answer if it matches
            // We use the full title for checking
            for (String match : matches) {
                if (match.equalsIgnoreCase(correctAnswer)) {
                    finalSuggestions.add(match);
                    correctIncluded = true;
                    break;
                }
            }

            // 2. Fill the rest with random matches
            List<String> remainingMatches = new ArrayList<>(matches);
            if (correctIncluded) {
                remainingMatches.remove(correctAnswer);
            }
            Collections.shuffle(remainingMatches);

            int slotsLeft = maxSuggestions - finalSuggestions.size();
            for (int i = 0; i < slotsLeft && i < remainingMatches.size(); i++) {
                finalSuggestions.add(remainingMatches.get(i));
            }
        }

        // Sort alphabetically for better UX
        Collections.sort(finalSuggestions);

        suggestionsAdapter.setSuggestions(finalSuggestions);
        suggestionsRecyclerView.setVisibility(View.VISIBLE);
    }
}
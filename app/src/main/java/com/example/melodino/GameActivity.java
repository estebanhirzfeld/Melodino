package com.example.melodino;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.melodino.adapters.SuggestionsAdapter;
import com.example.melodino.ui.VisualizerView;
import com.example.melodino.utils.AudioPlayer;
import com.example.melodino.utils.Levenshtein;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.melodino.ui.RoundCompleteActivity;

public class GameActivity extends AppCompatActivity {

    private static final int TOTAL_SONGS = 5;
    private static final int MAX_LIVES_DEFAULT = 3;
    private static final int MAX_LIVES_HARDCORE = 1;
    private static final long SONG_DURATION_MS = 30000; // 30 seconds
    private static final int[] TIME_INCREMENTS = { 2000, 8000, 16000, 30000 }; // 2s, 8s, 16s, 30s

    // UI Elements
    private ImageView[] hearts;
    private TextView playlistInfoText;
    private TextView songCounterText;
    private VisualizerView visualizerView;
    private Button skipButton;
    private ImageButton playButton;
    private Button x2Button;
    private View timeIncrementButton; // Changed to View to match LinearLayout
    private TextView timeIncrementLabel;
    private EditText guessInput;
    private Button submitButton;
    private RecyclerView suggestionsRecyclerView;

    // Game State
    private ArrayList<String> songUrls;
    private ArrayList<String> correctAnswers;
    private ArrayList<String> coverUrls;
    private ArrayList<String> allSongsList;
    private String apiUrl;
    private String challengeType;
    private String gameMode;
    private boolean isInfinite = false;
    private boolean isHardcore = false;
    private boolean isTimeIncrementMode = false;
    private int currentDurationIndex = 0; // 0 to 3

    // Stats
    private int correctGuesses = 0;
    private int x2UsedCount = 0;
    private long startTimeMs;

    private int currentSongIndex = 0;
    private int lives;
    private int score = 0;
    private boolean isX2Active = false;
    private boolean hasGuessedWrong = false;

    private AudioPlayer audioPlayer;
    private CountDownTimer songTimer;
    private long timeRemainingMs = SONG_DURATION_MS;
    private boolean isPlaying = false;

    // Autocomplete
    private SuggestionsAdapter suggestionsAdapter;
    private ObjectAnimator x2Pulsator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize UI
        hearts = new ImageView[] {
                findViewById(R.id.heart1),
                findViewById(R.id.heart2),
                findViewById(R.id.heart3)
        };
        playlistInfoText = findViewById(R.id.playlist_info);
        songCounterText = findViewById(R.id.song_counter);
        visualizerView = findViewById(R.id.visualizer_view);
        skipButton = findViewById(R.id.btn_skip);
        playButton = findViewById(R.id.btn_play);
        x2Button = findViewById(R.id.btn_x2);
        timeIncrementButton = findViewById(R.id.btn_time_increment);
        timeIncrementLabel = findViewById(R.id.tv_increment_label);
        guessInput = findViewById(R.id.guess_input);
        submitButton = findViewById(R.id.btn_submit);
        suggestionsRecyclerView = findViewById(R.id.suggestions_recycler_view);

        // Setup Suggestions
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        suggestionsAdapter = new SuggestionsAdapter();
        suggestionsRecyclerView.setAdapter(suggestionsAdapter);

        suggestionsAdapter.setOnItemClickListener(suggestion -> {
            guessInput.setText(suggestion);
            guessInput.setSelection(suggestion.length());
            suggestionsRecyclerView.setVisibility(View.GONE);
        });

        // Get Data from Intent
        Intent intent = getIntent();
        songUrls = intent.getStringArrayListExtra("song_urls");
        correctAnswers = intent.getStringArrayListExtra("correct_answers");
        coverUrls = intent.getStringArrayListExtra("cover_urls");
        allSongsList = intent.getStringArrayListExtra("all_songs");
        apiUrl = intent.getStringExtra("api_url");
        challengeType = intent.getStringExtra("challenge_type");
        gameMode = intent.getStringExtra("GAME_MODE");
        if (gameMode == null)
            gameMode = "CLASSIC";

        // Configure Mode Flags
        switch (gameMode) {
            case "INFINITE":
                isInfinite = true;
                challengeType = "Infinite";
                break;
            case "SURVIVAL":
                isTimeIncrementMode = true;
                challengeType = "Survival";
                break;
            case "TIME_CHALLENGE": // Legacy support or specific mode
                isTimeIncrementMode = true;
                isInfinite = true;
                challengeType = "Infinite"; // Rename for display
                break;
            case "HARDCORE":
                isInfinite = true;
                isHardcore = true;
                isTimeIncrementMode = true;
                challengeType = "Hardcore"; // Rename for display
                break;
            case "CLASSIC":
                challengeType = "Classic"; // Rename for display
                break;
            default:
                // Standard survival behavior
                break;
        }

        // Set Spotlight Color
        View spotlight = findViewById(R.id.spotlight);
        if (isHardcore) {
            // Red for Hardcore
            spotlight.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#ef4444")));
        } else if (isTimeIncrementMode) {
            // Violet for Competitive (Survival / Infinite)
            spotlight.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#8b5cf6")));
        } else {
            // Green for Casual (Classic / Infinite) - Default
            spotlight.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#10b981")));
        }

        lives = isHardcore ? MAX_LIVES_HARDCORE : MAX_LIVES_DEFAULT;

        String challengeSubtitle = intent.getStringExtra("challenge_subtitle");

        startTimeMs = System.currentTimeMillis();

        if (songUrls == null || songUrls.size() < TOTAL_SONGS) {
            Toast.makeText(this, "Error loading game data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String label = (challengeSubtitle != null && !challengeSubtitle.isEmpty()) ? challengeSubtitle : "Playlist";
        playlistInfoText.setText(label + ": " + (challengeType != null ? challengeType : "Unknown"));

        // Setup Listeners
        playButton.setOnClickListener(v -> togglePlayback());
        skipButton.setOnClickListener(v -> skipSong());
        x2Button.setOnClickListener(v -> toggleX2());
        timeIncrementButton.setOnClickListener(v -> incrementTime());
        submitButton.setOnClickListener(v -> submitGuess());

        // Setup Input
        guessInput.setOnClickListener(v -> {
            guessInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(guessInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        guessInput.addTextChangedListener(new TextWatcher() {
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

        // Start First Level
        startLevel(0);
    }

    private void startLevel(int index) {
        currentSongIndex = index;
        songCounterText.setText("Song " + (index + 1) + "/" + songUrls.size());
        guessInput.setText("");
        guessInput.setHint("Type your guess here...");
        guessInput.setHintTextColor(Color.parseColor("#94a3b8"));

        if (isTimeIncrementMode) {
            currentDurationIndex = 0;
            timeRemainingMs = TIME_INCREMENTS[0];
            timeIncrementButton.setVisibility(View.VISIBLE);
            updateIncrementLabel();
        } else {
            currentDurationIndex = TIME_INCREMENTS.length - 1;
            timeRemainingMs = SONG_DURATION_MS;
            timeIncrementButton.setVisibility(View.GONE);
        }

        // Reset x2
        isX2Active = false;
        hasGuessedWrong = false;
        updateX2ButtonState();
        updateHearts();

        // Reset Visualizer
        visualizerView.setPlaying(false);

        // Load Audio
        if (audioPlayer != null) {
            audioPlayer.release();
        }

        // Disable play button until audio is ready
        playButton.setEnabled(false);
        playButton.setAlpha(0.5f);
        playButton.setImageResource(R.drawable.ic_play_arrow);

        // Re-enable other controls
        submitButton.setEnabled(true);
        skipButton.setEnabled(true);

        audioPlayer = new AudioPlayer(this, songUrls.get(index));
        audioPlayer.setOnPlaybackListener(new AudioPlayer.OnPlaybackListener() {
            @Override
            public void onPlaybackStarted() {
                isPlaying = true;
                visualizerView.setPlaying(true);
                playButton.setImageResource(R.drawable.ic_pause);
                startTimer();
            }

            @Override
            public void onPlaybackStopped() {
                isPlaying = false;
                visualizerView.setPlaying(false);
                playButton.setImageResource(R.drawable.ic_play_arrow);
                stopTimer();

                // If the song stopped naturally or via timer and we are very close to the end,
                // treat it as a timeout to reset the state for the next press.
                if (timeRemainingMs < 500) {
                    handleTimeOut();
                }
            }

            @Override
            public void onPlaybackStateChanged(boolean isPlaying) {
            }

            @Override
            public void onAudioReady() {
                // Audio is ready to play
                playButton.setEnabled(true);
                playButton.setAlpha(1.0f);
            }

            @Override
            public void onAudioError(String message) {
                Toast.makeText(GameActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void togglePlayback() {
        if (audioPlayer == null)
            return;
        if (isPlaying) {
            audioPlayer.stopPlayback();
        } else {
            audioPlayer.playFragment((int) timeRemainingMs);
        }
    }

    private void startTimer() {
        if (songTimer != null)
            songTimer.cancel();
        songTimer = new CountDownTimer(timeRemainingMs, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingMs = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                timeRemainingMs = 0;
                handleTimeOut();
            }
        }.start();
    }

    private void stopTimer() {
        if (songTimer != null) {
            songTimer.cancel();
        }
    }

    private void handleTimeOut() {
        // Reset time immediately so if stopPlayback triggers onPlaybackStopped,
        // it sees the full duration and doesn't loop back here.
        isPlaying = false;

        // In Time Increment mode, if we time out, we don't reset duration,
        // user must manually tap to increase time if they want more.
        if (!isTimeIncrementMode) {
            timeRemainingMs = SONG_DURATION_MS;
        } else {
            timeRemainingMs = TIME_INCREMENTS[currentDurationIndex];
        }
    }

    private void submitGuess() {
        String guess = guessInput.getText().toString().trim();
        if (guess.isEmpty())
            return;

        String correct = correctAnswers.get(currentSongIndex);
        String correctTitle = correct.split(" - ")[0];
        int distance = Levenshtein.computeLevenshteinDistance(correctTitle, guess);
        boolean isCorrect = distance <= 2 || correct.equalsIgnoreCase(guess)
                || correct.toLowerCase().contains(guess.toLowerCase());

        if (isCorrect) {
            handleCorrectGuess();
        } else {
            handleIncorrectGuess();
        }
    }

    private void handleCorrectGuess() {
        audioPlayer.stopPlayback();
        stopTimer();

        // Disable controls to prevent race conditions
        playButton.setEnabled(false);
        playButton.setAlpha(0.5f);
        submitButton.setEnabled(false);
        skipButton.setEnabled(false);

        int points = 100;

        // Bonus for Time Increment Mode (less time = more points)
        if (isTimeIncrementMode) {
            // Base: 100. Bonus: (3 - index) * 50.
            // Index 0 (2s) -> +150 -> 250 total
            // Index 3 (30s) -> +0 -> 100 total
            points += (TIME_INCREMENTS.length - 1 - currentDurationIndex) * 50;
        }

        if (isX2Active) {
            points *= 2;
            x2UsedCount++;
        }
        score += points;
        correctGuesses++;

        Toast.makeText(this, "Correct! + " + points, Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(this::nextLevel, 1500);
    }

    private void handleIncorrectGuess() {
        boolean wasX2 = isX2Active;

        guessInput.setText("");
        guessInput.setHint("Wrong! Try again.");
        guessInput.setHintTextColor(Color.RED);
        loseLife();
        hasGuessedWrong = true;

        if (wasX2 && lives > 0) {
            Toast.makeText(this, "Risk failed! Skipping song.", Toast.LENGTH_SHORT).show();
            audioPlayer.stopPlayback();
            stopTimer();
            nextLevel();
        }
    }

    private void skipSong() {
        audioPlayer.stopPlayback();
        stopTimer();
        if (!hasGuessedWrong) {
            loseLife(); // Skipping costs a life only if haven't guessed wrong yet
        }
        if (lives > 0) {
            nextLevel();
        }
    }

    private void toggleX2() {
        if (lives < 2) {
            Toast.makeText(this, "Not enough lives for x2 risk!", Toast.LENGTH_SHORT).show();
            return;
        }

        isX2Active = !isX2Active;
        updateX2ButtonState();
        updateHearts();

        if (isX2Active) {
            Toast.makeText(this, "x2 Active! Risking 2 lives.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateX2ButtonState() {
        if (lives < 2) {
            x2Button.setVisibility(View.INVISIBLE); // Keep layout space
            stopPulsating();
            return;
        }

        x2Button.setVisibility(View.VISIBLE);

        if (isX2Active) {
            x2Button.setBackgroundResource(R.drawable.bg_organic_gradient);
            stopPulsating();
        } else {
            x2Button.setBackgroundResource(R.drawable.bg_organic_border);
            startPulsating();
        }
    }

    private void startPulsating() {
        if (x2Pulsator == null) {
            x2Pulsator = ObjectAnimator.ofPropertyValuesHolder(
                    x2Button,
                    PropertyValuesHolder.ofFloat("scaleX", 1.05f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.05f));
            x2Pulsator.setDuration(500);
            x2Pulsator.setRepeatCount(ObjectAnimator.INFINITE);
            x2Pulsator.setRepeatMode(ObjectAnimator.REVERSE);
        }
        if (!x2Pulsator.isRunning()) {
            x2Pulsator.start();
        }
    }

    private void stopPulsating() {
        if (x2Pulsator != null) {
            x2Pulsator.cancel();
            x2Button.setScaleX(1f);
            x2Button.setScaleY(1f);
        }
    }

    private void loseLife() {
        int livesLost = isX2Active ? 2 : 1;
        lives -= livesLost;

        updateHearts();
        updateX2ButtonState(); // Check if button should hide

        if (lives <= 0) {
            roundComplete();
        } else {
            isX2Active = false;
            updateX2ButtonState();
            updateHearts();
        }
    }

    private void updateHearts() {
        for (int i = 0; i < MAX_LIVES_DEFAULT; i++) {
            if (i < lives) {
                hearts[i].setVisibility(View.VISIBLE);

                // If x2 active, last 2 visible hearts are yellow (right to left)
                // Visible hearts are at indices 0 to lives-1.
                // So we want to target lives-1 and lives-2.
                boolean isRisked = isX2Active && (i == lives - 1 || i == lives - 2);

                if (isRisked) {
                    hearts[i].setImageResource(R.drawable.ic_heart_yellow);
                    hearts[i].clearColorFilter();
                    hearts[i].setImageTintList(null); // Explicitly clear tint
                } else {
                    hearts[i].setImageResource(R.drawable.ic_heart);
                    hearts[i].setColorFilter(Color.parseColor("#ef4444")); // Red tint
                }

            } else {
                hearts[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    private void nextLevel() {
        if (currentSongIndex < songUrls.size() - 1) {
            startLevel(currentSongIndex + 1);
        } else {
            // End of list
            if (isInfinite) {
                // For now, just end game or maybe loop?
                // User request implies "Infinite" but we only have loaded songs.
                // We'll treat running out of songs as a "Win" or "Round Complete" even in
                // infinite for now,
                // or we could loop back to 0? Looping might be confusing.
                // Let's go to RoundComplete.
                roundComplete();
            } else {
                roundComplete();
            }
        }
    }

    // Removed gameOver and gameWin methods as they are replaced by roundComplete

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

        // Removed limit as requested
        suggestionsAdapter.setSuggestions(matches);
        suggestionsRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            audioPlayer.release();
        }
        stopTimer();
        stopPulsating();
    }

    private void roundComplete() {
        long totalTimeSeconds = (System.currentTimeMillis() - startTimeMs) / 1000;
        String timeString = String.format("%dm %ds", totalTimeSeconds / 60, totalTimeSeconds % 60);

        Intent intent = new Intent(this, RoundCompleteActivity.class);
        intent.putExtra(RoundCompleteActivity.EXTRA_SCORE, score);
        intent.putExtra(RoundCompleteActivity.EXTRA_CORRECT_GUESSES, correctGuesses);
        intent.putExtra(RoundCompleteActivity.EXTRA_TOTAL_SONGS, isInfinite ? correctGuesses : songUrls.size());
        intent.putExtra(RoundCompleteActivity.EXTRA_TOTAL_TIME, timeString);
        intent.putExtra(RoundCompleteActivity.EXTRA_X2_USED, x2UsedCount);
        intent.putExtra(RoundCompleteActivity.EXTRA_CHALLENGE_NAME, challengeType);
        intent.putExtra(RoundCompleteActivity.EXTRA_API_URL, apiUrl);
        intent.putExtra("GAME_MODE", gameMode);

        startActivity(intent);
        finish();
    }

    private void incrementTime() {
        if (currentDurationIndex < TIME_INCREMENTS.length - 1) {
            currentDurationIndex++;
            timeRemainingMs = TIME_INCREMENTS[currentDurationIndex];
            updateIncrementLabel();

            // Visual feedback
            Toast.makeText(this, "Time extended to " + (timeRemainingMs / 1000) + "s", Toast.LENGTH_SHORT).show();

            // If playing, restart with new duration? Or just let user press play?
            // "adding the same behaviour of the current MainActivity" -> MainActivity
            // auto-played next tier on failure,
            // but here it's a manual button. Let's just update the state and maybe stop
            // playback if running so they can restart?
            // Or if they tap while playing, extend the timer?
            // Simplest: Stop playback, ready to play new duration.
            if (isPlaying) {
                audioPlayer.stopPlayback();
                visualizerView.setPlaying(false);
                playButton.setImageResource(R.drawable.ic_play_arrow);
                isPlaying = false;
                stopTimer();
            }

            // Auto-play? User said "button floating button for time increment".
            // Usually manual action implies manual play.
            // But to be smooth, maybe auto-play.
            // Let's stick to manual play for control.
        } else {
            Toast.makeText(this, "Max duration reached!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateIncrementLabel() {
        if (currentDurationIndex < TIME_INCREMENTS.length - 1) {
            long current = TIME_INCREMENTS[currentDurationIndex];
            long next = TIME_INCREMENTS[currentDurationIndex + 1];
            long diffSeconds = (next - current) / 1000;
            timeIncrementLabel.setText("+" + diffSeconds + "s");
        } else {
            timeIncrementLabel.setText("MAX");
        }
    }
}
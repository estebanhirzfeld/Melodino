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
import java.util.List;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.melodino.utils.AudioPlayer;
import com.example.melodino.utils.Levenshtein;

import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // AutoComplete HELP List
    // AutoComplete HELP List
    // Removed hardcoded SONGS array as per user request

    // SETTINGS
    private static final int MAX_ATTEMPTS = 5;
    private static final int INITIAL_DURATION = 1000; // 1 second
    private static final double DURATION_INCREMENT = 1.6; // Multiply by 1.6 per failed attempt
    private static final int TOTAL_SONG_DURATION = 15000; // 15 seconds total
    private static final int MAX_POINTS = 5;
    private static final int MIN_POINTS = 1;

    private AudioPlayer audioPlayer;
    private ImageButton playButton;
    private TextView titleText;
    private TextView[] answerTextViews;
    private EditText answerInput;
    private Button submitButton;
    private TextView timeText;
    private TextView pointsText;
    private View progressBar;

    private String correctAnswer = "";
    private String[] attempts = new String[MAX_ATTEMPTS];
    private int currentAttempt = 0;
    private int playbackDuration = INITIAL_DURATION;

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

        // Initialize AudioPlayer
        // audioPlayer = new AudioPlayer(this, R.raw.song);
        // ***********************************************

        // Initialize AudioPlayer w random song
        setupRandomSong();

        // Update progress bar and points initially
        updateProgressAndPoints();

        // Set up listener to change icon
        audioPlayer.setOnPlaybackListener(new AudioPlayer.OnPlaybackListener() {
            @Override
            public void onPlaybackStarted() {
                playButton.setEnabled(false);
            }

            @Override
            public void onPlaybackStopped() {
                playButton.setEnabled(true);
            }

            @Override
            public void onPlaybackStateChanged(boolean isPlaying) {
                if (isPlaying) {
                    playButton.setImageResource(R.drawable.ic_pause);
                } else {
                    playButton.setImageResource(R.drawable.ic_play);
                }
            }
        });

        // Play button - uses current playback duration
        playButton.setOnClickListener(v -> {
            audioPlayer.playFragment(playbackDuration);
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
                intent.putExtra("EXTRA_SCORE", calculatePoints() * 100 + 100);
                intent.putExtra("cover_url", getIntent().getStringExtra("cover_url"));
                intent.putExtra("api_url", getIntent().getStringExtra("api_url"));
                startActivity(intent);

            } else {
                // Increment playback duration for next attempt
                double adjustment = currentAttempt >= 1 ? currentAttempt * 1000 : 0;

                playbackDuration = (int) Math.ceil(1000 * Math.pow(DURATION_INCREMENT, currentAttempt) + adjustment);

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
        return MAX_POINTS - currentAttempt;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            audioPlayer.release();
        }
    }

    private void setupRandomSong() {
        String songUrl = getIntent().getStringExtra("song_url");
        String songTitle = getIntent().getStringExtra("correct_answer");

        if (songUrl != null && songTitle != null) {
            correctAnswer = songTitle;
            audioPlayer = new AudioPlayer(this, songUrl);
        } else {
            // Fallback to local resources if no URL provided (legacy support)
            int[] songResources = {
                    R.raw.du_hast_rammstein,
                    R.raw.i_wonder_kanye_west,
                    R.raw.mama_im_coming_home_ozzy_osbourne,
                    R.raw.paranoid_black_sabbath,
                    R.raw.sonne_rammstein,
                    R.raw.stronger_kanye_west,
                    R.raw.war_pigs_black_sabbath
            };

            String[] songNames = {
                    "Du Hast - Rammstein",
                    "I Wonder - Kanye West",
                    "Mama Im Coming Home - Ozzy Osbourne",
                    "Paranoid - Black Sabbath",
                    "Sonne - Rammstein",
                    "Stronger - Kanye West",
                    "War Pigs - Black Sabbath"
            };

            Random random = new Random();
            int randomIndex = random.nextInt(songResources.length);

            int currentSongResource = songResources[randomIndex];
            correctAnswer = songNames[randomIndex];

            audioPlayer = new AudioPlayer(this, currentSongResource);
        }
    }

    private void filterSuggestions(String query) {
        if (query.isEmpty()) {
            suggestionsRecyclerView.setVisibility(View.GONE);
            return;
        }

        List<String> filteredList = new ArrayList<>();
        for (String song : allSongsList) {
            if (song.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(song);
            }
        }

        if (filteredList.isEmpty()) {
            suggestionsRecyclerView.setVisibility(View.GONE);
        } else {
            suggestionsAdapter.setSuggestions(filteredList);
            suggestionsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
package com.example.melodino.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.melodino.LoadingActivity;
import com.example.melodino.MainActivity;
import com.example.melodino.R;
import com.example.melodino.WelcomeActivity;

public class RoundCompleteActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "EXTRA_SCORE";
    public static final String EXTRA_CORRECT_GUESSES = "EXTRA_CORRECT_GUESSES";
    public static final String EXTRA_TOTAL_SONGS = "EXTRA_TOTAL_SONGS";
    public static final String EXTRA_TOTAL_TIME = "EXTRA_TOTAL_TIME";
    public static final String EXTRA_X2_USED = "EXTRA_X2_USED";
    public static final String EXTRA_CHALLENGE_NAME = "EXTRA_CHALLENGE_NAME";
    public static final String EXTRA_API_URL = "api_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_complete);

        // Initialize Views
        TextView scoreText = findViewById(R.id.score_text);
        TextView challengeNameText = findViewById(R.id.challenge_name);
        TextView correctGuessesText = findViewById(R.id.correct_guesses_text);
        TextView totalTimeText = findViewById(R.id.total_time_text);
        TextView x2PointsText = findViewById(R.id.x2_points_text);
        Button playAgainButton = findViewById(R.id.play_again_button);
        Button mainMenuButton = findViewById(R.id.main_menu_button);

        // Get Data from Intent
        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int correctGuesses = getIntent().getIntExtra(EXTRA_CORRECT_GUESSES, 0);
        int totalSongs = getIntent().getIntExtra(EXTRA_TOTAL_SONGS, 5);
        String totalTime = getIntent().getStringExtra(EXTRA_TOTAL_TIME);
        int x2Used = getIntent().getIntExtra(EXTRA_X2_USED, 0);
        String challengeName = getIntent().getStringExtra(EXTRA_CHALLENGE_NAME);
        String apiUrl = getIntent().getStringExtra(EXTRA_API_URL);

        if (totalTime == null)
            totalTime = "0m 0s";
        if (challengeName == null)
            challengeName = "Unknown Challenge";

        // Set Data
        scoreText.setText(String.format("%,d", score));
        challengeNameText.setText(challengeName);
        correctGuessesText.setText(correctGuesses + " / " + totalSongs);
        totalTimeText.setText(totalTime);
        x2PointsText.setText(String.valueOf(x2Used));

        // Setup Buttons
        playAgainButton.setOnClickListener(v -> {
            if (apiUrl != null) {
                Intent intent = new Intent(RoundCompleteActivity.this, LoadingActivity.class);
                intent.putExtra("api_url", apiUrl);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(RoundCompleteActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        mainMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(RoundCompleteActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Disable back button
    }
}

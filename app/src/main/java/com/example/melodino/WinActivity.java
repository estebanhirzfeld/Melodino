package com.example.melodino;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.xml.KonfettiView;
import java.util.concurrent.TimeUnit;

public class WinActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "EXTRA_SCORE";
    public static final String EXTRA_POINTS_BONUS = "EXTRA_POINTS_BONUS";
    public static final String EXTRA_TIME_BONUS = "EXTRA_TIME_BONUS";

    private TextView scoreText;
    private TextView pointsBonusText;
    private TextView timeBonusText;
    private Button shareButton;
    private Button playAgainButton;
    private Button mainMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);

        // Initialize views
        scoreText = findViewById(R.id.score_text);
        pointsBonusText = findViewById(R.id.points_bonus_text);
        timeBonusText = findViewById(R.id.time_bonus_text);
        shareButton = findViewById(R.id.share_button);
        playAgainButton = findViewById(R.id.play_again_button);
        mainMenuButton = findViewById(R.id.main_menu_button);

        // Get data from intent
        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int pointsBonus = getIntent().getIntExtra(EXTRA_POINTS_BONUS, 0);
        int timeBonus = getIntent().getIntExtra(EXTRA_TIME_BONUS, 0);

        // Display score with formatting
        scoreText.setText(String.format("%,d", score));
        pointsBonusText.setText(String.format("+ %d points", pointsBonus));
        timeBonusText.setText(String.format("-%ds bonus", timeBonus));

        // Share button
        shareButton.setOnClickListener(v -> {
            String message = "Just crushed it at Melodino! Score: " + score;
            shareScore(message, score);
        });

        // Play again button
        playAgainButton.setOnClickListener(v -> {
            // Go back to MainActivity
            Intent intent = new Intent(WinActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Main menu button
        mainMenuButton.setOnClickListener(v -> {
            // TODO: Navigate to main menu
            // For now, just go back to MainActivity
            Intent intent = new Intent(WinActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // In your Activity or Fragment
        KonfettiView konfettiView = findViewById(R.id.konfettiView);

        Party party = new PartyFactory(
                new Emitter(5, TimeUnit.SECONDS).max(score)
        ).build();

        konfettiView.start(party);
    }

    private void shareScore(String message, int score) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Melodino Score!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message + "\n\nPlay Melodino and beat my score!");

        try {
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No apps available to share", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to game
        Intent intent = new Intent(WinActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
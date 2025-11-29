package com.example.melodino;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import android.widget.ImageView;

public class GameOverActivity extends AppCompatActivity {

    public static final String EXTRA_CORRECT_ANSWER = "EXTRA_CORRECT_ANSWER";

    private TextView finalScoreText;
    private TextView correctAnswerText;
    private Button tryAgainButton;
    private Button mainMenuButton;
    private ImageView sadDinoImage;
    private ImageView correctAnswerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Initialize views
        correctAnswerText = findViewById(R.id.correct_answer_text);
        tryAgainButton = findViewById(R.id.try_again_button);
        mainMenuButton = findViewById(R.id.main_menu_button);
        sadDinoImage = findViewById(R.id.sad_dino_image);
        correctAnswerImage = findViewById(R.id.correct_answer_image);

        // Get data from intent with null safety
        String correctAnswer = getIntent().getStringExtra("correctAnswer");
        String coverUrl = getIntent().getStringExtra("cover_url");
        String apiUrl = getIntent().getStringExtra("api_url");

        if (correctAnswer != null && !correctAnswer.isEmpty()) {
            correctAnswerText.setText(correctAnswer);
        } else {
            correctAnswerText.setText("Answer not available");
        }

        // Load album cover into the correct answer image, keep sad dino as is
        if (coverUrl != null) {
            Glide.with(this)
                    .load(coverUrl)
                    .placeholder(R.drawable.ic_music_note)
                    .error(R.drawable.ic_music_note)
                    .into(correctAnswerImage);
        }

        // Try again button
        tryAgainButton.setOnClickListener(v -> {
            if (apiUrl != null) {
                Intent intent = new Intent(GameOverActivity.this, LoadingActivity.class);
                intent.putExtra("api_url", apiUrl);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                // Fallback to MainActivity
                Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Main menu button
        mainMenuButton.setOnClickListener(v -> {
            // TODO: Navigate to main menu
            // For now, go to WelcomeActivity
            Intent intent = new Intent(GameOverActivity.this, WelcomeActivity.class);
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
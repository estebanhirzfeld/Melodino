package com.example.melodino;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    public static final String EXTRA_CORRECT_ANSWER = "EXTRA_CORRECT_ANSWER";

    private TextView finalScoreText;
    private TextView correctAnswerText;
    private Button tryAgainButton;
    private Button mainMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Initialize views
        correctAnswerText = findViewById(R.id.correct_answer_text);
        tryAgainButton = findViewById(R.id.try_again_button);
        mainMenuButton = findViewById(R.id.main_menu_button);

        // Get data from intent with null safety
        String correctAnswer = getIntent().getStringExtra("correctAnswer");

        if (correctAnswer != null && !correctAnswer.isEmpty()) {
            correctAnswerText.setText(correctAnswer);
        } else {
            correctAnswerText.setText("Answer not available");
        }


        // Try again button
        tryAgainButton.setOnClickListener(v -> {
            // Go back to MainActivity
            Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
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
        // Prevent going back to game
        Intent intent = new Intent(GameOverActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
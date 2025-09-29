package com.example.melodino;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.melodino.utils.AudioPlayer;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_ATTEMPTS = 5;
    private static final int INITIAL_DURATION = 1000; // 1 second
    private static final double DURATION_INCREMENT = 1.6; // Multiply by 1.6 per failed attempt
    private static final int TOTAL_SONG_DURATION = 15000; // 10 seconds total
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

    private String correctAnswer = "Song Name"; // TODO: Set your correct answer
    private String[] attempts = new String[MAX_ATTEMPTS];
    private int currentAttempt = 0;
    private int playbackDuration = INITIAL_DURATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        playButton = findViewById(R.id.play_button);
        titleText = findViewById(R.id.title_text);
        answerInput = findViewById(R.id.answer_input);
        submitButton = findViewById(R.id.submit_button);
        timeText = findViewById(R.id.time_text);
        pointsText = findViewById(R.id.points_text);
        progressBar = findViewById(R.id.progress_bar);

        answerTextViews = new TextView[]{
                findViewById(R.id.answer_1_text),
                findViewById(R.id.answer_2_text),
                findViewById(R.id.answer_3_text),
                findViewById(R.id.answer_4_text),
                findViewById(R.id.answer_5_text)
        };

        // Initialize AudioPlayer
        audioPlayer = new AudioPlayer(this, R.raw.song);

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

            String userAnswer = answerInput.getText().toString().trim();
            if (userAnswer.isEmpty()) {
                userAnswer = null; // skipped
            }

            attempts[currentAttempt] = userAnswer;
            TextView currentTextView = answerTextViews[currentAttempt];

            // Check if answer is correct
            boolean isCorrect = correctAnswer.equalsIgnoreCase(userAnswer);

            // Update corresponding TextView
            if (userAnswer != null) {
                currentTextView.setText(userAnswer);

                // Add strikethrough if incorrect
                if (!isCorrect) {
                    currentTextView.setPaintFlags(
                            currentTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                    );
                }
            } else {
                currentTextView.setText("Skipped");
                currentTextView.setPaintFlags(
                        currentTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                );
            }

            currentAttempt++;
            answerInput.setText("");

            // Update title
            if (isCorrect) {
                titleText.setText("Correct!");
                playButton.setEnabled(false); // Disable play button on correct answer
                submitButton.setEnabled(false); // Disable submit button
            } else {
                // Increment playback duration for next attempt
                double adjustment = currentAttempt >= 1 ? currentAttempt * 1000 : 0;

                playbackDuration = (int) Math.ceil(1000 * Math.pow(DURATION_INCREMENT, currentAttempt) + adjustment);

                // Update progress bar and points for next attempt
                updateProgressAndPoints();

                if (currentAttempt < MAX_ATTEMPTS) {
                    titleText.setText("Incorrect! Attempt " + (currentAttempt + 1) + "/" + MAX_ATTEMPTS);
                } else {
                    titleText.setText("Game over! Correct answer: " + correctAnswer);
                    playButton.setEnabled(false);
                    submitButton.setEnabled(false);
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
//        // Calculate points based on playback duration
//        // Less time listened = more points
//        float durationRatio = (float) playbackDuration / TOTAL_SONG_DURATION;
//
//        if (durationRatio <= 0.1) return MAX_POINTS;      // 1-2 seconds = 5 pts
//        else if (durationRatio <= 0.2) return MAX_POINTS - 1;  // 2-3 seconds = 4 pts
//        else if (durationRatio <= 0.4) return MAX_POINTS - 2;  // 3-6 seconds = 3 pts
//        else if (durationRatio <= 0.6) return MAX_POINTS - 3;  // 6-10 seconds = 2 pts
//        else return MIN_POINTS;                           // 10+ seconds = 1 pt
        return MAX_POINTS - currentAttempt;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            audioPlayer.release();
        }
    }
}
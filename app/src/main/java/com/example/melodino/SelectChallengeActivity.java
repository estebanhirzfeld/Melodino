package com.example.melodino;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SelectChallengeActivity extends AppCompatActivity {

    private ImageButton backButton;
    private EditText searchInput;
    private CardView challengeTop50;
    private CardView challengeArtist;
    private CardView challengeGenre;
    private CardView challengeRock;
    private CardView challengePop;
    private CardView challengeEdm;
    private CardView challengeHiphop;
    private CardView challenge80s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_challenge);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        searchInput = findViewById(R.id.search_input);

        challengeTop50 = findViewById(R.id.challenge_top50);
        challengeArtist = findViewById(R.id.challenge_artist);
        challengeGenre = findViewById(R.id.challenge_genre);
        challengeRock = findViewById(R.id.challenge_rock);
        challengePop = findViewById(R.id.challenge_pop);
        challengeEdm = findViewById(R.id.challenge_edm);
        challengeHiphop = findViewById(R.id.challenge_hiphop);
        challenge80s = findViewById(R.id.challenge_80s);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Search functionality (basic implementation)
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: Implement search filtering
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Challenge card click listeners
        challengeTop50.setOnClickListener(v -> startChallenge("Top 50"));
        challengeArtist.setOnClickListener(v -> startChallenge("By Artist"));
        challengeGenre.setOnClickListener(v -> startChallenge("By Genre"));
        challengeRock.setOnClickListener(v -> startChallenge("Rock"));
        challengePop.setOnClickListener(v -> startChallenge("Pop"));
        challengeEdm.setOnClickListener(v -> startChallenge("EDM"));
        challengeHiphop.setOnClickListener(v -> startChallenge("Hip-Hop"));
        challenge80s.setOnClickListener(v -> startChallenge("'80s"));
    }

    private static final String TOP_50_URL = "https://api.deezer.com/playlist/1111142221/tracks";

    private void startChallenge(String challengeName) {
        // Navigate to MainActivity with challenge type
        Intent intent = new Intent(SelectChallengeActivity.this, LoadingActivity.class);
        intent.putExtra("challenge_type", challengeName);
        if (challengeName.equals("Top 50")) {
            intent.putExtra("api_url", TOP_50_URL);
        }
        startActivity(intent);
    }
}
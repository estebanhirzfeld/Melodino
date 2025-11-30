package com.example.melodino;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ChooseModeActivity extends AppCompatActivity {

    private CardView cardClassic;
    private CardView cardSurvival;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);

        cardClassic = findViewById(R.id.card_classic);
        cardSurvival = findViewById(R.id.card_survival);
        backButton = findViewById(R.id.back_button);

        // Retrieve data passed from SelectChallengeActivity
        String challengeType = getIntent().getStringExtra("challenge_type");
        String challengeSubtitle = getIntent().getStringExtra("challenge_subtitle");
        String apiUrl = getIntent().getStringExtra("api_url");
        String imageUrl = getIntent().getStringExtra("image_url");

        cardClassic.setOnClickListener(v -> {
            startLoadingActivity("CLASSIC", challengeType, challengeSubtitle, apiUrl, imageUrl);
        });

        cardSurvival.setOnClickListener(v -> {
            startLoadingActivity("SURVIVAL", challengeType, challengeSubtitle, apiUrl, imageUrl);
        });

        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void startLoadingActivity(String mode, String challengeType, String challengeSubtitle, String apiUrl,
            String imageUrl) {
        Intent intent = new Intent(ChooseModeActivity.this, LoadingActivity.class);
        intent.putExtra("GAME_MODE", mode);
        intent.putExtra("challenge_type", challengeType);
        intent.putExtra("challenge_subtitle", challengeSubtitle);
        intent.putExtra("api_url", apiUrl);
        intent.putExtra("image_url", imageUrl);
        startActivity(intent);
    }
}

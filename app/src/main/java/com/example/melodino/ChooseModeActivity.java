package com.example.melodino;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class ChooseModeActivity extends AppCompatActivity {

    private TextView tabCasual;
    private TextView tabCompetitive;
    private LinearLayout contentCasual;
    private LinearLayout contentCompetitive;
    private ConstraintLayout cardClassic;
    private ConstraintLayout cardInfinite;
    private ConstraintLayout cardSurvival;
    private ConstraintLayout cardTimeChallenge;
    private ImageButton footerButton;

    private String challengeType;
    private String challengeSubtitle;
    private String apiUrl;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);

        // Retrieve data passed from SelectChallengeActivity
        challengeType = getIntent().getStringExtra("challenge_type");
        challengeSubtitle = getIntent().getStringExtra("challenge_subtitle");
        apiUrl = getIntent().getStringExtra("api_url");
        imageUrl = getIntent().getStringExtra("image_url");

        initializeViews();
        setupListeners();

        // Default to Casual tab
        selectCasualTab();
    }

    private void initializeViews() {
        tabCasual = findViewById(R.id.tab_casual);
        tabCompetitive = findViewById(R.id.tab_competitive);
        contentCasual = findViewById(R.id.content_casual);
        contentCompetitive = findViewById(R.id.content_competitive);
        cardClassic = findViewById(R.id.card_classic);
        cardInfinite = findViewById(R.id.card_infinite);
        cardSurvival = findViewById(R.id.card_survival);
        cardTimeChallenge = findViewById(R.id.card_time_challenge);
        footerButton = findViewById(R.id.footer_button);
    }

    private void setupListeners() {
        tabCasual.setOnClickListener(v -> selectCasualTab());
        tabCompetitive.setOnClickListener(v -> selectCompetitiveTab());

        cardClassic.setOnClickListener(v -> startLoadingActivity("CLASSIC"));
        cardInfinite.setOnClickListener(v -> startLoadingActivity("INFINITE"));
        cardSurvival.setOnClickListener(v -> startLoadingActivity("SURVIVAL"));
        cardTimeChallenge.setOnClickListener(v -> startLoadingActivity("TIME_CHALLENGE"));

        footerButton.setOnClickListener(v -> finish());
    }

    private void selectCasualTab() {
        // Update Tab Styles
        tabCasual.setBackgroundResource(R.drawable.bg_tab_active_casual);
        tabCasual.setTextColor(ContextCompat.getColor(this, R.color.colorBackgroundDark));

        tabCompetitive.setBackground(null);
        tabCompetitive.setTextColor(ContextCompat.getColor(this, R.color.white50));

        // Show/Hide Content
        contentCasual.setVisibility(View.VISIBLE);
        contentCompetitive.setVisibility(View.GONE);
    }

    private void selectCompetitiveTab() {
        // Update Tab Styles
        tabCasual.setBackground(null);
        tabCasual.setTextColor(ContextCompat.getColor(this, R.color.white50));

        tabCompetitive.setBackgroundResource(R.drawable.bg_tab_active_competitive);
        tabCompetitive.setTextColor(ContextCompat.getColor(this, R.color.white));

        // Show/Hide Content
        contentCasual.setVisibility(View.GONE);
        contentCompetitive.setVisibility(View.VISIBLE);
    }

    private void startLoadingActivity(String mode) {
        Intent intent = new Intent(ChooseModeActivity.this, LoadingActivity.class);
        intent.putExtra("GAME_MODE", mode);
        intent.putExtra("challenge_type", challengeType);
        intent.putExtra("challenge_subtitle", challengeSubtitle);
        intent.putExtra("api_url", apiUrl);
        intent.putExtra("image_url", imageUrl);
        startActivity(intent);
    }
}

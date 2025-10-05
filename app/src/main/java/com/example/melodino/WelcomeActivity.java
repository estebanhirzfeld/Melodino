package com.example.melodino;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MelodinoPrefs";
    private static final String KEY_PLAYER_NAME = "player_name";

    private EditText nameInput;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize views
        nameInput = findViewById(R.id.name_input);
        startButton = findViewById(R.id.start_button);

        // Load saved name if exists
        loadSavedName();

        // Start button click listener
        startButton.setOnClickListener(v -> {
            startGame();
        });

        // Handle "Done" button on keyboard
        nameInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                startGame();
                return true;
            }
            return false;
        });
    }

    private void loadSavedName() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedName = prefs.getString(KEY_PLAYER_NAME, "");
        if (!savedName.isEmpty()) {
            nameInput.setText(savedName);
            nameInput.setSelection(savedName.length()); // Put cursor at end
        }
    }

    private void startGame() {
        String playerName = nameInput.getText().toString().trim();

        if (TextUtils.isEmpty(playerName)) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            nameInput.requestFocus();
            return;
        }

        // Save player name
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_PLAYER_NAME, playerName).apply();

        // Navigate to MainActivity
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        intent.putExtra("player_name", playerName);
        startActivity(intent);
        finish();
    }

    public static String getPlayerName(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        return prefs.getString(KEY_PLAYER_NAME, "Player");
    }
}
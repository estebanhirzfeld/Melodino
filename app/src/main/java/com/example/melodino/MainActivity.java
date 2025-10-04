package com.example.melodino;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

    //AutoComplete HELP List
    private static final String[] SONGS = new String[] {



            //Michael Jackson Songs
            "Bad - Michael Jackson",
            "Billie Jean - Michael Jackson",
            "Thriller - Michael Jackson",
            "Beat It - Michael Jackson",
            "Smooth Criminal - Michael Jackson",
            "Don't Stop 'Til You Get Enough - Michael Jackson",
            "Man in the Mirror - Michael Jackson",
            "The Way You Make Me Feel - Michael Jackson",
            "Black or White - Michael Jackson",
            "Rock with You - Michael Jackson",
            "Wanna Be Startin' Somethin' - Michael Jackson",
            "P.Y.T. (Pretty Young Thing) - Michael Jackson",
            "Human Nature - Michael Jackson",
            "They Don't Care About Us - Michael Jackson",
            "Dirty Diana - Michael Jackson",
            "Heal the World - Michael Jackson",
            "Remember the Time - Michael Jackson",
            "You Are Not Alone - Michael Jackson",
            "Earth Song - Michael Jackson",
            "Jam - Michael Jackson",
            "Scream - Michael Jackson, Janet Jackson",
            "You Rock My World - Michael Jackson",
            "Leave Me Alone - Michael Jackson",
            "Another Part of Me - Michael Jackson",
            "The Girl Is Mine - Michael Jackson, Paul McCartney",
            "Off the Wall - Michael Jackson",
            "She's Out of My Life - Michael Jackson",
            "In the Closet - Michael Jackson",
            "Will You Be There - Michael Jackson",
            "Give In to Me - Michael Jackson",
            "I Want You Back - The Jackson 5",
            "ABC - The Jackson 5",
            "I'll Be There - The Jackson 5",
            "The Love You Save - The Jackson 5",
            "Never Can Say Goodbye - The Jackson 5",
            "Dancing Machine - The Jackson 5",
            "Blame It on the Boogie - The Jacksons",
            "Shake Your Body (Down to the Ground) - The Jacksons",
            "Can You Feel It - The Jacksons",
            "Enjoy Yourself - The Jacksons",

            // Twenty One Pilots Songs
            "Ride - Twenty One Pilots",
            "Stressed Out - Twenty One Pilots",
            "Heathens - Twenty One Pilots",
            "Chlorine - Twenty One Pilots",
            "Car Radio - Twenty One Pilots",
            "Tear in My Heart - Twenty One Pilots",
            "My Blood - Twenty One Pilots",
            "Jumpsuit - Twenty One Pilots",
            "Heavydirtysoul - Twenty One Pilots",
            "Lane Boy - Twenty One Pilots",
            "Holding On To You - Twenty One Pilots",
            "House of Gold - Twenty One Pilots",
            "Guns for Hands - Twenty One Pilots",
            "Migraine - Twenty One Pilots",
            "Trees - Twenty One Pilots",
            "Level of Concern - Twenty One Pilots",
            "Shy Away - Twenty One Pilots",
            "The Hype - Twenty One Pilots",
            "Nico and the Niners - Twenty One Pilots",
            "Goner - Twenty One Pilots",
            "Leave The City - Twenty One Pilots",
            "Neon Gravestones - Twenty One Pilots",
            "Addict With a Pen - Twenty One Pilots",
            "Truce - Twenty One Pilots",

            // General Popular Songs
            "Bohemian Rhapsody - Queen",
            "Like a Rolling Stone - Bob Dylan",
            "Smells Like Teen Spirit - Nirvana",
            "Imagine - John Lennon",
            "What's Going On - Marvin Gaye",
            "Hey Jude - The Beatles",
            "Good Vibrations - The Beach Boys",
            "Johnny B. Goode - Chuck Berry",
            "Stairway to Heaven - Led Zeppelin",
            "Billie Jean - Michael Jackson",
            "Hotel California - Eagles",
            "Sweet Child O' Mine - Guns N' Roses",
            "I Will Always Love You - Whitney Houston",
            "Hallelujah - Leonard Cohen",
            "Wonderwall - Oasis",
            "Yesterday - The Beatles",
            "Superstition - Stevie Wonder",
            "(I Can't Get No) Satisfaction - The Rolling Stones",
            "No Woman, No Cry - Bob Marley & The Wailers",
            "Losing My Religion - R.E.M.",
            "One - U2",
            "Bridge Over Troubled Water - Simon & Garfunkel",
            "God Only Knows - The Beach Boys",
            "Respect - Aretha Franklin",
            "Purple Haze - The Jimi Hendrix Experience",
            "London Calling - The Clash",
            "Born to Run - Bruce Springsteen",
            "Be My Baby - The Ronettes",
            "In Da Club - 50 Cent",
            "Crazy in Love - Beyoncé ft. Jay-Z",
            "Rolling in the Deep - Adele",
            "Uptown Funk - Mark Ronson ft. Bruno Mars",
            "Shape of You - Ed Sheeran",
            "Blinding Lights - The Weeknd",
            "Someone Like You - Adele",
            "Get Lucky - Daft Punk ft. Pharrell Williams",
            "Happy - Pharrell Williams",
            "Call Me Maybe - Carly Rae Jepsen",
            "Gangnam Style - PSY",
            "Despacito - Luis Fonsi & Daddy Yankee ft. Justin Bieber",
            "Old Town Road - Lil Nas X ft. Billy Ray Cyrus",
            "Bad Guy - Billie Eilish",
            "Watermelon Sugar - Harry Styles",
            "Levitating - Dua Lipa",
            "Good 4 U - Olivia Rodrigo",
            "Stay - The Kid LAROI & Justin Bieber",
            "As It Was - Harry Styles",
            "Anti-Hero - Taylor Swift",
            "Flowers - Miley Cyrus",
            "Last Nite - The Strokes",
            "Mr. Brightside - The Killers",
            "Take Me Out - Franz Ferdinand",
            "Seven Nation Army - The White Stripes",
            "Hey Ya! - OutKast",
            "All My Friends - LCD Soundsystem",
            "Cranes in the Sky - Solange",
            "Alright - Kendrick Lamar",
            "Redbone - Childish Gambino",
            "Thinkin Bout You - Frank Ocean",
            "Royals - Lorde",
            "Chandelier - Sia",
            "Formation - Beyoncé",
            "This Is America - Childish Gambino",
            "Thank U, Next - Ariana Grande",
            "Juice - Lizzo",
            "Adore You - Harry Styles",
            "Don't Start Now - Dua Lipa",
            "Save Your Tears - The Weeknd",
            "Montero (Call Me By Your Name) - Lil Nas X",
            "Drivers License - Olivia Rodrigo",
            "Peaches - Justin Bieber ft. Daniel Caesar & Giveon",
            "Kiss Me More - Doja Cat ft. SZA",
            "Industry Baby - Lil Nas X & Jack Harlow",
            "Easy On Me - Adele",
            "Shivers - Ed Sheeran",
            "Heat Waves - Glass Animals",
            "Cold Heart (PNAU Remix) - Elton John & Dua Lipa",
            "abcdefu - GAYLE",
            "The Twist - Chubby Checker",
            "Smooth - Santana ft. Rob Thomas",
            "Mack the Knife - Bobby Darin",
            "Party Rock Anthem - LMFAO ft. Lauren Bennett & GoonRock",
            "I Gotta Feeling - The Black Eyed Peas",
            "Macarena (Bayside Boys Mix) - Los Del Rio",
            "Your Song - Elton John",
            "Take on Me - A-ha",
            //Other Songs
            "Du Hast - Rammstein",
            "I Wonder - Kanye West",
            "Mama, I'm Coming Home - Ozzy Osbourne",
            "Paranoid - Black Sabbath",
            "Sonne - Rammstein",
            "Stronger - Kanye West",
            "War Pigs - Black Sabbath"
    };

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

    private String correctAnswer = SONGS[0]; // TODO: Will be file name  -------------------------------------------------------------------------------
    private String[] attempts = new String[MAX_ATTEMPTS];
    private int currentAttempt = 0;
    private int playbackDuration = INITIAL_DURATION;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //AutoComplete HELP List
        // Get a reference to the AutoCompleteTextView from the layout
        AutoCompleteTextView answerInput = findViewById(R.id.answer_input);

        // Create an adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, SONGS);
        answerInput.setAdapter(adapter);

        // Initialize views
        playButton = findViewById(R.id.play_button);
        titleText = findViewById(R.id.title_text);
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
        // audioPlayer = new AudioPlayer(this, R.raw.song); ***********************************************

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

            String userAnswer = answerInput.getText().toString().trim().replace(",", "").replace("\"", "");
            if (userAnswer.isEmpty()) {
                userAnswer = ""; // skipped
            }

            attempts[currentAttempt] = userAnswer;
            TextView currentTextView = answerTextViews[currentAttempt];

            // Check if answer is correct
//            boolean isCorrect = correctAnswer.equalsIgnoreCase(userAnswer);
//            boolean isCorrect = correctAnswer.toLowerCase().contains(userAnswer.toLowerCase());


            //Levenshtein Algorithm
            String correctAnswerTitle = correctAnswer.split(" - ")[0];
            //Calc Levenshtein Distance
            int distance = Levenshtein.computeLevenshteinDistance(correctAnswerTitle, userAnswer);
            boolean isCorrect = distance <= 2 || correctAnswer.equalsIgnoreCase(userAnswer);


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

            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(answerInput.getWindowToken(), 0);
            }

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
                    titleText.setText("Game over!\nCorrect answer: " + correctAnswer);
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
            "Mama, I'm Coming Home - Ozzy Osbourne", 
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
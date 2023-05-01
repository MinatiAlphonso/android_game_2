package edu.sdsmt.group3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 Project 2 Grading

 firebase login: ryandriscollcsc476@gmail.com
 firebase password: csc476mobile
 Time out period: 30 seconds
 How to reset database (file or button): Button is available on the login page
 Reminder: Mark where the timeout period is set with GRADING: TIMEOUT


 Group:

 __X__ 6pt Game still works and Database setup
 __X__ 8pt Database setup\reset
 __X__ 8pt New user activity
 __X__ 18pt Opening\login activity
 __X__ 5pt rotation


 Individual:

 Sequencing
 __X__ 4pt Registration sequence
 __X__ 9pt Login Sequence
 __X__ 18pt Play Sequence
 __X__ 9pt Exiting menu, and handlers
 __P__ 5pt rotation


 Upload

 __X__ 6pt intial setup
 __X__ 6pt waiting
 __X__ 17pt store game state
 __X__ 11pt notify end/early exits
 __P__ 5pt rotation


 Download

 __X__ 6pt intial setup
 __X__ 6pt waiting
 __X__ 17pt store game state
 __X__ 11pt grab and forward end/early exits
 __P__ 5pt rotation

 Known Bugs:
 On Rotation, a new set of collectibles get drawn at different locations
 even though the locations do seem to get saved to the database.

 Note:
 The way our passive timer works, is that when the game ends passively, that
 player's game ends, but the other player's game ends when their timer runs out.

 Please list any additional rules that may be needed to properly grade your project:

 */

/*
Project 1 Grading

Group:
__Done__ 6pt No redundant activities
__Done__ 6pt How to play dialog
__Done__ 6pt Icons
__Done__ 6pt End activity
__Done__ 6pt Back button handled
How to open the "how to play dialog": Click the help button on the start activity

Individual:

	Play activity and custom view

		__Done__ 9pt Activity appearence
		__Done__ 16pt Static Custom View
		__Done__ 20pt Dynamic part of the Custom View
		__Done__ 15pt Rotation

	Welcome activity and Game Class

		__Done__ 13pt Welcome activity appearence
		__Done__ 20pt Applying capture rules
		__Done__ 12pt Game state
		__Done__ 15pt Rotation
		What is the probaility of the reactangle capture: 1 - the scale of the rectangle (starts at 0.50 scale which would be 50% capture)

	Capture activity and activity sequencing

		__Done__ 9pt Capture activity apearence
		__Done__ 16pt Player round sequencing
		__Done__ 20pt Move to next activity
		__Done__ 15pt Rotation


Please list any additional rules that may be needed to properly grade your project:
 */



public class StartActivity extends AppCompatActivity {
    private final DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference().child("Games");
    // Views to get the data

    private EditText RoundInput;
    private String userId;
    private String displayName;
    private TextView playerDisplayName;
    private String gameId = "";

    // database feilds
    private String player1ScoreDB = "p1score";
    private String player2ScoreDB = "p2score";
    private String p1NameDB = "p1Name";
    private String gameEndedDB = "GameEnded";
    private String collectiblesDB = "collectibles";
    private String p1idDB = "p1id";
    private String roundDB = "round";
    private String turnDB = "turnId";
    private String totalRoundDB = "totalRounds";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        // assign the views
        RoundInput = findViewById(R.id.RoundsInput);
        playerDisplayName = findViewById(R.id.firstPlayerName);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is signed in
            userId = user.getUid();
            displayName = user.getDisplayName();
            playerDisplayName.setText(displayName);
        }
    }


    public void onPlay(View view) {
        // load the data from the views
        String p1Name = displayName;
        String rounds = RoundInput.getText().toString();

        if (rounds.length() == 0) {
            // If a view has no information display a dialog prompting them to enter it
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Information!");
            builder.setMessage("Please enter both players names and the number of rounds to play.");
            builder.setPositiveButton(android.R.string.ok, null);

            AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            makeNewGame(rounds, view);
            // If all the data is entered put it in the intent
            Intent intent = new Intent(this, WaitingActivity.class);
            intent.putExtra(GameActivity.GAME_ID, gameId);
            intent.putExtra(WaitingActivity.ROUND_COUNT, Integer.parseInt(rounds));
            // launch the Wait Activity
            startActivity(intent);
        }
    }

    private void makeNewGame(String rounds, View view) {
        // make the game in the database
        gameId = gameRef.push().getKey();
        DatabaseReference newGame = gameRef.child(gameId);

        DatabaseReference.CompletionListener listen = new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference newGame) {
                if (error != null) {
                    // we have an error
                    Toast.makeText(view.getContext(), "Error in Game Creation", Toast.LENGTH_SHORT).show();
                }
            }
        };

        HashMap<String, Object> result = new HashMap<>();
        result.put(player1ScoreDB, 0);
        result.put(player2ScoreDB, 0);
        result.put(p1NameDB, displayName);
        result.put(p1idDB, userId);
        result.put(roundDB, 1);
        result.put(totalRoundDB, Integer.parseInt(rounds));
        result.put(turnDB, userId);
        result.put(gameEndedDB, false);

        newGame.setValue(result, listen);
    }

    public void onHelp(View view) {
        // create new web view
        WebView webView = new WebView(this);
        // set contents
        webView.loadData(getString(R.string.Help_paragraph), "text/html", "UTF-8");
        // create dialog, set title and add web view to dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(webView);
        builder.setTitle("Asteroids!");
        builder.setPositiveButton(android.R.string.ok, null);

        // show dialog
        AlertDialog alert = builder.create();
        alert.show();
        // get dialog window
        Window dialogWindow = alert.getWindow();
        // set dialog height to match parent and fill screen for easier reading
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(layoutParams);
    }


}
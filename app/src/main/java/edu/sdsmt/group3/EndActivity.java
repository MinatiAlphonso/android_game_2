package edu.sdsmt.group3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class EndActivity extends AppCompatActivity {

    public final static String PLAYER1_SCORE = "edu.sdsmt.group3.PLAYER1_SCORE";
    public final static String PLAYER2_SCORE = "edu.sdsmt.group3.PLAYER2_SCORE";
    private final static String PLAYER1_NAME = "PLAYER1_NAME";
    private final static String PLAYER2_NAME = "PLAYER2_NAME";


    private TextView player1NameTextView;
    private TextView player2NameTextView;
    private TextView player1FinalScoreTextView;
    private TextView player2FinalScoreTextView;
    private TextView victoryTextView;

    private String player1Name;
    private String player2Name;
    private int player1Score;
    private int player2Score;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle){
        super.onSaveInstanceState(bundle);

        // save names
        bundle.putString(PLAYER1_NAME, player1NameTextView.getText().toString());
        bundle.putString(PLAYER2_NAME, player2NameTextView.getText().toString());

        // save scores
        int p1Score = Integer.parseInt(String.valueOf(player1FinalScoreTextView.getText()));
        int p2Score = Integer.parseInt(String.valueOf(player2FinalScoreTextView.getText()));
        bundle.putInt(PLAYER1_SCORE, p1Score);
        bundle.putInt(PLAYER2_SCORE, p2Score);
    }

    protected void loadInstanceState(Bundle bundle) {
        player1Name = bundle.getString(PLAYER1_NAME);
        player2Name = bundle.getString(PLAYER2_NAME);

        player1Score = bundle.getInt(PLAYER1_SCORE);
        player2Score = bundle.getInt(PLAYER2_SCORE);
    }

    protected void getValuesFromIntent(Intent intent) {

        player1Name = intent.getStringExtra(GameActivity.PLAYER1_NAME);
        player2Name = intent.getStringExtra(GameActivity.PLAYER2_NAME);
        player1Score = intent.getIntExtra(PLAYER1_SCORE, 0);
        player2Score = intent.getIntExtra(PLAYER2_SCORE, 0);
    }

    private void updateUI() {
        player1NameTextView = findViewById(R.id.player1FinalTextView);
        player2NameTextView = findViewById(R.id.player2FinalTextView);
        player1FinalScoreTextView = findViewById(R.id.player1FinalScoreTextView);
        player2FinalScoreTextView = findViewById(R.id.player2FinalScoreTextView);
        victoryTextView = findViewById(R.id.victoryTextView);

        player1NameTextView.setText(player1Name);
        player2NameTextView.setText(player2Name);
        player1FinalScoreTextView.setText(String.valueOf(player1Score));
        player2FinalScoreTextView.setText(String.valueOf(player2Score));
        setWinner();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        if (savedInstanceState != null) {
            loadInstanceState(savedInstanceState);
        } else {
            getValuesFromIntent(getIntent());
        }

        updateUI();
    }

    public void onReturnToStartButtonClick(View view) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void setWinner() {
        if (player1Score > player2Score) {
            victoryTextView.setText(R.string.p1win);
        } else if (player2Score > player1Score) {
            victoryTextView.setText(R.string.p2win);
        } else {
            victoryTextView.setText(R.string.tie);
        }
    }
}
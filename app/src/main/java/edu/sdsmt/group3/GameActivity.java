package edu.sdsmt.group3;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

public class GameActivity extends AppCompatActivity {
    private DatabaseReference _gameRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //tag to identify information being passed to THIS activity
    public final static String PLAYER1_NAME = "edu.sdsmt.group3.PLAYER1_NAME";
    public final static String PLAYER2_NAME = "edu.sdsmt.group3.PLAYER2_NAME";
    public final static String ROUND_COUNT = "edu.sdsmt.group3.ROUND_COUNT";
    public final static String GAME_ID = "edu.sdsmt.group3.GAME_ID";
    private final String _endGameDB = "GameEnded";
    private final String _collectiblesDB = "collectibles";
    //gets the capture information back from the captureActivity
    public final static String RETURN_CAPTURE_MESSAGE = "edu.sdsmt.group3.RETURN_CAPTURE_MESSAGE";

    private TextView Player1Name = null;
    private TextView Player2Name = null;
    private TextView RoundCount = null;
    private TextView Turn = null;
    private Button Capture = null;
    private Button CaptureSelect = null;
    private GameView gameView = null;
    private String _gameId;
    private String _playerOneNameFromDb;
    private String _playerTwoNameFromDb = "";
    private String _roundCountFromDb = "";
    private String _totalRounds = "";

    private String _p1id;
    private String _p2id;
    private String _hasGameEnded;
    private String _currentTurnPlayerId;
    private Game game;

    // countdown timer
    CountDownTimer turnTimer;

    private Boolean isCaptureEnabled = false;

    //activity launcher for captureActivity
    ActivityResultLauncher<Intent> captureResultLauncher;


    private ValueEventListener valueListener=null;

    private static final String IS_CAPTURE_ENABLED = "isCaptureEnabled";

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(IS_CAPTURE_ENABLED, isCaptureEnabled);
        gameView.saveGameState(bundle);
    }

    public void setUIPlayerTurn(String turn) {_currentTurnPlayerId = turn;}

    public void updateUI(){
        if (game._currentTurn.equals(user.getUid())) {
            CaptureSelect.setEnabled(true);
        } else {
            CaptureSelect.setEnabled(false);
        }
        Capture.setEnabled(isCaptureEnabled);
        Player1Name.setText(String.format("%s%s", getString(R.string.player1_text),game.getPlayer1().getName()));
        Player2Name.setText(String.format("%s%s", getString(R.string.player2_text),game.getPlayer2().getName()));
        RoundCount.setText(String.format("%s%s%s%s", getString(R.string.round_text), game.getRound(),"/",game.getTotalRounds()));
        Turn.setText(String.format("%s%s%s",getString(R.string.turn_text),"Player ",game.getPlayerTurn()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Log.i("Status","onCreate is called here");

        Intent intent = getIntent();

        _gameId = intent.getStringExtra(GAME_ID);
        Authenticator.INSTANCE.setGameID(_gameId);
        _gameRef = FirebaseDatabase.getInstance().getReference().child("Games").child(_gameId);

        _gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                _hasGameEnded = snapshot.child("GameEnded").getValue().toString();
                _playerOneNameFromDb = snapshot.child("p1Name").getValue().toString();
                _playerTwoNameFromDb = snapshot.child("p2Name").getValue().toString();
                _roundCountFromDb = snapshot.child("round").getValue().toString();
                _totalRounds = snapshot.child("totalRounds").getValue().toString();
                _p1id = snapshot.child("p1id").getValue().toString();
                _p2id = snapshot.child("p2id").getValue().toString();
                _currentTurnPlayerId = snapshot.child("turnId").getValue().toString();
                initGame(savedInstanceState);
                game.updateCollectibleFromDB();
                gameView.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Info from CaptureActivity
        ActivityResultContracts.StartActivityForResult contract =
                new ActivityResultContracts.StartActivityForResult();
        captureResultLauncher =
                registerForActivityResult(contract, (result)->
                { int resultCode = result.getResultCode();
                    if (resultCode == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        game.setCapture(data.getIntExtra(RETURN_CAPTURE_MESSAGE, -1));
                    }});
    }

    public void initGame(Bundle savedInstanceState) {
        Player1Name = (TextView) findViewById(R.id.textViewPlayer1Name);
        Player2Name = (TextView) findViewById(R.id.textViewPlayer2Name);
        RoundCount = (TextView) findViewById(R.id.textViewRoundCount);
        Turn = (TextView) findViewById(R.id.textViewTurn);
        Capture = (Button) findViewById(R.id.buttonCapture);
        CaptureSelect = (Button) findViewById(R.id.buttonSelectCaptureOption);
        gameView = (GameView) findViewById(R.id.gameView);

        game = gameView.getGame();
        //Get the message from the intent (StartActivity started up this activity)
        game.setPlayersNames(_playerOneNameFromDb, _playerTwoNameFromDb);
        game.setTurn(_currentTurnPlayerId);
        game.setRounds(Integer.parseInt(_totalRounds));
        game.setPlayersIDs(_p1id, _p2id);
        game.setGameActivity(this);
        game.setGameRef(_gameRef);

        HashMap<String, Object> collectiblesList = new HashMap<>();
        collectiblesList.put(_collectiblesDB, game.getListCollectiblesDB());
        _gameRef.updateChildren(collectiblesList);

        initTimer();

        startListeningToDB(game);

        /**
         * Restore Game State
         */
        if(savedInstanceState != null){
            isCaptureEnabled = savedInstanceState.getBoolean(IS_CAPTURE_ENABLED);
            gameView.restoreGameState(savedInstanceState);
            updateUI();
        }
        //updateUI();
    }

    // GRADING: TIMEOUT
    private void initTimer() {
        turnTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Used for formatting digit to be in 2 digits only

                // this function gets called every second so we could use it if needed
                NumberFormat f = new DecimalFormat("00");
                long hour = (millisUntilFinished / 3600000) % 24;
                long min = (millisUntilFinished / 60000) % 60;
                long sec = (millisUntilFinished / 1000) % 60;
            }
            // When the timer expires to 0, that means there was a lack of activity and players will be taken to end game
            public void onFinish() {
                openEndActivity();
            }
        };

        turnTimer.start();
    }

    private void restartTimer() {
        // reset the timer
        turnTimer.cancel();
        turnTimer.start();
    }

    private void stopTimer() {
        turnTimer.cancel();
    }

    private void startListeningToDB(final Game game) {
        valueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                game.loadJSON(snapshot);
                game.updateCollectibleFromDB();
                gameView.invalidate();

                //_hasGameEnded = snapshot.child("GameEnded").getValue().toString();
                checkIfGameOver();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        _gameRef.addValueEventListener(valueListener);
    }

    public void onSelectCaptureOption(View view){
        isCaptureEnabled = true;
        Capture.setEnabled(isCaptureEnabled);
        Intent intent = new Intent(this, CaptureActivity.class);
        captureResultLauncher.launch(intent);
    }

    public void onCapture(View view){
        checkIfGameOver();
        isCaptureEnabled = false;
        Capture.setEnabled(isCaptureEnabled);
        game.captureCollectibles();
        game.getListCollectiblesDB().clear();
        game.setListCollectiblesDB();

        restartTimer();

        //reset the capture option
        game.setCapture(-1);

        //redraw the view
        gameView.invalidate();

        game.advanceTurn();

        game.saveGame();

        updateUI();
    }

    public void checkIfGameOver() {
        if (game.getGameState() == Game.GAME_OVER) {
            openEndActivity();
            //this.finish();// close GameActivity so EndActivity will reveal StartActivity on closing
        }
    }


    private void openEndActivity() {
        stopTimer();
        // write to database that game ended
        DatabaseReference.CompletionListener listen = new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference newGame) {
                if (error != null) {
                    // we have an error maybe make a toast??
                    Log.d("DB", "Error in joining game" );
                } else {
                    _gameRef.removeEventListener(valueListener);
                    game.setGameEnded();
                    moveToEnd();
                }
            }
        };

        HashMap<String, Object> result = new HashMap<>();
        result.put(_endGameDB, true);
        _gameRef.updateChildren(result, listen);
    }

    private void moveToEnd() {
        Intent intent = new Intent(this, EndActivity.class);
        intent.putExtra(PLAYER1_NAME, Player1Name.getText());
        intent.putExtra(PLAYER2_NAME, Player2Name.getText());
        intent.putExtra(EndActivity.PLAYER1_SCORE, game.getPlayer1().getScore());
        intent.putExtra(EndActivity.PLAYER2_SCORE, game.getPlayer2().getScore());
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed()//GRADING: BACK
    {
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {
                if(option == DialogInterface.BUTTON_POSITIVE) {
                    finish();
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String dialogMessage = "Both players' progress will be lost. Are you sure you want to quit the game?";
        builder.setMessage(dialogMessage)
                .setPositiveButton("Yes", dialogListener)
                .setNegativeButton("No", dialogListener)
                .show();
    }

    /**
     * Called when it is time to create the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.quit_game:
                onQuitGameSelect();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onQuitGameSelect() {
        openEndActivity();
    }
}
package edu.sdsmt.group3;

import static edu.sdsmt.group3.GameActivity.GAME_ID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WaitingActivity extends AppCompatActivity {
    public final static String PLAYER2_NAME = "edu.sdsmt.group3.PLAYER2_NAME";
    public final static String ROUND_COUNT = "edu.sdsmt.group3.ROUND_COUNT";
    private String p2idDB = "p2id";
    private String p2NameDB = "p2Name";
    private final DatabaseReference _gameRef = FirebaseDatabase.getInstance().getReference().child("Games");
    private String _gameId;
    private DatabaseReference _createdGameRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        //Get the game id from previous activity for database listening
        Intent intent = getIntent();
        _gameId = intent.getStringExtra(GAME_ID);
        _createdGameRef = _gameRef.child(_gameId);

        startWaiting();
    }


    private void startWaiting() {
        _createdGameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // some data has changed
                if (snapshot.child(p2idDB).exists()) {
                    // new user has joined
                    Log.i("GAME", "Someone joined the game!");
                    startGame();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startGame() {
        Intent gameIntent = new Intent(this, GameActivity.class);
        gameIntent.putExtra(GAME_ID, _gameId);
        startActivity(gameIntent);
    }
}

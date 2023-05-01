package edu.sdsmt.group3;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class JoinGame {
    private final DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference().child("Games");
    private String _playerId;
    private String _playerName;
    private DatabaseReference _newGameRef;

    // database values
    private String p2NameDB = "p2Name";
    private String p2idDB = "p2id";


    public JoinGame(String gameId) {
        _newGameRef =  gameRef.child(gameId);
        addUserToTheGame();
    }

    private void addUserToTheGame() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is signed in
            _playerId = user.getUid();
            _playerName = user.getDisplayName();

            DatabaseReference.CompletionListener listen = new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference newGame) {
                    if (error != null) {
                        // we have an error maybe make a toast??
                        Log.d("DB", "Error in joining game" );
                    }
                }
            };

            HashMap<String, Object> result = new HashMap<>();
            result.put(p2idDB, _playerId);
            result.put(p2NameDB, _playerName);

            _newGameRef.updateChildren(result, listen);
        }
    }
}

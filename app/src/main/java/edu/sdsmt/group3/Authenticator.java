package edu.sdsmt.group3;
import static edu.sdsmt.group3.GameActivity.GAME_ID;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Authenticator {
    public final static Authenticator INSTANCE = new Authenticator();
    private String _email;
    private String _password;
    private String _username;
    private LoginActivity _currentIntent;
    private boolean _authenticated = false;
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
    private final DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference().child("Games");
    private FirebaseUser firebaseUser;
    private String _gameId;

    public void setEmail(String email) {
        _email = email;
    }

    public void setPassword(String password) {
        _password = password;
    }

    public void setUsername(String username) {_username = username;}
    public void setCurrentIntent(LoginActivity intent) {_currentIntent = intent;}
    public FirebaseUser getCurrentUser() {return firebaseUser;}

    private Authenticator() {

    }

    public void signIn() {
        Task<AuthResult> result = userAuth.signInWithEmailAndPassword(_email, _password);
        result.addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Log.d("AUTH", "signInWithEmail:onComplete:" + task.isSuccessful());
                    _authenticated = true;

                    // check to see if there is an open game
                    // get most recent game created
                    gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean thereIsAnOpenGame = false;
                            for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                                if(dataSnapshot.hasChild("GameEnded")){
                                    boolean gameEnded = (boolean) dataSnapshot.child("GameEnded").getValue();
                                    if (gameEnded == false) {
                                        thereIsAnOpenGame = true;
                                        _gameId = dataSnapshot.getKey();
                                    }
                                }
                            }

                            if (thereIsAnOpenGame) {
                                // join the game
                                new JoinGame(_gameId);
                                Intent gameIntent = new Intent(_currentIntent, GameActivity.class);
                                gameIntent.putExtra(GAME_ID, _gameId);

                                _currentIntent.startActivity(gameIntent);
                            } else {
                                // no open games so make one
                                Intent startIntent = new Intent(_currentIntent, StartActivity.class);
                                _currentIntent.startActivity(startIntent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Log.w("AUTH", "signInWithEmail:failed", task.getException());
                    _authenticated = false;
                    //createUser(_username, _email, _password);
                }
            }
        });

        startAuthListening();
    }




    private void startAuthListening() {
        userAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if ( firebaseUser != null) {

                    // User is signed in
                    _authenticated = true;
                    Log.d("AUTH", "onAuthStateChanged:signed_in:" +  firebaseUser.getUid());
                } else {

                    // User is signed out
                    _authenticated = false;
                    Log.d("AUTH", "onAuthStateChanged:signed_out");
                }
            }
        });

    }

    public String getUserUid(){
        //stop people from getting the Uid if not logged in
        if(firebaseUser == null)
            return "";
        else
            return firebaseUser.getUid();
    }

    public String getGameID(){
        return _gameId;
    }
    public void setGameID(String gameId) {_gameId = gameId;}
}

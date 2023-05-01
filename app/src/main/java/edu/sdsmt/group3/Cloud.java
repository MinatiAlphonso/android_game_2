package edu.sdsmt.group3;

import com.google.firebase.database.DatabaseReference;

public class Cloud {
    private DatabaseReference gamesList;
    public Cloud(DatabaseReference game) {
        gamesList = game;
    };

    public void saveToCloud(Game game){
        game.saveJSON(gamesList);
    }

}

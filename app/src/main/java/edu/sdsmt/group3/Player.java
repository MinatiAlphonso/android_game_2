package edu.sdsmt.group3;

import android.os.Bundle;

import java.io.Serializable;

public class Player {

    private Parameters params;

    public Player() {
        params = new Parameters();
    }

    public String getName() {

        return params.name;
    }

    public void setName(String name) {
        params.name = name;
    }

    public int getScore() {
        return params.score;
    }

    public void setScore(int score){
        params.score = score;
    }

    /**
     * increases the player's score by a given amount
     * @param points: int - the amount by which to increase the player's score
     */
    public void scored(int points) {
        params.score += points;
    }

    public void setID(String id){
        params.ID = id;
    }

    public String getID(){
        return params.ID;
    }

    public static class Parameters implements Serializable {
        private int score = 0;
        private String name = null;
        private String ID = null;
    }
    // Serialize and store parameters in bundle
    public void savePlayer(String key, Bundle bundle) {
        bundle.putSerializable(key, params);
    }
    // Load serializable
    public void restorePlayer(String key, Bundle bundle) {
        params = (Parameters)bundle.getSerializable(key);
    }
}

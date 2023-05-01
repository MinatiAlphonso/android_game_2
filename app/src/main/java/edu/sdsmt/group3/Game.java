package edu.sdsmt.group3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Game {
    private Bitmap backgroundBitmap;
    private GameView gameView;
    private float imageScale;
    private final static float SCALE_IN_VIEW = 1.0f;
    private float marginLeft;
    private float marginTop;
    private ArrayList<HashMap<String, Float>> listCollectiblesDB;

    // Public variables to reference
    public static final int GAME_OVER = -1;
    public static final int GAME_PLAYING = 0;
    public static final int RECTANGLE_CAPTURE = 0;
    public static final int CIRCLE_CAPTURE = 1;
    public static final int LINE_CAPTURE = 2;

    //public Context savedContext = null;

    // class to serialize parameters
    private static class Parameters implements Serializable {
        // added rounds for now. Might not be needed
        public int rounds = 0;
        public int remainingRounds = 0;
        public int turn = 1;
        public int capture = -1;//default is no capture option selected
        public float x = 0;
        public float y = 0;
        public float angle = 0;
        public float scale = 0.25f;
        public int collectibles = 15;
        public boolean gameEnded = false;
    }
    private Parameters params;
    private Player player1;
    private Player player2;
    private Random random;

    private Capture selectedCapture = null;//default is no capture option selected
    // finish Capture in game class
    private final Capture circleCapture;
    private final Capture rectangleCapture;
    private final Capture lineCapture;
    private ArrayList<Collectible> collectibles;

    // for next turn
    String _currentTurn;

    GameActivity _gameActivity;

    DatabaseReference gameRef;

    // add collectibles here
    private static final String GAME_PARAMS = "edu.sdsmt.group3.GAME_PARAMS";
    private static final String PLAYER1_PARAMS = "edu.sdsmt.group3.PLAYER1_PARAMS";
    private static final String PLAYER2_PARAMS = "edu.sdsmt.group3.PLAYER2_PARAMS";

    private static final String COLLECTIBLE_PARAMS = "edu.sdsmt.group3.captureparams";
    private static final String DB_COLLECTIBLE_PARAMS = "edu.sdsmt.group3.DB_COLLECTIBLE_PARAMS";
    private static final String DB_COLLECTIBLE_X = "edu.sdsmt.group3.DB_COLLECTIBLE_X";
    private static final String DB_COLLECTIBLE_Y = "edu.sdsmt.group3.DB_COLLECTIBLE_Y";

    // constructor
    // context used to pass to collectibles and captures
    public Game(Context context) {
        params = new Parameters();
        player1 = new Player();
        player2 = new Player();
        random = new Random();
        listCollectiblesDB = new ArrayList<>();
        // should either 1 or 2
        params.turn = 1;

        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.space);
        collectibles = new ArrayList<>();

        // initialization for captures and collectibles.
        rectangleCapture = new RectangleCapture(context, R.drawable.rectangle_concentric);
        circleCapture = new CircleCapture(context, R.drawable.circle_concentric);
        lineCapture = new LineCapture(context, R.drawable.line);

        circleCapture.setScalable(false);
        circleCapture.setScale(0.25f);
        lineCapture.setScalable(false);
        lineCapture.setScale(0.50f);

        addCollectibleToList(context);
        //savedContext = context;
        shuffle();
        setListCollectiblesDB();
    }

    public void addCollectibleToList(Context context){
        for(int i = 0; i < params.collectibles; i++) {
            // added background image dimensions so that the get x and y functions can return absolute location for collisions
            collectibles.add(new Collectible(context, R.drawable.collectible, backgroundBitmap.getWidth(), backgroundBitmap.getHeight()));
        }
    }
    public  void shuffle(){
        for(Collectible collect : collectibles){
            collect.shuffle(random);
        }
    }

    public void setGameRef(DatabaseReference ref) {
        gameRef = ref;
    }

    public int getGameState() {

        // check if collectibles are all captured
        if (params.remainingRounds <= 0 || allCollectiblesCaptured()) {
            return GAME_OVER;
        }
        else {
            return GAME_PLAYING;
        }
    }

    public void setGameToEnd() {
        params.remainingRounds = 0;
        params.gameEnded = true;
    }

    public boolean setGameEnded(){
        if(getGameState() == GAME_OVER){
            params.gameEnded = true;
        }
        else{
            params.gameEnded = false;
        }

        return params.gameEnded;
    }

    public int getPlayerTurn() {
        Integer turnNumber;
        if (_currentTurn.equals(getPlayer1().getID()))
            turnNumber = 1;
        else
            turnNumber = 2;

        return turnNumber;
    }

    public void setRounds(int rounds) {
        params.rounds = rounds;
        params.remainingRounds = rounds;
    }

    public int getTotalRounds(){
        return params.rounds;
    }

    public int getRound() {
        return params.rounds - params.remainingRounds + 1;
    }

    public void setPlayersNames(String player1Name, String player2Name) {
        player1.setName(player1Name);
        player2.setName(player2Name);
    }

    public void setPlayersIDs(String p1ID, String p2ID){
        player1.setID(p1ID);
        player2.setID(p2ID);
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Capture getCapture() {
        return selectedCapture;
    }

    public void setTurn(String turn) {_currentTurn = turn;}

    public void setCurrentRound(Integer round) {params.remainingRounds = round;}

    /**
     * This function sets the capture option for the current turn.
     * -1 means that capture option has not been selected.
     * 0 refers to the rectangle capture
     * 1 refers to the circle capture
     * 2 refers to the line capture
     * @param capture
     */
    public void setCapture(int capture) {
        if (capture >= -1 && capture < CaptureActivity.CaptureType.values().length) {
            params.capture = capture;
            Boolean needsReset = true;
            float tempX = 0, tempY = 0, tempAngle = 0;
            if(selectedCapture != null){
                needsReset = false;
                tempX = selectedCapture.getX();
                tempY = selectedCapture.getY();
                tempAngle = selectedCapture.getAngle();
            }
            switch (params.capture) {
                case RECTANGLE_CAPTURE:
                    selectedCapture = rectangleCapture;
                    if(needsReset){selectedCapture.setScale(0.25f);}
                    break;

                case CIRCLE_CAPTURE:
                    selectedCapture = circleCapture;
                    break;

                case LINE_CAPTURE:
                    selectedCapture = lineCapture;
                    break;

                default:
                    selectedCapture = null;
            }
            if(selectedCapture != null){
                selectedCapture.setX(tempX);
                selectedCapture.setY(tempY);
                selectedCapture.setAngle(tempAngle);
                if(needsReset){
                    selectedCapture.setX(0);
                    selectedCapture.setY(0);
                    selectedCapture.setAngle(tempAngle);
                }
            }

        }
    }

    public ArrayList<Collectible> getCollectibles() {
        return collectibles;
    }

    /**
     * This function determines whether each collectible in collectibles
     * should be collected based on the current capture shape's probability
     * of collection. It then removes them from the list
     */
    public void captureCollectibles() {
        ArrayList<Collectible> capturedCollectibles = new ArrayList<>();
        // for every collectible in collectibles
        for (int colIndex = 0; colIndex < getCollectibles().size(); colIndex++) {
            // if the collectible overlaps with the capture shape
            Collectible col = getCollectibles().get(colIndex);
            if (getCapture().collisionTest(col)
                    && random.nextFloat() < getCapture().getChance()) {
                // add it to the list of overlapping collectibles, given the collection chance
                capturedCollectibles.add(col);
            }
        }
        // remove the captured collectibles from collectibles
        getCollectibles().removeAll(capturedCollectibles);
        // increase the current player's score by the number of collectibles captured
        getCurrentPlayer().scored(capturedCollectibles.size());
    }

    /**
     * Returns the player whose turn it is
     * */
    private Player getCurrentPlayer() {
        Player currentPlayer;
        if (getPlayerTurn() == 1) {
            currentPlayer = getPlayer1();
        } else {
            currentPlayer = getPlayer2();
        }
        return currentPlayer;
    }

    private void setNextTurnID() {
        if (_currentTurn.equals(getPlayer1().getID())){
            _currentTurn = getPlayer2().getID();
        } else {
            _currentTurn = getPlayer1().getID();
        }
    }

    /**
     * Advances to the next players turn, unless both players have already
     * played, in which case the next round will begin
     */
    public void advanceTurn() {
//        params.turn++;
//
//        // try to add logic in here next
//        if (params.turn > 2) {
//            advanceRound();
//        }

        if (_currentTurn.equals(getPlayer2().getID())) {
            advanceRound();
        }

        setNextTurnID();
    }

    /**
     * Begins the next round
     */
    public void advanceRound() {
        params.remainingRounds--;
        params.turn = 1;
    }

    private boolean allCollectiblesCaptured() {
        for (Collectible collect : collectibles) {
            if (!collect.isCaptured()) {
                return false;
            }
        }
        return true;
    }

    public void draw(Canvas canvas) {
        /**
         * Calculations
         */

        float wid = canvas.getWidth();
        float hit = canvas.getHeight();

        // What would be the scale to draw the where it fits both
        // horizontally and vertically?
        float scaleH = wid / backgroundBitmap.getWidth();
        float scaleV = hit / backgroundBitmap.getHeight();

        // Use the lesser of the two
        imageScale = scaleH < scaleV ? scaleH : scaleV;
        imageScale *= SCALE_IN_VIEW;

        // What is the scaled image size?
        float iWid = imageScale * backgroundBitmap.getWidth();
        float iHit = imageScale * backgroundBitmap.getHeight();

        // Determine the top and left margins to center
        marginLeft = (wid - iWid) / 2.0f;
        marginTop = (hit - iHit) / 2.0f;

        // set the collectibles so that the get x and y functions return absolute position for collision bounds
        for (Collectible collect : collectibles) {
            collect.setImageScale(imageScale);
        }


        /**
         * Draw the Space Background
         */
        if(backgroundBitmap != null) {
            drawBackground(canvas,marginLeft,marginTop,imageScale);
        }

        /**
         * Draw Collectibles
         * */
        Log.i("Status","BeforeDrawing");
        Log.i("x (collectible)", String.valueOf(collectibles.get(0).getX()));
        Log.i("y (collectible)", String.valueOf(collectibles.get(0).getY()));
        for(Collectible collect : collectibles){
            if(collect != null){
                collect.draw(canvas, marginLeft, marginTop, imageScale, backgroundBitmap.getWidth(), backgroundBitmap.getHeight());
            }
        }

        /**
         * Drawing the Capture Option
         * */

        if(selectedCapture != null){
            selectedCapture.draw(canvas, marginLeft, marginTop, imageScale);
        }

    }

    public void drawBackground(Canvas canvas, float marginLeft, float marginTop, float imageScale){
        canvas.save();
        canvas.translate(marginLeft,  marginTop);
        canvas.scale(imageScale, imageScale);
        canvas.drawBitmap(backgroundBitmap, 0,0, null);
        canvas.restore();
    }

    public boolean onTouchEvent(View gameView, MotionEvent event) {
        if(getCapture() != null) {
            return selectedCapture.onTouchEvent(gameView, event, marginLeft, marginTop, imageScale);
        }
        return false;
    }

    public void setGameView(GameView gameView) {
        this.gameView = gameView;
    }

    public void saveGame(){
        Cloud cloud = new Cloud(gameRef);
        cloud.saveToCloud(this);
    }

    public void setListCollectiblesDB(){
        for(Collectible collect : collectibles) {
            if(collect != null){
                HashMap<String, Float> locations = new HashMap<>();
                locations.put("x", collect.getX());
                locations.put("y", collect.getY());
                listCollectiblesDB.add(locations);
            }
        }
    }

    public ArrayList<HashMap<String, Float>> getListCollectiblesDB(){
        return listCollectiblesDB;
    }

    public void saveJSON(DatabaseReference snapshot){
        snapshot.child("GameEnded").setValue(setGameEnded());
        snapshot.child("p1id").setValue(player1.getID());
        snapshot.child("p1Name").setValue(player1.getName());
        snapshot.child("p1score").setValue(player1.getScore());
        snapshot.child("p2id").setValue(player2.getID());
        snapshot.child("p2Name").setValue(player2.getName());
        snapshot.child("p2score").setValue(player2.getScore());
        snapshot.child("collectibles").setValue(listCollectiblesDB);
        snapshot.child("round").setValue(getRound());
        snapshot.child("turnId").setValue(_currentTurn);
    }

    public void updateCollectibleFromDB(){

        collectibles.clear();
        for(HashMap<String, Float> collect : listCollectiblesDB) {
            // added background image dimensions so that the get x and y functions can return absolute location for collisions
            Collectible c = new Collectible(gameView.getContext(), R.drawable.collectible, backgroundBitmap.getWidth(), backgroundBitmap.getHeight());
            collectibles.add(c);
            c.setXAndY(collect.get("x"), collect.get("y"));
        }
        Log.i("Status","UpdateCollectibleFromDB");
        Log.i("x (collectible)", String.valueOf(collectibles.get(0).getX()));
        Log.i("y (collectible)", String.valueOf(collectibles.get(0).getY()));
    }

    public void loadJSON(DataSnapshot snapshot){
        params.gameEnded = (boolean) (snapshot.child("GameEnded").getValue());
        player1.setID(snapshot.child("p1id").getValue().toString());
        player1.setName(snapshot.child("p1Name").getValue().toString());
        player1.setScore(Integer.parseInt(snapshot.child("p1score").getValue().toString()));
        player2.setID(snapshot.child("p2id").getValue().toString());
        player2.setName(snapshot.child("p2Name").getValue().toString());
        player2.setScore(Integer.parseInt(snapshot.child("p2score").getValue().toString()));

        listCollectiblesDB.clear();
        for (DataSnapshot child : snapshot.child("collectibles").getChildren()) {

            if(child != null){
                HashMap<String, Float> locations = new HashMap<>();
                locations.put("x", Float.valueOf(child.child("x").getValue().toString()));
                locations.put("y", Float.valueOf(child.child("y").getValue().toString()));
                listCollectiblesDB.add(locations);
            }
        }

        params.remainingRounds = 1 + params.rounds - Integer.parseInt(snapshot.child("round").getValue().toString());

        // added this, the below commented out code was changing the player id for some reason
        _currentTurn = snapshot.child("turnId").getValue().toString();
        //getCurrentPlayer().setID(snapshot.child("turnId").getValue().toString());

        updateTheView();
    }

    private void updateTheView() {
        setGameEnded();
        _gameActivity.checkIfGameOver();
        _gameActivity.updateUI();
    }

    public void saveGameState(Bundle bundle) {
        if(selectedCapture!=null){
            params.x = selectedCapture.getX();
            params.y = selectedCapture.getY();
            params.angle = selectedCapture.getAngle();
            params.scale = selectedCapture.getScale();
        }
        params.collectibles = collectibles.size();
        bundle.putSerializable(GAME_PARAMS, params);
        player1.savePlayer(PLAYER1_PARAMS, bundle);
        player2.savePlayer(PLAYER2_PARAMS, bundle);
        int i = 0;
        for (Collectible collect : collectibles) {
            collect.saveCollectibleState(COLLECTIBLE_PARAMS + i, bundle);
            if(i == 0) {
                Log.i("Status", "SaveGameState");
                Log.i("x (collectibles)", String.valueOf(collect.getX()));
                Log.i("y (collectibles)", String.valueOf(collect.getY()));
            }
            i++;
        }
        saveGame();
        Log.i("x (listCollectiblesDB)", String.valueOf(listCollectiblesDB.get(0).get("x")));
        Log.i("y (listCollectiblesDB)", String.valueOf(listCollectiblesDB.get(0).get("y")));
    }

    public void restoreGameState(Bundle bundle) {
        params = (Parameters)bundle.getSerializable(GAME_PARAMS);
        player1.restorePlayer(PLAYER1_PARAMS, bundle);
        player2.restorePlayer(PLAYER2_PARAMS, bundle);
        setCapture(params.capture);
        if(selectedCapture!=null){
            selectedCapture.setX(params.x);
            selectedCapture.setY(params.y);
            selectedCapture.setAngle(params.angle);
            selectedCapture.setScale(params.scale);
        }
        collectibles.clear();
        addCollectibleToList(gameView.getContext());
        int i = 0;
        for (Collectible collect : collectibles) {
            collect.loadCollectibleState(COLLECTIBLE_PARAMS + i, bundle);
            if(i == 0) {
                Log.i("Status", "RestoreGameState");
                Log.i("x (collectibles)", String.valueOf(collect.getX()));
                Log.i("y (collectibles)", String.valueOf(collect.getY()));
            }
            i++;
        }
        listCollectiblesDB.clear();
        setListCollectiblesDB();
        Log.i("x (listCollectiblesDB)", String.valueOf(listCollectiblesDB.get(0).get("x")));
        Log.i("y (listCollectiblesDB)", String.valueOf(listCollectiblesDB.get(0).get("y")));
    }


    public void setGameActivity(GameActivity activity) {
        _gameActivity = activity;
    }
}
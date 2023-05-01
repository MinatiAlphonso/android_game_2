package edu.sdsmt.group3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;

import java.io.Serializable;
import java.util.Random;

public class Collectible {

    private Bitmap collectBitmap;
    private Rect rect;
    private Parameters params;

    public Collectible(Context context, int id, int width, int height) {
        collectBitmap = BitmapFactory.decodeResource(context.getResources(), id);
        rect = new Rect();
        params = new Parameters();
        params.backgroundHeight = height;
        params.backgroundWidth = width;
        setRect();
    }

    private void setRect() {
        rect.set((int)(params.x * params.backgroundHeight * params.imageScale - params.imageScale * collectBitmap.getWidth() * params.scale / 2),
                (int)(params.y * params.backgroundWidth * params.imageScale - params.imageScale * collectBitmap.getHeight() * params.scale / 2),
                (int)(params.x * params.backgroundHeight * params.imageScale) + (int)(params.imageScale * collectBitmap.getWidth() * params.scale / 2),
                (int)(params.y  * params.backgroundWidth * params.imageScale) + (int)(params.imageScale * collectBitmap.getHeight() * params.scale / 2));
    }

    public Bitmap getCollectBitmap() {
        return collectBitmap;
    }

    public float getXscaled() {
        return params.x * params.backgroundHeight * params.imageScale - params.imageScale * collectBitmap.getWidth() * params.scale / 2;
    }

    public float getYscaled() {
        return params.y * params.backgroundWidth * params.imageScale - params.imageScale * collectBitmap.getHeight() * params.scale / 2;
    }

    public float getX(){
        return params.x;
    }
    public float getY(){
        return params.y;
    }

    public Rect getRect() {
        return rect;
    }

    public void setImageScale(float scale) {
        params.imageScale = scale;
        setRect();
    }

    public float getScale() {
        return params.scale;
    }

    public boolean isCaptured() {
        return params.captured;
    }


    public void draw(Canvas canvas, float marginLeft, float marginTop, float imageScale, float imgWid, float imgHit) {
        /**
         * Draw Collectible
         */
        canvas.save();
        canvas.translate(marginLeft + (params.x * imgWid * imageScale), marginTop + (params.y * imgHit * imageScale));
        canvas.scale(imageScale*params.scale, imageScale*params.scale);
        canvas.translate(-collectBitmap.getWidth() / 2.0f, -collectBitmap.getHeight() / 2.0f);
        canvas.drawBitmap(collectBitmap, 0, 0, null);
        canvas.restore();
    }

    public void shuffle(Random rand) {
        params.x = rand.nextFloat();
        params.y = rand.nextFloat();
        setRect();
    }

    public void setXAndY(float x, float y) {
        params.x = x;
        params.y = y;
        setRect();
    }

    static class Parameters implements Serializable {
        public float x = 0;
        public float y = 0;
        public float scale = 0.15f;
        public boolean captured = false;
        public int backgroundHeight = 0;
        public int backgroundWidth = 0;
        public float imageScale = 0;
    }

    public void saveCollectibleState(String key, Bundle bundle) {
        bundle.putSerializable(key, params);
    }

    public void loadCollectibleState(String key, Bundle bundle) {
        params = (Parameters)bundle.getSerializable(key);
        setRect();
    }
}

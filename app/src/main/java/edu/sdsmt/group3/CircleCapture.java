package edu.sdsmt.group3;

import android.content.Context;

public class CircleCapture extends Capture{

    public static final float CAPTURE_CHANCE = 1.0f;

    public CircleCapture(Context context, int id) {
        super(context, id);
        setScale(0.25f);
        setChance(CAPTURE_CHANCE);
    }
}

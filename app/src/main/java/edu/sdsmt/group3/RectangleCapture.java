package edu.sdsmt.group3;

import android.content.Context;

public class RectangleCapture extends Capture {

    public static final float CAPTURE_CHANCE = 1.0f;

    public RectangleCapture(Context context, int id) {
        super(context, id);
        setChance(CAPTURE_CHANCE);
        setScalable(true);
        setScale(0.5f);
    }

    @Override
    public float getChance() {
        return 1.0f - super.getScale();
    }
}

package edu.sdsmt.group3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CaptureActivity extends AppCompatActivity {

    public enum CaptureType {
        RECTANGLE,
        CIRCLE,
        LINE
    }

    private int captureType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
    }

    public void onRectClick(View view) {
        captureType = CaptureType.RECTANGLE.ordinal();
        close();
    }

    public void onCircleClick(View view) {
        captureType = CaptureType.CIRCLE.ordinal();
        close();
    }

    public void onLineClick(View view) {
        captureType = CaptureType.LINE.ordinal();
        close();
    }

    private void close() {
        Intent intent = new Intent();
        intent.putExtra(GameActivity.RETURN_CAPTURE_MESSAGE, captureType);
        setResult(RESULT_OK, intent);
        finish();
    }

}
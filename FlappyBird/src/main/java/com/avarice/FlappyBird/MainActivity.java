package com.avarice.FlappyBird;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.avarice.app.R;

public class MainActivity extends Activity {

    final int REQUEST_CODE = 111;
    public SharedPreferences bestRecord;
    TextView textViewBestRecord;
    String keyRecord = "bestRecord";

    @SuppressLint({"SetTextI18n", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        textViewBestRecord = findViewById(R.id.textViewBestRecord);
        bestRecord = getSharedPreferences(keyRecord, MODE_PRIVATE);

        textViewBestRecord.setText("Best record: " + bestRecord.getInt(keyRecord, 0));
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, StartGame.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int oldRecord = bestRecord.getInt(keyRecord, 0);
            int newRecord = data.getIntExtra("newRecord", 0);

            if (newRecord > oldRecord) {
                SharedPreferences.Editor editRecord = bestRecord.edit();
                editRecord.putInt(keyRecord, newRecord);
                editRecord.apply();
                textViewBestRecord.setText("Best record: " + newRecord);
            }
        }
    }
}
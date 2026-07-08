package com.example.droidcraft;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView timerText;
    private Button btnStartTimer, btnColorPicker;
    private CountDownTimer countDownTimer;
    private MediaPlayer clickSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerText = findViewById(R.id.timerText);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnColorPicker = findViewById(R.id.btnColorPicker);
        
        clickSound = MediaPlayer.create(this, R.raw.click_effect);

        btnStartTimer.setOnClickListener(v -> {
            playSound();
            if (countDownTimer != null) countDownTimer.cancel();
            
            countDownTimer = new CountDownTimer(30000, 1000) {
                public void onTick(long millisUntilFinished) {
                    timerText.setText("Seconds remaining: " + millisUntilFinished / 1000);
                }
                public void onFinish() {
                    timerText.setText("Time's up!");
                }
            }.start();
        });

        btnColorPicker.setOnClickListener(v -> {
            playSound();
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            timerText.setTextColor(color);
            btnStartTimer.setBackgroundColor(color);
        });
    }

    private void playSound() {
        if (clickSound != null) {
            clickSound.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (clickSound != null) clickSound.release();
    }
}
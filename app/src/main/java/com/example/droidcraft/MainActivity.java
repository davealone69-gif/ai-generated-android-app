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
    private Button btnStart, btnColor;
    private CountDownTimer countDownTimer;
    private MediaPlayer clickSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerText = findViewById(R.id.timerText);
        btnStart = findViewById(R.id.btnStart);
        btnColor = findViewById(R.id.btnColor);

        clickSound = MediaPlayer.create(this, R.raw.click_effect);

        btnStart.setOnClickListener(v -> {
            playSound();
            startCountdown(60000);
        });

        btnColor.setOnClickListener(v -> {
            playSound();
            changeBackgroundColor();
        });
    }

    private void startCountdown(long duration) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time: " + millisUntilFinished / 1000);
            }
            public void onFinish() {
                timerText.setText("Done!");
            }
        }.start();
    }

    private void changeBackgroundColor() {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        findViewById(android.R.id.content).setBackgroundColor(color);
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
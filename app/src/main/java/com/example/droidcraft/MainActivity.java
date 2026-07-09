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
    private TextView timerText, statusLabel;
    private Button btnStart, btnColor;
    private CountDownTimer countDownTimer;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerText = findViewById(R.id.timerText);
        statusLabel = findViewById(R.id.statusLabel);
        btnStart = findViewById(R.id.btnStart);
        btnColor = findViewById(R.id.btnColor);

        mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);

        btnStart.setOnClickListener(v -> {
            playSound();
            startCountdown(30000);
        });

        btnColor.setOnClickListener(v -> {
            playSound();
            int color = Color.rgb(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));
            timerText.setTextColor(color);
            statusLabel.setText("Theme Color Updated!");
        });
    }

    private void startCountdown(long millis) {
        if (countDownTimer != null) countDownTimer.cancel();
        
        countDownTimer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time: " + millisUntilFinished / 1000 + "s");
            }
            public void onFinish() {
                timerText.setText("Time: 0s");
                statusLabel.setText("System Ready!");
            }
        }.start();
    }

    private void playSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
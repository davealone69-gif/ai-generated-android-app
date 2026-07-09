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
            if (countDownTimer != null) countDownTimer.cancel();
            
            countDownTimer = new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    timerText.setText("Time: " + millisUntilFinished / 1000);
                }
                public void onFinish() {
                    timerText.setText("Task Complete!");
                }
            }.start();
        });

        btnColor.setOnClickListener(v -> {
            playSound();
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            statusLabel.setTextColor(color);
            statusLabel.setText("System Color Updated");
        });
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
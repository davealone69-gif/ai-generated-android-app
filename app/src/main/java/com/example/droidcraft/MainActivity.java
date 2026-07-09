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
    private TextView timerText, statusText;
    private Button btnStart, btnChangeColor;
    private CountDownTimer countDownTimer;
    private MediaPlayer clickSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerText = findViewById(R.id.timerText);
        statusText = findViewById(R.id.statusText);
        btnStart = findViewById(R.id.btnStart);
        btnChangeColor = findViewById(R.id.btnChangeColor);

        clickSound = MediaPlayer.create(this, R.raw.click_effect);

        btnStart.setOnClickListener(v -> {
            playSound();
            startTimer();
        });

        btnChangeColor.setOnClickListener(v -> {
            playSound();
            int color = Color.rgb(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));
            timerText.setTextColor(color);
            statusText.setText("Color Cycle Engaged");
        });
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        
        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("T-Minus: " + millisUntilFinished / 1000);
            }
            public void onFinish() {
                timerText.setText("System Active!");
                statusText.setText("Ready for Operation");
            }
        }.start();
    }

    private void playSound() {
        if (clickSound != null) {
            clickSound.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clickSound != null) clickSound.release();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
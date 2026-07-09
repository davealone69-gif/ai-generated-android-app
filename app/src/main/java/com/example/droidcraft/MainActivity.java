package com.example.droidcraft;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView timerText;
    private Button btnStartTimer;
    private Button btnColorPicker;
    private CountDownTimer countDownTimer;
    private MediaPlayer clickSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerText = findViewById(R.id.timerText);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnColorPicker = findViewById(R.id.btnColorPicker);

        clickSound = MediaPlayer.create(this, R.raw.click_sound);

        btnStartTimer.setOnClickListener(v -> {
            playSound();
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            countDownTimer = new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    timerText.setText("Seconds remaining: " + millisUntilFinished / 1000);
                }
                public void onFinish() {
                    timerText.setText("Timer Finished!");
                }
            }.start();
        });

        btnColorPicker.setOnClickListener(v -> {
            playSound();
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            timerText.setTextColor(color);
            Toast.makeText(MainActivity.this, "Theme Color Shifted!", Toast.LENGTH_SHORT).show();
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
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (clickSound != null) {
            clickSound.release();
        }
    }
}
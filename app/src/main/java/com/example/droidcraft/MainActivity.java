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
    private Button btnStart, btnChangeColor;
    private CountDownTimer countDownTimer;
    private MediaPlayer beepSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerText = findViewById(R.id.timerText);
        btnStart = findViewById(R.id.btnStart);
        btnChangeColor = findViewById(R.id.btnChangeColor);
        
        beepSound = MediaPlayer.create(this, android.R.raw.click);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSound();
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                
                countDownTimer = new CountDownTimer(30000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timerText.setText("Time: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        timerText.setText("Time's Up!");
                    }
                }.start();
            }
        });

        btnChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSound();
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                timerText.setTextColor(color);
                findViewById(R.id.mainLayout).setBackgroundColor(color & 0x40FFFFFF);
            }
        });
    }

    private void playClickSound() {
        if (beepSound != null) {
            beepSound.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (beepSound != null) {
            beepSound.release();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
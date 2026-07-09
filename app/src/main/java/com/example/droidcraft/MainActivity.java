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
    private TextView tvTimer;
    private Button btnStart, btnColor;
    private CountDownTimer countDownTimer;
    private MediaPlayer clickSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTimer = findViewById(R.id.tvTimer);
        btnStart = findViewById(R.id.btnStart);
        btnColor = findViewById(R.id.btnColor);

        clickSound = MediaPlayer.create(this, R.raw.click_sound);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound();
                if (countDownTimer != null) countDownTimer.cancel();
                
                countDownTimer = new CountDownTimer(30000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        tvTimer.setText("Time: " + millisUntilFinished / 1000);
                    }
                    public void onFinish() {
                        tvTimer.setText("Done!");
                    }
                }.start();
            }
        });

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound();
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                tvTimer.setTextColor(color);
            }
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
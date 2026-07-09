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
    private Button btnStartTimer, btnChangeColor;
    private CountDownTimer countDownTimer;
    private MediaPlayer beepPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerText = findViewById(R.id.timerText);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnChangeColor = findViewById(R.id.btnChangeColor);
        
        beepPlayer = MediaPlayer.create(this, R.raw.beep_sound);

        btnStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                
                countDownTimer = new CountDownTimer(10000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timerText.setText("Seconds remaining: " + millisUntilFinished / 1000);
                        if (beepPlayer != null) beepPlayer.start();
                    }

                    public void onFinish() {
                        timerText.setText("Timer Finished!");
                    }
                }.start();
            }
        });

        btnChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                timerText.setTextColor(color);
                findViewById(R.id.mainLayout).setBackgroundColor(Color.argb(50, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (beepPlayer != null) {
            beepPlayer.release();
            beepPlayer = null;
        }
    }
}
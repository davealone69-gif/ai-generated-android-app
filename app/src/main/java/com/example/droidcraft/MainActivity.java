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
    private TextView txtTimer;
    private Button btnStartTimer, btnChangeColor;
    private CountDownTimer countDownTimer;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTimer = findViewById(R.id.txtTimer);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnChangeColor = findViewById(R.id.btnChangeColor);

        mediaPlayer = MediaPlayer.create(this, R.raw.beep_sound);

        btnStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound();
                if (countDownTimer != null) countDownTimer.cancel();
                
                countDownTimer = new CountDownTimer(10000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        txtTimer.setText("Time: " + millisUntilFinished / 1000);
                    }
                    public void onFinish() {
                        txtTimer.setText("Done!");
                        playSound();
                    }
                }.start();
            }
        });

        btnChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                txtTimer.setTextColor(color);
                Toast.makeText(MainActivity.this, "Color Shifted!", Toast.LENGTH_SHORT).show();
            }
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
            mediaPlayer = null;
        }
    }
}
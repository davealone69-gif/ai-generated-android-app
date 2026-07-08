package com.example.droidcraft;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView timerText;
    private Button btnStart, btnColor;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerText = findViewById(R.id.countdownDisplay);
        btnStart = findViewById(R.id.btnStartTimer);
        btnColor = findViewById(R.id.btnColorPicker);

        btnStart.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            countDownTimer = new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    timerText.setText(String.format("00:%02d", millisUntilFinished / 1000));
                }
                public void onFinish() {
                    timerText.setText("READY!");
                }
            }.start();
        });

        btnColor.setOnClickListener(v -> {
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            timerText.setTextColor(color);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
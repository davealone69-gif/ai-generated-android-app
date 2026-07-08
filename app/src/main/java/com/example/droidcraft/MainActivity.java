package com.example.droidcraft;

import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private AudioManager audioManager;

    public static class AudioManager {
        private final SoundPool soundPool;
        private final int clickSoundId;

        public AudioManager(MainActivity activity) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            this.soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(attrs).build();
            this.clickSoundId = soundPool.load(activity, R.raw.click_sound, 1);
        }

        public void playClick() {
            soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
        }

        public void release() {
            soundPool.release();
        }
    }

    public static class MainViewModel extends ViewModel {
        private final MutableLiveData<String> timerDisplay = new MutableLiveData<>("Ready");
        private final MutableLiveData<Integer> textColor = new MutableLiveData<>(Color.DKGRAY);
        private CountDownTimer timer;
        private long timeLeft = 10000;

        public LiveData<String> getTimerDisplay() { return timerDisplay; }
        public LiveData<Integer> getTextColor() { return textColor; }

        public void startTimer() {
            if (timer != null) timer.cancel();
            timer = new CountDownTimer(timeLeft, 1000) {
                public void onTick(long millis) {
                    timeLeft = millis;
                    timerDisplay.setValue("Time: " + (millis / 1000));
                }
                public void onFinish() {
                    timeLeft = 0;
                    timerDisplay.setValue("Complete!");
                }
            }.start();
        }

        public void randomizeColor() {
            Random rnd = new Random();
            int color;
            do {
                color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            } while (ColorUtils.calculateLuminance(color) < 0.2 || ColorUtils.calculateLuminance(color) > 0.8);
            textColor.setValue(color);
        }

        @Override
        protected void onCleared() {
            if (timer != null) timer.cancel();
            super.onCleared();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        audioManager = new AudioManager(this);

        MaterialTextView timerText = findViewById(R.id.timerText);
        MaterialButton btnStart = findViewById(R.id.btnStartTimer);
        MaterialButton btnColor = findViewById(R.id.btnChangeColor);

        viewModel.getTimerDisplay().observe(this, timerText::setText);
        viewModel.getTextColor().observe(this, timerText::setTextColor);

        btnStart.setOnClickListener(v -> {
            audioManager.playClick();
            viewModel.startTimer();
        });

        btnColor.setOnClickListener(v -> {
            audioManager.playClick();
            viewModel.randomizeColor();
        });
    }

    @Override
    protected void onDestroy() {
        audioManager.release();
        super.onDestroy();
    }
}
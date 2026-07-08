package com.example.droidcraft;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import androidx.activity.viewModels;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textview.MaterialTextView;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private SoundPool soundPool;
    private int clickSoundId;

    public static class MainViewModel extends ViewModel {
        private final MutableLiveData<String> timerDisplay = new MutableLiveData<>("Press Start");
        private final MutableLiveData<Integer> textColor = new MutableLiveData<>(0xFF000000);
        private android.os.CountDownTimer timer;

        public LiveData<String> getTimerDisplay() { return timerDisplay; }
        public LiveData<Integer> getTextColor() { return textColor; }

        public void startTimer() {
            if (timer != null) timer.cancel();
            timer = new android.os.CountDownTimer(10000, 1000) {
                public void onTick(long millis) { timerDisplay.setValue("Time: " + millis / 1000); }
                public void onFinish() { timerDisplay.setValue("Complete!"); }
            }.start();
        }

        public void randomizeColor() {
            Random rnd = new Random();
            int color = 0xFF000000 | rnd.nextInt(0xFFFFFF);
            textColor.setValue(color);
        }

        @Override
        protected void onCleared() {
            super.onCleared();
            if (timer != null) timer.cancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(MainViewModel.class);
        setupAudio();

        MaterialTextView timerText = findViewById(R.id.timerText);
        MaterialButton btnStart = findViewById(R.id.btnStartTimer);
        MaterialButton btnColor = findViewById(R.id.btnChangeColor);

        viewModel.getTimerDisplay().observe(this, timerText::setText);
        viewModel.getTextColor().observe(this, timerText::setTextColor);

        btnStart.setOnClickListener(v -> {
            playSound();
            viewModel.startTimer();
        });

        btnColor.setOnClickListener(v -> {
            playSound();
            viewModel.randomizeColor();
        });
    }

    private void setupAudio() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        clickSoundId = soundPool.load(this, R.raw.click_sound, 1);
    }

    private void playSound() {
        soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
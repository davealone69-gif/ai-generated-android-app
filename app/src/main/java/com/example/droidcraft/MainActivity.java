package com.example.droidcraft;

import android.app.Application;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewModelScope;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import java.util.Locale;
import java.util.Random;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.delay;
import kotlinx.coroutines.launch;

class SoundEffectManager {
    private final SoundPool soundPool;
    private final int clickSoundId;

    public SoundEffectManager(Application app) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        this.soundPool = new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(attrs).build();
        // Assumes R.raw.click_sound exists in the resource folder
        this.clickSoundId = soundPool.load(app, R.raw.click_sound, 1);
    }

    public void playClick() {
        soundPool.play(clickSoundId, 0.7f, 0.7f, 1, 0, 1f);
    }

    public void release() {
        soundPool.release();
    }
}

class MainViewModel extends AndroidViewModel {
    private final SoundEffectManager soundManager;
    private final MutableLiveData<Long> remainingTime = new MutableLiveData<>(10000L);
    private final MutableLiveData<Integer> textColor = new MutableLiveData<>(0xFF000000);
    private Job timerJob;

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.soundManager = new SoundEffectManager(application);
    }

    public LiveData<Long> getRemainingTime() { return remainingTime; }
    public LiveData<Integer> getTextColor() { return textColor; }

    public void startTimer() {
        soundManager.playClick();
        if (timerJob != null) timerJob.cancel();

        timerJob = viewModelScope.launch {
            long time = remainingTime.getValue() != null ? remainingTime.getValue() : 10000L;
            while (time > 0) {
                delay(1000);
                time -= 1000;
                remainingTime.setValue(time);
            }
        }();
    }

    public void randomizeColor() {
        soundManager.playClick();
        float[] hsl = new float[]{new Random().nextFloat() * 360f, 0.7f, 0.5f};
        textColor.setValue(ColorUtils.HSLToColor(hsl));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        soundManager.release();
    }
}

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        MaterialTextView timerText = findViewById(R.id.timerText);
        MaterialButton btnStart = findViewById(R.id.btnStartTimer);
        MaterialButton btnColor = findViewById(R.id.btnChangeColor);

        viewModel.getRemainingTime().observe(this, millis -> {
            String text = millis > 0 ? String.format(Locale.getDefault(), "%ds", millis / 1000) : "Done!";
            timerText.setText(text);
        });

        viewModel.getTextColor().observe(this, color -> {
            timerText.animate()
                    .textColor(color)
                    .setDuration(400)
                    .start();
        });

        btnStart.setOnClickListener(v -> viewModel.startTimer());
        btnColor.setOnClickListener(v -> viewModel.randomizeColor());
    }
}
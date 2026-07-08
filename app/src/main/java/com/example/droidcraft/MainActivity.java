package com.example.droidcraft;

import android.app.Application;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import java.util.Random;

interface SoundEffectProvider {
    void playClick();
    void release();
}

class SoundEffectManager implements SoundEffectProvider {
    private final SoundPool soundPool;
    private int clickSoundId = -1;
    private boolean isLoaded = false;

    public SoundEffectManager(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        this.soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(attrs).build();
        this.soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) isLoaded = true;
        });
        this.clickSoundId = soundPool.load(context, R.raw.click_sound, 1);
    }

    @Override
    public void playClick() {
        if (isLoaded && clickSoundId != -1) {
            soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
        }
    }

    @Override
    public void release() {
        soundPool.release();
    }
}

class MainViewModel extends AndroidViewModel {
    private final MutableLiveData<String> timerDisplay = new MutableLiveData<>("10s");
    private final MutableLiveData<Integer> textColor = new MutableLiveData<>(0xFF333333);
    private final SoundEffectProvider audioManager;
    private CountDownTimer timer;
    private final long INITIAL_TIME = 10000;

    public MainViewModel(@NonNull Application application, SoundEffectProvider soundEffectProvider) {
        super(application);
        this.audioManager = soundEffectProvider;
    }

    public LiveData<String> getTimerDisplay() { return timerDisplay; }
    public LiveData<Integer> getTextColor() { return textColor; }

    public void startTimer() {
        audioManager.playClick();
        if (timer != null) timer.cancel();
        
        timer = new CountDownTimer(INITIAL_TIME, 1000) {
            @Override
            public void onTick(long millis) {
                timerDisplay.setValue((millis / 1000) + "s");
            }
            @Override
            public void onFinish() {
                timerDisplay.setValue("Done!");
            }
        }.start();
    }

    public void randomizeColor() {
        audioManager.playClick();
        Random random = new Random();
        float hue = random.nextFloat() * 360f;
        // Enforce a saturation and lightness that guarantees contrast against white/light gray backgrounds
        float[] hsl = new float[]{hue, 0.8f, 0.4f}; 
        textColor.setValue(ColorUtils.HSLToColor(hsl));
    }

    @Override
    protected void onCleared() {
        if (timer != null) timer.cancel();
        audioManager.release();
        super.onCleared();
    }
}

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SoundEffectProvider soundProvider = new SoundEffectManager(getApplicationContext());
        
        ViewModelProvider.Factory factory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new MainViewModel(getApplication(), soundProvider);
            }
        };

        MainViewModel viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        MaterialTextView timerText = findViewById(R.id.timerText);
        MaterialButton btnStart = findViewById(R.id.btnStartTimer);
        MaterialButton btnColor = findViewById(R.id.btnChangeColor);

        viewModel.getTimerDisplay().observe(this, timerText::setText);
        viewModel.getTextColor().observe(this, timerText::setTextColor);

        btnStart.setOnClickListener(v -> viewModel.startTimer());
        btnColor.setOnClickListener(v -> viewModel.randomizeColor());
    }
}
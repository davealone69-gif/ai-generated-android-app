package com.example.droidcraft;

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

    public MainViewModel(@NonNull android.app.Application application) {
        super(application);
        this.audioManager = new SoundEffectManager(application);
    }

    public LiveData<String> getTimerDisplay() { return timerDisplay; }
    public LiveData<Integer> getTextColor() { return textColor; }

    public void startTimer() {
        audioManager.playClick();
        if (timer != null) timer.cancel();
        
        timer = new CountDownTimer(INITIAL_TIME, 1000) {
            @Override
            public void onTick(long millis) {
                timerDisplay.postValue((millis / 1000) + "s");
            }
            @Override
            public void onFinish() {
                timerDisplay.postValue("Done!");
            }
        }.start();
    }

    public void randomizeColor() {
        audioManager.playClick();
        float[] hsl = new float[]{new Random().nextFloat() * 360f, 0.7f, 0.5f};
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

        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        MaterialTextView timerText = findViewById(R.id.timerText);
        MaterialButton btnStart = findViewById(R.id.btnStartTimer);
        MaterialButton btnColor = findViewById(R.id.btnChangeColor);

        btnStart.setContentDescription(getString(R.string.desc_start_timer));
        btnColor.setContentDescription(getString(R.string.desc_change_color));

        viewModel.getTimerDisplay().observe(this, timerText::setText);
        viewModel.getTextColor().observe(this, timerText::setTextColor);

        btnStart.setOnClickListener(v -> viewModel.startTimer());
        btnColor.setOnClickListener(v -> viewModel.randomizeColor());
    }
}
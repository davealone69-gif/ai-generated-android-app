package com.example.droidcraft;

import android.app.Application;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import java.util.Random;

class SoundEffectManager {
    private final SoundPool soundPool;
    private final int clickSoundId;

    public SoundEffectManager(Application app) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        this.soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(attrs).build();
        this.clickSoundId = soundPool.load(app, R.raw.click_sound, 1);
    }

    public void playClick() {
        soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
    }

    public void release() {
        soundPool.release();
    }
}

class MainViewModel extends ViewModel {
    private final SavedStateHandle state;
    private final SoundEffectManager soundManager;
    private CountDownTimer timer;

    private static final String KEY_REMAINING = "remaining_time";
    private static final String KEY_COLOR = "text_color";

    public MainViewModel(SavedStateHandle state, SoundEffectManager soundManager) {
        this.state = state;
        this.soundManager = soundManager;
        if (!state.contains(KEY_REMAINING)) state.set(KEY_REMAINING, 10000L);
        if (!state.contains(KEY_COLOR)) state.set(KEY_COLOR, Color.BLACK);
    }

    public LiveData<Long> getRemainingTime() { return state.getLiveData(KEY_REMAINING); }
    public LiveData<Integer> getTextColor() { return state.getLiveData(KEY_COLOR); }

    public void startTimer() {
        soundManager.playClick();
        if (timer != null) timer.cancel();
        
        timer = new CountDownTimer(state.get(KEY_REMAINING), 1000) {
            public void onTick(long millis) { state.set(KEY_REMAINING, millis); }
            public void onFinish() { state.set(KEY_REMAINING, 0L); }
        }.start();
    }

    public void randomizeColor() {
        soundManager.playClick();
        float[] hsl = new float[]{new Random().nextFloat() * 360f, 0.8f, 0.4f};
        state.set(KEY_COLOR, ColorUtils.HSLToColor(hsl));
    }

    @Override
    protected void onCleared() {
        if (timer != null) timer.cancel();
        soundManager.release();
    }
}

public class MainActivity extends AppCompatActivity {
    private SoundEffectManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        soundManager = new SoundEffectManager(getApplication());

        MainViewModel viewModel = new ViewModelProvider(this, new AbstractSavedStateViewModelFactory(this, null) {
            @NonNull
            @Override
            protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
                return (T) new MainViewModel(handle, soundManager);
            }
        }).get(MainViewModel.class);

        TextView timerText = findViewById(R.id.timerText);
        MaterialButton btnStart = findViewById(R.id.btnStartTimer);
        MaterialButton btnColor = findViewById(R.id.btnChangeColor);

        viewModel.getRemainingTime().observe(this, millis -> 
            timerText.setText(millis > 0 ? (millis / 1000) + "s" : "Done!"));
            
        viewModel.getTextColor().observe(this, color -> 
            timerText.animate().textColor(color).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator()).start());

        btnStart.setOnClickListener(v -> viewModel.startTimer());
        btnColor.setOnClickListener(v -> viewModel.randomizeColor());
    }
}
package com.example.droidcraft

import android.graphics.Color
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var timerDisplay: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSoundToggle: Button
    private var countDownTimer: CountDownTimer? = null
    private var isSoundEnabled = true
    private var toneGenerator: ToneGenerator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ToneGenerator for system sounds since raw files weren't provided
        toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)

        timerDisplay = findViewById(R.id.timerDisplay)
        btnStart = findViewById(R.id.btnStart)
        btnSoundToggle = findViewById(R.id.btnSoundToggle)

        btnStart.setOnClickListener {
            playSound()
            startTimer(30000)
        }

        btnSoundToggle.setOnClickListener {
            isSoundEnabled = !isSoundEnabled
            btnSoundToggle.text = if (isSoundEnabled) "Sound On" else "Sound Off"
        }

        findViewById<View>(R.id.colorRed).setOnClickListener { changeThemeColor(Color.parseColor("#CF6679")) }
        findViewById<View>(R.id.colorBlue).setOnClickListener { changeThemeColor(Color.parseColor("#03DAC5")) }
        findViewById<View>(R.id.colorGold).setOnClickListener { changeThemeColor(Color.parseColor("#FDD835")) }
    }

    private fun playSound() {
        if (isSoundEnabled) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        }
    }

    private fun startTimer(millis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000) % 60
                timerDisplay.text = String.format(Locale.getDefault(), "00:%02d", seconds)
            }

            override fun onFinish() {
                timerDisplay.text = "00:00"
            }
        }.start()
    }

    private fun changeThemeColor(color: Int) {
        timerDisplay.setTextColor(color)
        btnStart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color))
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        toneGenerator?.release()
        toneGenerator = null
    }
}
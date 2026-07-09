package com.example.droidcraft

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSoundToggle: Button
    
    private var countDownTimer: CountDownTimer? = null
    private var clickSound: MediaPlayer? = null
    private var soundEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.countdownDisplay)
        btnStart = findViewById(R.id.btnStart)
        btnSoundToggle = findViewById(R.id.btnSoundToggle)

        // Initialize sound (ensure click_sound.mp3 exists in res/raw/)
        clickSound = MediaPlayer.create(this, R.raw.click_sound)

        btnStart.setOnClickListener {
            playSound()
            startTimer()
        }

        btnSoundToggle.setOnClickListener {
            soundEnabled = !soundEnabled
            btnSoundToggle.text = if (soundEnabled) "Sound: ON" else "Sound: OFF"
        }

        setupColorPickers()
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                timerText.text = String.format("00:%02d", seconds)
            }
            override fun onFinish() {
                timerText.text = "Done!"
            }
        }.start()
    }

    private fun setupColorPickers() {
        val colors = listOf(R.id.colorRed, R.id.colorGreen, R.id.colorBlue)
        colors.forEach { id ->
            findViewById<View>(id).setOnClickListener {
                playSound()
                timerText.setTextColor((it.background as android.graphics.drawable.ColorDrawable).color)
            }
        }
    }

    private fun playSound() {
        if (soundEnabled) {
            clickSound?.seekTo(0)
            clickSound?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        clickSound?.release()
        clickSound = null
    }
}
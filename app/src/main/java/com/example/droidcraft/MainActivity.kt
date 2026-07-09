package com.example.droidcraft

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var countdownDisplay: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSoundToggle: Button
    
    private var countDownTimer: CountDownTimer? = null
    private var clickSound: MediaPlayer? = null
    private var isSoundEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countdownDisplay = findViewById(R.id.countdownDisplay)
        btnStart = findViewById(R.id.btnStart)
        btnSoundToggle = findViewById(R.id.btnSoundToggle)

        // Safely check if the resource exists to prevent build failures
        val resId = resources.getIdentifier("click_sound", "raw", packageName)
        if (resId != 0) {
            clickSound = MediaPlayer.create(this, resId)
        }

        btnStart.setOnClickListener {
            playSound()
            startTimer(10000)
        }

        btnSoundToggle.setOnClickListener {
            isSoundEnabled = !isSoundEnabled
            btnSoundToggle.text = if (isSoundEnabled) "SFX On" else "SFX Off"
        }

        findViewById<View>(R.id.colorRed).setOnClickListener { changeColor(Color.parseColor("#CF6679")) }
        findViewById<View>(R.id.colorBlue).setOnClickListener { changeColor(Color.parseColor("#03DAC5")) }
        findViewById<View>(R.id.colorGold).setOnClickListener { changeColor(Color.parseColor("#FFD700")) }
    }

    private fun startTimer(millis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownDisplay.text = String.format(Locale.getDefault(), "%02d", millisUntilFinished / 1000)
            }
            override fun onFinish() {
                countdownDisplay.text = "00"
            }
        }.start()
    }

    private fun changeColor(color: Int) {
        playSound()
        countdownDisplay.setTextColor(color)
    }

    private fun playSound() {
        if (isSoundEnabled && clickSound != null) {
            try {
                clickSound?.seekTo(0)
                clickSound?.start()
            } catch (e: Exception) {
                // Ignore playback errors
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        clickSound?.release()
        clickSound = null
    }
}
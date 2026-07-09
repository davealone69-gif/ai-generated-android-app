package com.example.droidcraft

import android.media.AudioAttributes
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var countdownDisplay: TextView
    private lateinit var btnToggleTimer: Button
    private lateinit var switchSound: Switch
    
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private val toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countdownDisplay = findViewById(R.id.countdownDisplay)
        btnToggleTimer = findViewById(R.id.btnToggleTimer)
        switchSound = findViewById(R.id.switchSound)

        btnToggleTimer.setOnClickListener {
            playSound()
            if (isTimerRunning) stopTimer() else startTimer()
        }

        findViewById<ImageButton>(R.id.colorRed).setOnClickListener { applyTheme("#FF5252") }
        findViewById<ImageButton>(R.id.colorBlue).setOnClickListener { applyTheme("#00E5FF") }
        findViewById<ImageButton>(R.id.colorGreen).setOnClickListener { applyTheme("#69F0AE") }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                countdownDisplay.text = String.format("00:%02d", seconds)
            }
            override fun onFinish() {
                countdownDisplay.text = "00:00"
                isTimerRunning = false
                btnToggleTimer.text = "START TIMER"
            }
        }.start()
        isTimerRunning = true
        btnToggleTimer.text = "STOP TIMER"
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnToggleTimer.text = "START TIMER"
    }

    private fun applyTheme(colorHex: String) {
        val color = android.graphics.Color.parseColor(colorHex)
        countdownDisplay.setTextColor(color)
        btnToggleTimer.setBackgroundColor(color)
    }

    private fun playSound() {
        if (switchSound.isChecked) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        toneGenerator.release()
    }
}
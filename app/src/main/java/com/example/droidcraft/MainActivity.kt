package com.example.droidcraft

import android.os.Bundle
import android.os.CountDownTimer
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var timerDisplay: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSoundToggle: Button
    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var isSoundEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerDisplay = findViewById(R.id.timerDisplay)
        btnStart = findViewById(R.id.btnStart)
        btnSoundToggle = findViewById(R.id.btnSoundToggle)

        btnStart.setOnClickListener {
            performFeedback()
            toggleTimer()
        }

        btnSoundToggle.setOnClickListener {
            isSoundEnabled = !isSoundEnabled
            btnSoundToggle.text = if (isSoundEnabled) "Sound: ON" else "Sound: OFF"
            performFeedback()
        }

        // Setup color picker clicks
        val colors = intArrayOf(
            android.graphics.Color.parseColor("#38BDF8"),
            android.graphics.Color.parseColor("#4ADE80"),
            android.graphics.Color.parseColor("#FB923C"),
            android.graphics.Color.parseColor("#F87171")
        )

        val ids = intArrayOf(R.id.colorOption1, R.id.colorOption2, R.id.colorOption3, R.id.colorOption4)
        for (i in ids.indices) {
            findViewById<android.view.View>(ids[i]).setOnClickListener {
                timerDisplay.setTextColor(colors[i])
                performFeedback()
            }
        }
    }

    private fun toggleTimer() {
        if (isRunning) {
            countDownTimer?.cancel()
            btnStart.text = "Start"
            isRunning = false
        } else {
            countDownTimer = object : CountDownTimer(1500000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val minutes = (millisUntilFinished / 1000) / 60
                    val seconds = (millisUntilFinished / 1000) % 60
                    timerDisplay.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                }
                override fun onFinish() {
                    timerDisplay.text = "00:00"
                    isRunning = false
                    btnStart.text = "Start"
                }
            }.start()
            btnStart.text = "Stop"
            isRunning = true
        }
    }

    private fun performFeedback() {
        if (isSoundEnabled) {
            window.decorView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
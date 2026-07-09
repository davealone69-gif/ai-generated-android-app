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
    private lateinit var countdownDisplay: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSound: Button
    private var countDownTimer: CountDownTimer? = null
    private var isSoundEnabled = true
    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countdownDisplay = findViewById(R.id.countdownDisplay)
        btnStart = findViewById(R.id.btnStart)
        btnSound = findViewById(R.id.btnSound)

        btnStart.setOnClickListener {
            playSound()
            startTimer(30000)
        }

        btnSound.setOnClickListener {
            isSoundEnabled = !isSoundEnabled
            btnSound.text = if (isSoundEnabled) "Sound: ON" else "Sound: OFF"
        }

        // Color Picker Setup
        val colors = listOf(Color.parseColor("#FF5252"), Color.parseColor("#03DAC5"), Color.parseColor("#BB86FC"))
        val colorViews = listOf(findViewById<View>(R.id.colorOption1), findViewById<View>(R.id.colorOption2), findViewById<View>(R.id.colorOption3))
        
        colorViews.forEachIndexed { index, view ->
            view.setOnClickListener {
                window.decorView.setBackgroundColor(colors[index])
                playSound()
            }
        }
    }

    private fun startTimer(millis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                countdownDisplay.text = String.format(Locale.getDefault(), "00:%02d", seconds)
            }
            override fun onFinish() {
                countdownDisplay.text = "00:00"
            }
        }.start()
    }

    private fun playSound() {
        if (isSoundEnabled) {
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        toneGen.release()
    }
}
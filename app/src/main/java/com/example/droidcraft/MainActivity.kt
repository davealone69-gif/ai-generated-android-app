package com.example.droidcraft

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var timerDisplay: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSound: Button
    private var countDownTimer: CountDownTimer? = null
    private var isSoundEnabled = true
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        timerDisplay = findViewById(R.id.timerDisplay)
        btnStart = findViewById(R.id.btnStart)
        btnSound = findViewById(R.id.btnSound)

        btnStart.setOnClickListener {
            playSound()
            countDownTimer?.cancel()
            countDownTimer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timerDisplay.text = String.format("00:%02d", millisUntilFinished / 1000)
                }
                override fun onFinish() {
                    timerDisplay.text = "00:00"
                }
            }.start()
        }

        btnSound.setOnClickListener {
            isSoundEnabled = !isSoundEnabled
            btnSound.text = if (isSoundEnabled) "Sound: ON" else "Sound: OFF"
            playSound()
        }

        // Define colors matching the XML theme
        val colors = intArrayOf(
            android.graphics.Color.parseColor("#FF5252"), 
            android.graphics.Color.parseColor("#4CAF50"), 
            android.graphics.Color.parseColor("#2196F3")
        )
        val colorIds = intArrayOf(R.id.colorOption1, R.id.colorOption2, R.id.colorOption3)
        
        for (i in colorIds.indices) {
            findViewById<View>(colorIds[i]).setOnClickListener {
                timerDisplay.setTextColor(colors[i])
                playSound()
            }
        }
    }

    private fun playSound() {
        if (isSoundEnabled) {
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        countDownTimer = null
    }
}
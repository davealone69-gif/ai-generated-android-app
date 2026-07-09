package com.example.droidcraft

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSoundToggle: Button
    
    private var countDownTimer: CountDownTimer? = null
    private var soundEnabled = true
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.countdownDisplay)
        btnStart = findViewById(R.id.btnStart)
        btnSoundToggle = findViewById(R.id.btnSoundToggle)

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
                timerText.text = "00:00"
            }
        }.start()
    }

    private fun setupColorPickers() {
        val colorMap = mapOf(
            R.id.colorRed to android.R.color.holo_red_light,
            R.id.colorGreen to android.R.color.holo_green_light,
            R.id.colorBlue to android.R.color.holo_blue_light
        )
        
        colorMap.forEach { (viewId, colorRes) ->
            findViewById<View>(viewId)?.setOnClickListener {
                playSound()
                val color = ContextCompat.getColor(this, colorRes)
                timerText.setTextColor(color)
            }
        }
    }

    private fun playSound() {
        if (soundEnabled) {
            // Using a simple system tone for reliability
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                mediaPlayer?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
package com.example.droidcraft

import android.media.AudioAttributes
import android.media.SoundPool
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
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SoundPool for better performance
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
        // Note: In a production app, load a specific asset. 
        // Using system notification as a placeholder.
        soundId = soundPool?.load(this, android.R.drawable.stat_notify_chat, 1) ?: 0

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
        if (soundEnabled && soundId != 0) {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        soundPool?.release()
        soundPool = null
        super.onDestroy()
    }
}
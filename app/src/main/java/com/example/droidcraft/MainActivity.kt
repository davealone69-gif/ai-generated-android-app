package com.example.droidcraft

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.ShapeDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

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

        // Initialize sound
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
            clickSound?.apply {
                if (isPlaying) stop()
                prepareAsync()
                start()
            }
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        clickSound?.release()
        clickSound = null
        super.onDestroy()
    }
}
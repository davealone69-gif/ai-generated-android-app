package com.example.droidcraft

import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

class MainActivity : AppCompatActivity() {
    private lateinit var countdownDisplay: TextView
    private lateinit var btnStartTimer: Button
    private lateinit var btnColorPicker: Button
    
    private var countDownTimer: CountDownTimer? = null
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var isSoundEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        countdownDisplay = findViewById(R.id.countdownDisplay)
        btnStartTimer = findViewById(R.id.btnStartTimer)
        btnColorPicker = findViewById(R.id.btnColorPicker)
        val btnToggleSound = findViewById<Button>(R.id.btnToggleSound)

        // Initialize SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        btnStartTimer.setOnClickListener {
            playSound()
            startCountdown()
        }

        btnColorPicker.setOnClickListener {
            playSound()
            val rnd = Random()
            val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            countdownDisplay.setTextColor(color)
        }

        btnToggleSound.setOnClickListener {
            isSoundEnabled = !isSoundEnabled
            btnToggleSound.text = if (isSoundEnabled) "Sound: ON" else "Sound: OFF"
        }
    }

    private fun startCountdown() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownDisplay.text = (millisUntilFinished / 1000).toString()
            }
            override fun onFinish() {
                countdownDisplay.text = "Done!"
            }
        }.start()
    }

    private fun playSound() {
        if (isSoundEnabled) {
            soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        soundPool?.release()
        soundPool = null
    }
}
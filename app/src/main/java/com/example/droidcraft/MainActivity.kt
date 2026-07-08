package com.example.droidcraft

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var timerText: TextView
    private lateinit var btnStart: Button
    private lateinit var soundToggle: CheckBox
    private var countDownTimer: CountDownTimer? = null
    private var clickSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.countdownDisplay)
        btnStart = findViewById(R.id.btnStartTimer)
        soundToggle = findViewById(R.id.toggleSound)

        // Ensure raw resource exists or handle gracefully
        try {
            clickSound = MediaPlayer.create(this, R.raw.click_sound)
        } catch (e: Exception) {
            clickSound = null
        }

        btnStart.setOnClickListener {
            playSound()
            countDownTimer?.cancel()
            countDownTimer = object : CountDownTimer(30000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val seconds = millisUntilFinished / 1000
                    timerText.text = String.format("%02d", seconds)
                }
                override fun onFinish() {
                    timerText.text = "00"
                }
            }.start()
        }

        findViewById<ImageButton>(R.id.btnColorRed).setOnClickListener { changeColor(Color.parseColor("#FF5252")) }
        findViewById<ImageButton>(R.id.btnColorGreen).setOnClickListener { changeColor(Color.parseColor("#69F0AE")) }
        findViewById<ImageButton>(R.id.btnColorBlue).setOnClickListener { changeColor(Color.parseColor("#448AFF")) }
    }

    private fun changeColor(color: Int) {
        playSound()
        timerText.setTextColor(color)
    }

    private fun playSound() {
        if (soundToggle.isChecked && clickSound != null) {
            clickSound?.apply {
                if (isPlaying) {
                    pause()
                    seekTo(0)
                }
                start()
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
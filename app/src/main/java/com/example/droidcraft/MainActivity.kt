package com.example.droidcraft

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var timerText: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSound: Button
    private var countDownTimer: CountDownTimer? = null
    private var isSoundEnabled = true
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.countdownTimer)
        btnStart = findViewById(R.id.btnStart)
        btnSound = findViewById(R.id.btnSoundToggle)

        // Initialize click sound placeholder (ToneGenerator could be used if no raw file exists)
        mediaPlayer = MediaPlayer.create(this, android.R.raw.click) 

        btnStart.setOnClickListener {
            playSound()
            countDownTimer?.cancel()
            countDownTimer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timerText.text = String.format(Locale.getDefault(), "%02d", millisUntilFinished / 1000)
                }
                override fun onFinish() {
                    timerText.text = "00"
                }
            }.start()
        }

        btnSound.setOnClickListener {
            isSoundEnabled = !isSoundEnabled
            btnSound.text = if (isSoundEnabled) "FX: On" else "FX: Off"
        }

        val colors = listOf(Color.parseColor("#FF5252"), Color.parseColor("#00E676"), Color.parseColor("#2979FF"))
        val views = listOf(findViewById(R.id.colorOption1), findViewById(R.id.colorOption2), findViewById(R.id.colorOption3))
        
        views.forEachIndexed { index, view ->
            view.setOnClickListener {
                playSound()
                timerText.setTextColor(colors[index])
            }
        }
    }

    private fun playSound() {
        if (isSoundEnabled) {
            mediaPlayer?.seekTo(0)
            mediaPlayer?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        mediaPlayer?.release()
    }
}
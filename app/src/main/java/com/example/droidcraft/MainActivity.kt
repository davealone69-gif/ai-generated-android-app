package com.example.droidcraft

import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var tvCountdown: TextView
    private lateinit var btnStart: Button
    private var countDownTimer: CountDownTimer? = null
    private var clickSound: MediaPlayer? = null
    private var isSoundEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCountdown = findViewById(R.id.tvCountdown)
        btnStart = findViewById(R.id.btnStart)
        val btnSoundToggle = findViewById<Button>(R.id.btnSoundToggle)

        // Initialize sound safely
        clickSound = MediaPlayer.create(this, android.R.raw.alert_light)

        btnStart.setOnClickListener {
            playSound()
            startTimer(30000)
        }

        btnSoundToggle.setOnClickListener {
            isSoundEnabled = !isSoundEnabled
            playSound()
        }

        // Robust Color Picker Logic
        val colorIds = listOf(R.id.colorRed, R.id.colorBlue, R.id.colorYellow)
        colorIds.forEach { id ->
            findViewById<View>(id).setOnClickListener { view ->
                val background = view.background
                if (background is ColorDrawable) {
                    tvCountdown.setTextColor(background.color)
                    playSound()
                }
            }
        }
    }

    private fun startTimer(millis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvCountdown.text = String.format(Locale.getDefault(), "00:%02d", seconds)
            }
            override fun onFinish() {
                tvCountdown.text = "00:00"
            }
        }.start()
    }

    private fun playSound() {
        if (isSoundEnabled && clickSound != null) {
            try {
                if (clickSound!!.isPlaying) {
                    clickSound!!.pause()
                    clickSound!!.seekTo(0)
                }
                clickSound!!.start()
            } catch (e: Exception) {
                e.printStackTrace()
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
package com.example.droidcraft

import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var tvTimer: TextView
    private lateinit var tvHeader: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSound: Button
    private var countDownTimer: CountDownTimer? = null
    private val toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTimer = findViewById(R.id.timerDisplay)
        tvHeader = findViewById(R.id.titleHeader)
        btnStart = findViewById(R.id.btnStart)
        btnSound = findViewById(R.id.btnSound)

        btnStart.setOnClickListener {
            playSound()
            startTimer()
        }

        btnSound.setOnClickListener {
            playSound()
        }

        setupColorPickers()
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvTimer.text = String.format(Locale.getDefault(), "00:%02d", seconds)
            }
            override fun onFinish() {
                tvTimer.text = "00:00"
            }
        }.start()
    }

    private fun setupColorPickers() {
        val colors = listOf(R.id.colorRed, R.id.colorBlue, R.id.colorPurple)
        colors.forEach { id ->
            findViewById<View>(id).setOnClickListener { view ->
                playSound()
                val background = view.background
                if (background is ColorDrawable) {
                    tvHeader.setTextColor(background.color)
                }
            }
        }
    }

    private fun playSound() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        toneGenerator.release()
    }
}
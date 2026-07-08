package com.example.droidcraft

import android.graphics.Color
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
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTimer = findViewById(R.id.timerDisplay)
        tvHeader = findViewById(R.id.titleHeader)
        btnStart = findViewById(R.id.btnStart)

        btnStart.setOnClickListener {
            startTimer()
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
                tvHeader.setTextColor((view.background as android.graphics.drawable.ColorDrawable).color)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
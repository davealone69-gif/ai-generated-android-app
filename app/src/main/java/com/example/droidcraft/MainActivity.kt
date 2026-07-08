package com.example.droidcraft

import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var timerText: TextView
    private lateinit var btnStart: Button
    private var countDownTimer: CountDownTimer? = null
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.countdownDisplay)
        btnStart = findViewById(R.id.btnStart)
        
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // Using a standard system sound resource available across all API levels
        soundId = soundPool?.load(this, android.R.drawable.btn_default, 1) ?: 0

        btnStart.setOnClickListener {
            playSound()
            startTimer()
        }

        findViewById<View>(R.id.colorRed)?.setOnClickListener { timerText.setTextColor(Color.RED) }
        findViewById<View>(R.id.colorBlue)?.setOnClickListener { timerText.setTextColor(Color.BLUE) }
        findViewById<View>(R.id.colorGreen)?.setOnClickListener { timerText.setTextColor(Color.parseColor("#00E676")) }
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = "00:${(millisUntilFinished / 1000).toString().padStart(2, '0')}"
            }
            override fun onFinish() {
                timerText.text = "Done!"
            }
        }.start()
    }

    private fun playSound() {
        if (soundId != 0) {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        soundPool?.release()
        soundPool = null
    }
}
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
    private lateinit var timerDisplay: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSound: Button
    private var countDownTimer: CountDownTimer? = null
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var isSoundEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerDisplay = findViewById(R.id.timerDisplay)
        btnStart = findViewById(R.id.btnStart)
        btnSound = findViewById(R.id.btnSound)

        // Initialize SoundPool safely
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build()
        
        // Load sound safely, 0 indicates failure/missing
        val resId = resources.getIdentifier("click", "raw", packageName)
        if (resId != 0) {
            soundId = soundPool?.load(this, resId, 1) ?: 0
        }

        btnStart.setOnClickListener {
            playSound()
            countDownTimer?.cancel()
            countDownTimer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timerDisplay.text = String.format("00:%02d", millisUntilFinished / 1000)
                }
                override fun onFinish() {
                    timerDisplay.text = "00:00"
                }
            }.start()
        }

        btnSound.setOnClickListener {
            isSoundEnabled = !isSoundEnabled
            playSound()
        }

        // Color Picker Logic
        val colors = intArrayOf(Color.parseColor("#FF5252"), Color.parseColor("#4CAF50"), Color.parseColor("#2196F3"))
        val colorIds = intArrayOf(R.id.colorOption1, R.id.colorOption2, R.id.colorOption3)
        
        for (i in colorIds.indices) {
            findViewById<View>(colorIds[i]).setOnClickListener {
                timerDisplay.setTextColor(colors[i])
            }
        }
    }

    private fun playSound() {
        if (isSoundEnabled && soundId != 0) {
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
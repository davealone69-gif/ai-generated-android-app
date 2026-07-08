package com.example.droidcraft

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var timerDisplay: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSound: Button
    private var countDownTimer: CountDownTimer? = null
    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0
    private var isSoundEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerDisplay = findViewById(R.id.timerDisplay)
        btnStart = findViewById(R.id.btnStart)
        btnSound = findViewById(R.id.btnSound)

        // Initialize SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build()
        
        // Note: Ensure res/raw/click.ogg exists or handle soundId safely
        soundId = try {
            soundPool.load(this, resources.getIdentifier("click", "raw", packageName), 1)
        } catch (e: Exception) { 0 }

        btnStart.setOnClickListener {
            playSound()
            countDownTimer?.cancel()
            countDownTimer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secs = millisUntilFinished / 1000
                    timerDisplay.text = String.format("00:%02d", secs)
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
        val colors = listOf(0xFFFF5252.toInt(), 0xFF4CAF50.toInt(), 0xFF2196F3.toInt())
        listOf(R.id.colorOption1, R.id.colorOption2, R.id.colorOption3).forEachIndexed { index, id ->
            findViewById<android.view.View>(id).setOnClickListener {
                timerDisplay.setTextColor(colors[index])
            }
        }
    }

    private fun playSound() {
        if (isSoundEnabled && soundId != 0) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        soundPool.release()
    }
}
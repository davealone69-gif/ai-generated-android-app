package com.example.droidcraft

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class SoundEffectManager(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()
    
    // Using a system sound constant
    private val soundId = soundPool.load(context, android.R.drawable.stat_notify_more, 1)

    fun play() = soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    fun release() = soundPool.release()
}

class TimerViewModel : ViewModel() {
    private val _timeLeft = MutableStateFlow(10L)
    val timeLeft = _timeLeft.asStateFlow()

    private val _textColor = MutableStateFlow(Color(0xFF6200EE))
    val textColor = _textColor.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer(onFinished: () -> Unit) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (i in 10 downTo 0) {
                _timeLeft.value = i.toLong()
                delay(1000)
            }
            onFinished()
        }
    }

    fun randomizeColor() {
        _textColor.value = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var soundManager: SoundEffectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundEffectManager(this)
        
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimerScreen(onPlaySound = { soundManager.play() })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

@Composable
fun TimerScreen(viewModel: TimerViewModel = viewModel(), onPlaySound: () -> Unit) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val targetColor by viewModel.textColor.collectAsState()
    val animatedColor by animateColorAsState(targetColor, tween(500), label = "color")

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (timeLeft == 0L) "READY!" else "00:${timeLeft.toString().padStart(2, '0')}",
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            color = animatedColor
        )
        
        Spacer(modifier = Modifier.height(64.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { viewModel.startTimer(onPlaySound) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(64.dp).weight(1f)
            ) {
                Text("Start")
            }

            OutlinedButton(
                onClick = { viewModel.randomizeColor() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(64.dp).weight(1f)
            ) {
                Text("Color")
            }
        }
    }
}
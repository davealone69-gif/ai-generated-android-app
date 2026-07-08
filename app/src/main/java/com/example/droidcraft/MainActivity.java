package com.example.droidcraft

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class TimerViewModel : ViewModel() {
    private val _timeLeft = MutableStateFlow(10L)
    val timeLeft: StateFlow<Long> = _timeLeft

    private val _textColor = MutableStateFlow(Color(0xFF6200EE))
    val textColor: StateFlow<Color> = _textColor

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
        _textColor.value = Color(
            red = Random.nextFloat(),
            green = Random.nextFloat(),
            blue = Random.nextFloat(),
            alpha = 1f
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: TimerViewModel by viewModels()
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TimerScreen(viewModel) {
                        mediaPlayer?.start()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

@Composable
fun TimerScreen(viewModel: TimerViewModel, playSound: () -> Unit) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val textColor by viewModel.textColor.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (timeLeft == 0L) "READY!" else "00:${timeLeft.toString().padStart(2, '0')}",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { viewModel.startTimer(playSound) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp).weight(1f)
            ) {
                Text("Start Timer")
            }

            OutlinedButton(
                onClick = { viewModel.randomizeColor() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp).weight(1f)
            ) {
                Text("Pick Color")
            }
        }
    }
}
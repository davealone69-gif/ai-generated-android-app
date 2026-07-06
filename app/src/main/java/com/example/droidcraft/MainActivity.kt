package com.example.droidcraft

import android.os.Bundle
import android.media.ToneGenerator
import android.media.AudioManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainAppScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    // Custom Color picker states (RGB)
    var redVal by remember { mutableStateOf(0.24f) }
    var greenVal by remember { mutableStateOf(0.52f) }
    var blueVal by remember { mutableStateOf(0.95f) }
    val pickedColor = Color(redVal, greenVal, blueVal)

    // Preset color list for fast selecting
    val presetColors = listOf(
        Color(0xFFFF5252), // Coral Flame
        Color(0xFFFF9800), // Sunshine Gold
        Color(0xFF4CAF50), // Fresh Mint
        Color(0xFF00BCD4), // Ocean Cyan
        Color(0xFF9C27B0), // Regal Violet
        Color(0xFFE91E63)  // Hot Rose
    )

    // Sound preferences
    var soundEnabled by remember { mutableStateOf(true) }
    var alertToneType by remember { mutableStateOf(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD) }

    // Helper functions for safe sound generator calls
    fun playSystemTone(tone: Int, duration: Int = 120) {
        if (!soundEnabled) return
        try {
            val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            tg.startTone(tone, duration)
            // Auto release after sound
            tg.release()
        } catch (e: Exception) {
            // Safe fallback if system audio resources are locked
        }
    }

    // Countdown Timer logic state
    var totalDurationSeconds by remember { mutableStateOf(60) }
    var remainingSeconds by remember { mutableStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Active tick-tick background effect or pulse factor
    val animatedProgress by animateFloatAsState(
        targetValue = if (totalDurationSeconds > 0) remainingSeconds.toFloat() / totalDurationSeconds.toFloat() else 0f,
        label = "timerProgress"
    )

    // Background runner
    LaunchedEffect(isTimerRunning, remainingSeconds) {
        if (isTimerRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds > 0) {
                // Play subtle tick audio
                playSystemTone(ToneGenerator.TONE_PROP_BEEP, 60)
            } else {
                isTimerRunning = false
                // Play triumph completion sound sequence
                playSystemTone(alertToneType, 500)
            }
        } else if (remainingSeconds == 0) {
            isTimerRunning = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F111A) // Sleek Premium Dark Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Title Header
            Text(
                text = "DroidCraft Chrono",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )

            // Visual Dynamic Circle Progress Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161925)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Circular Track background glow
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.size(200.dp),
                        color = pickedColor.copy(alpha = 0.15f),
                        strokeWidth = 14.dp
                    )

                    // Active Countdown Indicator
                    CircularProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.size(200.dp),
                        color = pickedColor,
                        strokeWidth = 14.dp
                    )

                    // Timer Counter Digits
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val displayMinutes = remainingSeconds / 60
                        val displaySeconds = remainingSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", displayMinutes, displaySeconds),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTimerRunning) "RUNNING" else if (remainingSeconds == 0) "FINISHED" else "PAUSED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = pickedColor
                        )
                    }
                }
            }

            // Quick controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preset modification (-10s, +10s)
                Button(
                    onClick = {
                        playSystemTone(ToneGenerator.TONE_PROP_BEEP2, 50)
                        if (remainingSeconds >= 10) {
                            remainingSeconds -= 10
                        } else {
                            remainingSeconds = 0
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23283D))
                ) {
                    Text("-10s", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Primary Start / Pause Toggle action button
                Button(
                    onClick = {
                        if (remainingSeconds == 0) {
                            remainingSeconds = totalDurationSeconds
                        }
                        isTimerRunning = !isTimerRunning
                        playSystemTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                    },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = pickedColor)
                ) {
                    Text(
                        text = if (isTimerRunning) "Pause" else "Start",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                // Add 10 seconds button
                Button(
                    onClick = {
                        playSystemTone(ToneGenerator.TONE_PROP_BEEP2, 50)
                        remainingSeconds += 10
                        totalDurationSeconds = maxOf(totalDurationSeconds, remainingSeconds)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23283D))
                ) {
                    Text("+10s", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Full reset & configuration panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preset times selector (1 Min, 3 Min, 5 Min)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3, 5).forEach { mins ->
                        val secs = mins * 60
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (totalDurationSeconds == secs) pickedColor else Color(0xFF23283D))
                                .clickable {
                                    playSystemTone(ToneGenerator.TONE_PROP_BEEP2, 80)
                                    totalDurationSeconds = secs
                                    remainingSeconds = secs
                                    isTimerRunning = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${mins}m",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Refresh / Reset button to initial configured state
                IconButton(
                    onClick = {
                        playSystemTone(ToneGenerator.TONE_PROP_BEEP, 120)
                        isTimerRunning = false
                        remainingSeconds = totalDurationSeconds
                    },
                    modifier = Modifier.background(Color(0xFF23283D), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = Color.White
                    )
                }
            }

            // Custom Color Theme Picker Panel Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161925)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Customize Theme Color",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )

                    // Quick-switch Color Palette selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        presetColors.forEach { colorItem ->
                            val isSelected = (colorItem == pickedColor)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(colorItem)
                                    .clickable {
                                        playSystemTone(ToneGenerator.TONE_PROP_BEEP2, 60)
                                        // Update standard RGB sliders based on hex match
                                        val argb = colorItem.toArgb()
                                        redVal = ((argb shr 16) and 0xFF) / 255f
                                        greenVal = ((argb shr 8) and 0xFF) / 255f
                                        blueVal = (argb and 0xFF) / 255f
                                    }
                                    .padding(4.dp)
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.6f))
                                    )
                                }
                            }
                        }
                    }

                    // Precise Custom RGB sliders
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // RED
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("R", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(16.dp))
                            Slider(
                                value = redVal,
                                onValueChange = { redVal = it },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red.copy(0.4f))
                            )
                        }
                        // GREEN
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(16.dp))
                            Slider(
                                value = greenVal,
                                onValueChange = { greenVal = it },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green.copy(0.4f))
                            )
                        }
                        // BLUE
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("B", color = Color(0xFF3F51B5), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(16.dp))
                            Slider(
                                value = blueVal,
                                onValueChange = { blueVal = it },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF3F51B5), activeTrackColor = Color(0xFF3F51B5).copy(0.4f))
                            )
                        }
                    }
                }
            }

            // Sound and Customization Preference Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161925)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sound Effects",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Plays tick and finish alarm sound",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            if (soundEnabled) {
                                playSystemTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = pickedColor,
                            checkedTrackColor = pickedColor.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }
    }
}
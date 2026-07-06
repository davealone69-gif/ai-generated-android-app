package com.example.droidcraft

import android.os.Bundle
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.ceil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainAppScreen()
        }
    }
}

// Global sound synthesis helpers to bypass asset dependencies
fun playTone(frequency: Double, durationMs: Int) {
    Thread {
        try {
            val sampleRate = 8000
            val numSamples = durationMs * sampleRate / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
                buffer[i] = (Math.sin(angle) * 32767).toInt().toShort()
            }
            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffer.size * 2,
                AudioTrack.MODE_STATIC
            )
            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 30)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}

fun playFinishMelody() {
    Thread {
        val notes = doubleArrayOf(523.25, 659.25, 784.00, 1046.50)
        for (note in notes) {
            playTone(note, 130)
            Thread.sleep(150)
        }
    }.start()
}

data class ColorPreset(val name: String, val hue: Float, val saturation: Float, val value: Float)

@Composable
fun MainAppScreen() {
    // Styling States
    var hue by remember { mutableFloatStateOf(200f) }
    var saturation by remember { mutableFloatStateOf(0.85f) }
    var value by remember { mutableFloatStateOf(0.95f) }
    
    val accentColor = Color.hsv(hue, saturation, value)
    val animatedAccentColor by animateColorAsState(targetValue = accentColor, animationSpec = tween(400))
    val darkBgColor = Color(0xFF0F1016)
    val cardBgColor = Color(0xFF1B1D2A)

    // Presets
    val presets = listOf(
        ColorPreset("Sunset", 15f, 0.9f, 0.95f),
        ColorPreset("Emerald", 145f, 0.85f, 0.9f),
        ColorPreset("Retro Neon", 315f, 0.9f, 0.95f),
        ColorPreset("Ocean Glow", 195f, 0.9f, 0.95f),
        ColorPreset("Amethyst", 270f, 0.8f, 0.95f)
    )

    // Timer States
    var totalDurationMs by remember { mutableLongStateOf(60000L) }
    var timeLeftMs by remember { mutableLongStateOf(60000L) }
    var isRunning by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }

    // Synchronize total duration on custom manual reset adjustments
    fun addTime(ms: Long) {
        if (!isRunning) {
            totalDurationMs = (totalDurationMs + ms).coerceIn(1000L, 3599000L)
            timeLeftMs = totalDurationMs
        } else {
            timeLeftMs = (timeLeftMs + ms).coerceAtLeast(0L)
            if (timeLeftMs > totalDurationMs) {
                totalDurationMs = timeLeftMs
            }
        }
    }

    // Main Timer Loop Effect
    LaunchedEffect(isRunning, timeLeftMs) {
        if (isRunning && timeLeftMs > 0) {
            var lastTickTime = System.currentTimeMillis()
            var lastSec = ceil(timeLeftMs / 1000.0).toInt()
            while (isRunning && timeLeftMs > 0) {
                delay(40)
                val current = System.currentTimeMillis()
                val delta = current - lastTickTime
                lastTickTime = current
                timeLeftMs = (timeLeftMs - delta).coerceAtLeast(0L)

                // Manage sound effects per second change
                val currentSec = ceil(timeLeftMs / 1000.0).toInt()
                if (currentSec != lastSec && isRunning && soundEnabled && currentSec > 0) {
                    playTone(900.0, 25)
                    lastSec = currentSec
                }
            }
            if (timeLeftMs == 0L && isRunning) {
                isRunning = false
                if (soundEnabled) {
                    playFinishMelody()
                }
            }
        }
    }

    // Arc fraction calculation
    val progressFraction = if (totalDurationMs > 0) {
        timeLeftMs.toFloat() / totalDurationMs.toFloat()
    } else {
        0f
    }
    val animatedProgressFraction by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(50)
    )

    // Timer Text Format
    val displayMin = (timeLeftMs / 60000).toString().padStart(2, '0')
    val displaySec = ((timeLeftMs % 60000) / 1000).toString().padStart(2, '0')
    val displayMs = ((timeLeftMs % 1000) / 100).toString()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = darkBgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AURA",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "Dynamic Timer & Color Synthesizer",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = animatedAccentColor.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            }

            // Radial Timer Card Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBgColor)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Radial Arc
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val size = Size(diameter, diameter)
                    val offset = Offset((this.size.width - diameter) / 2, (this.size.height - diameter) / 2)

                    // Track background with subtle color
                    drawArc(
                        color = Color.White.copy(alpha = 0.05f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = offset,
                        size = size,
                        style = Stroke(width = strokeWidth)
                    )

                    // Progress sweep
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                animatedAccentColor.copy(alpha = 0.4f),
                                animatedAccentColor,
                                animatedAccentColor.copy(alpha = 0.4f)
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedProgressFraction * 360f,
                        useCenter = false,
                        topLeft = offset,
                        size = size,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner Time Readout
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$displayMin:$displaySec",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 54.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ".$displayMs",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = animatedAccentColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isRunning) "RUNNING" else if (timeLeftMs == 0L) "FINISHED" else "READY",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isRunning) animatedAccentColor else Color.LightGray,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Quick Actions & Controls Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Time adjustment quick links
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            "+10s" to 10000L,
                            "+1m" to 60000L,
                            "+5m" to 300000L
                        ).forEach { (label, duration) ->
                            Button(
                                onClick = { addTime(duration) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.07f),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = label, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                isRunning = false
                                timeLeftMs = 60000L
                                totalDurationMs = 60000L
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.15f),
                                contentColor = Color(0xFFFF5252)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Reset", fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Core state modifiers: Start/Pause & Audio options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Sound FX",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { soundEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = animatedAccentColor,
                                    checkedTrackColor = animatedAccentColor.copy(alpha = 0.4f),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )
                        }

                        Button(
                            onClick = { isRunning = !isRunning },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) Color.DarkGray else animatedAccentColor,
                                contentColor = if (isRunning) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.width(140.dp)
                        ) {
                            Text(
                                text = if (isRunning) "PAUSE" else "START",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // Custom Dynamic Color Synthesizer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "COLOR SYNTHESIZER",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )

                    // Presets Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            val isSelected = (hue == preset.hue)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.hsv(preset.hue, preset.saturation, preset.value))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        hue = preset.hue
                                        saturation = preset.saturation
                                        value = preset.value
                                        if (soundEnabled) {
                                            playTone(600.0 + preset.hue.toDouble(), 60)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.name.take(3).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = if (preset.hue > 40f && preset.hue < 180f) Color.Black else Color.White
                                )
                            }
                        }
                    }

                    // Interactive Sliders
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Hue Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Hue Value", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text(text = "${hue.toInt()}°", color = animatedAccentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = hue,
                                onValueChange = { hue = it },
                                valueRange = 0f..360f,
                                colors = SliderDefaults.colors(
                                    thumbColor = animatedAccentColor,
                                    activeTrackColor = animatedAccentColor.copy(alpha = 0.5f),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }

                        // Saturation Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Saturation", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text(text = "${(saturation * 100).toInt()}%", color = animatedAccentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = saturation,
                                onValueChange = { saturation = it },
                                valueRange = 0.1f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = animatedAccentColor,
                                    activeTrackColor = animatedAccentColor.copy(alpha = 0.5f),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            }

            // Simple aesthetic footer glow
            Box(
                modifier = Modifier
                    .size(60.dp, 4.dp)
                    .clip(CircleShape)
                    .background(animatedAccentColor.copy(alpha = 0.5f))
            )
        }
    }
}
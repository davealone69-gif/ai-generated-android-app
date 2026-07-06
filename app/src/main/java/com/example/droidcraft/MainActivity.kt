package com.example.droidcraft

import android.os.Bundle
import android.media.AudioManager
import android.media.ToneGenerator
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainAppScreen()
        }
    }
}

// Fail-safe System Audio Synthesizer for high-performance responsive sound design
class SoundSynthesizer {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun playTick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (e: Exception) { /* Handle hardware resource limitation gracefully */ }
    }

    fun playComplete() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 350)
        } catch (e: Exception) { /* Fail-safe */ }
    }

    fun playToggle() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
        } catch (e: Exception) { /* Fail-safe */ }
    }

    fun playPreset() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
        } catch (e: Exception) { /* Fail-safe */ }
    }

    fun release() {
        toneGenerator?.release()
    }
}

@Composable
fun MainAppScreen() {
    // Sound System Lifecycle Management
    val soundSynthesizer = remember { SoundSynthesizer() }
    DisposableEffect(Unit) {
        onDispose {
            soundSynthesizer.release()
        }
    }

    // Interactive Custom Theme Color Setup (HSV implementation for highly vibrant colors)
    var hue by remember { mutableFloatStateOf(180f) }
    var saturation by remember { mutableFloatStateOf(0.9f) }
    var value by remember { mutableFloatStateOf(0.95f) }
    
    val currentThemeColor by remember(hue, saturation, value) {
        derivedStateOf {
            Color.hsv(hue, saturation, value)
        }
    }
    
    val animatedThemeColor by animateColorAsState(
        targetValue = currentThemeColor,
        animationSpec = tween(durationMillis = 300),
        label = "ThemeColorTransition"
    )

    // Preset color palettes for streamlined user experience
    val presetPalettes = listOf(
        Color(0xFFFF2A6D), // Cyber Neon Pink
        Color(0xFF05D9E8), // Neon Cyan
        Color(0xFF01F9C6), // Mint Matrix
        Color(0xFFFCEE09), // Cyberpunk Gold
        Color(0xFF9D4EDD)  // Hyper Purple
    )

    // Core Countdown State Machine Variables
    var totalDurationSeconds by remember { mutableIntStateOf(60) }
    var remainingSeconds by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Visual Percentage Progress Calculation
    val progressFraction by remember(remainingSeconds, totalDurationSeconds) {
        derivedStateOf {
            if (totalDurationSeconds > 0) {
                remainingSeconds.toFloat() / totalDurationSeconds.toFloat()
            } else {
                0f
            }
        }
    }
    
    val animatedProgressFraction by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 400),
        label = "ProgressRingTransition"
    )

    // Interactive Core Ticker Loop
    LaunchedEffect(isTimerRunning, remainingSeconds) {
        if (isTimerRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                isTimerRunning = false
                soundSynthesizer.playComplete()
            } else {
                soundSynthesizer.playTick()
            }
        }
    }

    // Adaptive Display Text Formatter
    fun formatDuration(totalSecs: Int): String {
        val minutes = totalSecs / 60
        val seconds = totalSecs % 60
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16)) // Premium deep galactic black background
    ) {
        // High-fidelity background glow based on dynamic custom selected color
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-150).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedThemeColor.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Application Premium Header Bar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DROIDCRAFT CHRONO",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = animatedThemeColor,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "Aesthetic Audio Countdown Hub",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // High-fidelity Circular Visual Countdown Core Component
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(260.dp)
                    .padding(12.dp)
            ) {
                // Secondary background track indicator
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = animatedThemeColor.copy(alpha = 0.08f),
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Primary glowing animated indicator ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = animatedThemeColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgressFraction * 360f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner Glassmorphic Display Core
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize(0.82f)
                        .clip(CircleShape)
                        .background(Color(0xFF131A2A).copy(alpha = 0.85f))
                        .border(1.5.dp, animatedThemeColor.copy(alpha = 0.25f), CircleShape)
                ) {
                    Text(
                        text = formatDuration(remainingSeconds),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isTimerRunning) "ACTIVE TICKING" else if (remainingSeconds == 0) "FINISHED" else "STANDBY READY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isTimerRunning) animatedThemeColor else Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                }
            }

            // Quick Interval Adders Panel (Only clickable when timer is idle/paused)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val adjustmentPresets = listOf(
                    "+30s" to 30,
                    "+1m" to 60,
                    "+5m" to 300,
                    "+10m" to 600
                )
                adjustmentPresets.forEach { (label, duration) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF131A2A))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .clickable(enabled = !isTimerRunning) {
                                soundSynthesizer.playPreset()
                                totalDurationSeconds += duration
                                remainingSeconds = totalDurationSeconds
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (!isTimerRunning) Color.White else Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // Core Playback Controls Module
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Secondary Button: Reset Time
                Button(
                    onClick = {
                        soundSynthesizer.playToggle()
                        isTimerRunning = false
                        totalDurationSeconds = 60
                        remainingSeconds = 60
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESET", fontWeight = FontWeight.Bold)
                }

                // Primary Dynamic Button: Play / Pause
                Button(
                    onClick = {
                        if (remainingSeconds > 0) {
                            soundSynthesizer.playToggle()
                            isTimerRunning = !isTimerRunning
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = animatedThemeColor,
                            spotColor = animatedThemeColor
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = animatedThemeColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                        contentDescription = if (isTimerRunning) "Pause Timer" else "Start Timer",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTimerRunning) "PAUSE CHRONO" else "START CHRONO",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Interactive Live Theme Color Picker Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "THEME & CHROMATIC SELECTOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.5.sp
                    )

                    // Color presets horizontal list view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetPalettes.forEach { presetColor ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(presetColor)
                                    .border(
                                        width = if (currentThemeColor == presetColor) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        soundSynthesizer.playPreset()
                                        // Deconstruct Hex Color back to HSV components
                                        val hsv = FloatArray(3)
                                        android.graphics.Color.colorToHSV(
                                            android.graphics.Color.rgb(
                                                (presetColor.red * 255).toInt(),
                                                (presetColor.green * 255).toInt(),
                                                (presetColor.blue * 255).toInt()
                                            ),
                                            hsv
                                        )
                                        hue = hsv[0]
                                        saturation = hsv[1]
                                        value = hsv[2]
                                    }
                            )
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.08f))

                    // Dynamic Hue Control Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Hue Color Tone",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${hue.toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = animatedThemeColor
                            )
                        }
                        Slider(
                            value = hue,
                            onValueChange = { hue = it },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(
                                thumbColor = animatedThemeColor,
                                activeTrackColor = animatedThemeColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    // Dynamic Saturation Level Control Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Color Saturation Intensity",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${(saturation * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = animatedThemeColor
                            )
                        }
                        Slider(
                            value = saturation,
                            onValueChange = { saturation = it },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = animatedThemeColor,
                                activeTrackColor = animatedThemeColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
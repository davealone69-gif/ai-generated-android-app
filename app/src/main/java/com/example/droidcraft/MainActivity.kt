package com.example.droidcraft

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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

@Composable
fun MainAppScreen() {
    var selectedHue by remember { mutableStateOf(340f) } // Default pink-red hue
    val primaryColor = remember(selectedHue) { colorFromHue(selectedHue) }
    var isDarkMode by remember { mutableStateOf(true) }

    val colorScheme = if (isDarkMode) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.Black,
            primaryContainer = primaryColor.copy(alpha = 0.25f),
            onPrimaryContainer = primaryColor,
            secondary = primaryColor,
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onBackground = Color(0xFFE3E3E3),
            onSurface = Color(0xFFE3E3E3)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = primaryColor,
            secondary = primaryColor,
            background = Color(0xFFF5F7FA),
            surface = Color.White,
            onBackground = Color(0xFF1A1A1E),
            onSurface = Color(0xFF1A1A1E)
        )
    }

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            TimerAppContent(
                primaryColor = primaryColor,
                selectedHue = selectedHue,
                onHueChange = { selectedHue = it },
                isDarkMode = isDarkMode,
                onDarkModeToggle = { isDarkMode = !isDarkMode }
            )
        }
    }
}

@Composable
fun TimerAppContent(
    primaryColor: Color,
    selectedHue: Float,
    onHueChange: (Float) -> Unit,
    isDarkMode: Boolean,
    onDarkModeToggle: () -> Unit
) {
    // Sound Generator safely managed inside the Composable lifecycle
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            null
        }
    }

    var soundEffectsEnabled by remember { mutableStateOf(true) }
    var tickSoundsEnabled by remember { mutableStateOf(true) }

    val triggerSound = { toneType: Int, duration: Int ->
        if (soundEffectsEnabled) {
            try {
                toneGenerator?.startTone(toneType, duration)
            } catch (e: Exception) {
                // Safely ignore failures in audio hardware/emulators
            }
        }
    }

    // Timer States
    var totalTimeMs by remember { mutableStateOf(60000L) } // default 60 seconds
    var remainingTimeMs by remember { mutableStateOf(60000L) }
    var isRunning by remember { mutableStateOf(false) }
    var targetEndTime by remember { mutableStateOf(0L) }
    var pauseTimeMs by remember { mutableStateOf(60000L) }

    // Sound alert logic trigger helper
    var lastTickSecond by remember { mutableStateOf(-1) }

    // Primary Timer Control Loop
    LaunchedEffect(isRunning, targetEndTime) {
        if (isRunning) {
            while (isRunning && remainingTimeMs > 0) {
                val difference = targetEndTime - System.currentTimeMillis()
                if (difference <= 0) {
                    remainingTimeMs = 0
                    isRunning = false
                    triggerSound(ToneGenerator.TONE_CDMA_HIGH_L, 800)
                } else {
                    remainingTimeMs = difference
                    
                    // Tick second changes audio feedback
                    val currentSec = (remainingTimeMs / 1000).toInt()
                    if (currentSec != lastTickSecond) {
                        if (lastTickSecond != -1 && tickSoundsEnabled) {
                            triggerSound(ToneGenerator.TONE_PROP_BEEP, 50)
                        }
                        lastTickSecond = currentSec
                    }
                }
                delay(20) // Tight updates for perfectly smooth UI radial progress
            }
        }
    }

    // Color Theme Animated Transition
    val animatedPrimary = animateColorAsState(targetValue = primaryColor, label = "primaryColorAnim")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ChronoCraft",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = animatedPrimary.value
                )
                Text(
                    text = "Interactive Audio-Color Timer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            
            // Theme Mode Toggle
            IconButton(
                onClick = {
                    triggerSound(ToneGenerator.TONE_PROP_BEEP, 30)
                    onDarkModeToggle()
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape)
            ) {
                Text(
                    text = if (isDarkMode) "☀️" else "🌙",
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Visual Main Timer Canvas Container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(260.dp)
                .shadow(16.dp, CircleShape, clip = false)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(2.dp, animatedPrimary.value.copy(alpha = 0.2f), CircleShape)
        ) {
            // Radial Progress Ring
            Canvas(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                val strokeWidth = 14.dp.toPx()
                // Outer Track
                drawCircle(
                    color = animatedPrimary.value.copy(alpha = 0.12f),
                    style = Stroke(width = strokeWidth)
                )
                // Active Countdown Arc
                val sweepAngle = if (totalTimeMs > 0) {
                    360f * (remainingTimeMs.toFloat() / totalTimeMs.toFloat())
                } else {
                    0f
                }
                drawArc(
                    color = animatedPrimary.value,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Central Time Values display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val totalSecondsRemaining = remainingTimeMs / 1000
                val minutes = totalSecondsRemaining / 60
                val seconds = totalSecondsRemaining % 60
                val millisecondsFraction = (remainingTimeMs % 1000) / 100

                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = String.format(".%ds", millisecondsFraction),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = animatedPrimary.value
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Countdown Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Secondary Reset Button
            Button(
                onClick = {
                    triggerSound(ToneGenerator.TONE_PROP_BEEP, 40)
                    isRunning = false
                    remainingTimeMs = totalTimeMs
                    pauseTimeMs = totalTimeMs
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f)
                    .border(1.5.dp, animatedPrimary.value.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Reset ⏹️",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Primary Start/Pause Button
            Button(
                onClick = {
                    if (isRunning) {
                        triggerSound(ToneGenerator.TONE_PROP_BEEP, 40)
                        isRunning = false
                        pauseTimeMs = remainingTimeMs
                    } else {
                        triggerSound(ToneGenerator.TONE_PROP_BEEP, 60)
                        if (remainingTimeMs <= 0) {
                            remainingTimeMs = totalTimeMs
                        }
                        targetEndTime = System.currentTimeMillis() + remainingTimeMs
                        isRunning = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedPrimary.value,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .height(56.dp)
                    .weight(1.3f)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = if (isRunning) "Pause ⏸️" else "Start ▶️",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Preset Quick Configurations Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Timer Quick Presets",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        "15s" to 15000L,
                        "1m" to 60000L,
                        "3m" to 180000L,
                        "5m" to 300000L,
                        "10m" to 600000L
                    )
                    presets.forEach { (label, duration) ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (totalTimeMs == duration) animatedPrimary.value.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (totalTimeMs == duration) animatedPrimary.value else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    triggerSound(ToneGenerator.TONE_PROP_BEEP, 30)
                                    isRunning = false
                                    totalTimeMs = duration
                                    remainingTimeMs = duration
                                    pauseTimeMs = duration
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalTimeMs == duration) animatedPrimary.value else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Timer Fine Control Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fine Adjustment Slider",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${totalTimeMs / 1000} seconds",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = animatedPrimary.value
                        )
                    )
                }
                
                Slider(
                    value = (totalTimeMs / 1000).toFloat(),
                    onValueChange = { secondsVal ->
                        isRunning = false
                        val calculatedMs = (secondsVal.toLong() * 1000L)
                        totalTimeMs = calculatedMs
                        remainingTimeMs = calculatedMs
                        pauseTimeMs = calculatedMs
                    },
                    valueRange = 5f..1200f, // 5 seconds up to 20 minutes
                    colors = SliderDefaults.colors(
                        thumbColor = animatedPrimary.value,
                        activeTrackColor = animatedPrimary.value,
                        inactiveTrackColor = animatedPrimary.value.copy(alpha = 0.2f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom Color Picker & Theme Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Custom Theme Color Picker",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Slide to dynamically change the app's entire interface color theme and lighting effects.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Continuous Hue Gradient Picker Slider
                val gradientBrush = remember {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF0000), // Red
                            Color(0xFFFF9900), // Orange
                            Color(0xFFFFFF00), // Yellow
                            Color(0xFF00FF00), // Green
                            Color(0xFF00FFFF), // Cyan
                            Color(0xFF0000FF), // Blue
                            Color(0xFF9900FF), // Purple
                            Color(0xFFFF00FF), // Magenta
                            Color(0xFFFF0000)  // Wrap back to Red
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(gradientBrush)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Slider(
                        value = selectedHue,
                        onValueChange = onHueChange,
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Predefined Palette circles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val presetHues = listOf(0f, 35f, 120f, 185f, 220f, 280f, 325f)
                    presetHues.forEach { hue ->
                        val itemColor = colorFromHue(hue)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(itemColor)
                                .border(
                                    width = if (Math.abs(selectedHue - hue) < 12f) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable {
                                    triggerSound(ToneGenerator.TONE_PROP_BEEP, 30)
                                    onHueChange(hue)
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Audio Effects Preferences Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Tone & Sound Feedback Customizer",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Sound FX Switch row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Main Sound FX",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Play tone notifications upon starting, pausing, resetting and final alarm.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = soundEffectsEnabled,
                        onCheckedChange = {
                            soundEffectsEnabled = it
                            if (it) triggerSound(ToneGenerator.TONE_PROP_BEEP, 60)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedPrimary.value,
                            checkedTrackColor = animatedPrimary.value.copy(alpha = 0.3f)
                        )
                    )
                }

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )

                // Tick Sound Switch row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rhythmic Tick Sound",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Play a short system beep sound exactly on every passing second.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = tickSoundsEnabled,
                        onCheckedChange = {
                            tickSoundsEnabled = it
                            if (it) triggerSound(ToneGenerator.TONE_PROP_BEEP, 40)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedPrimary.value,
                            checkedTrackColor = animatedPrimary.value.copy(alpha = 0.3f)
                        )
                    )
                }

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )

                // Audio Output Test trigger button
                Button(
                    onClick = { triggerSound(ToneGenerator.TONE_CDMA_PIP, 120) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Test Sound Generator 🔊",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Brand Footer
        Text(
            text = "Designed & Built with Jetpack Compose",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

// Helper utility to convert a float HSV Hue configuration to standard Compose Color object
fun colorFromHue(hue: Float): Color {
    val hsv = floatArrayOf(hue, 0.82f, 0.95f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}
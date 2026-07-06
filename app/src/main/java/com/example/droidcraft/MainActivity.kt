package com.example.droidcraft

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.floor

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
    // Dynamic Hue Color State (0 - 360)
    var hue by remember { mutableStateOf(195f) } // Default beautiful Cyan/Blue
    var saturation by remember { mutableStateOf(0.85f) }
    
    // Convert HSV to Color
    val pickedColor = remember(hue, saturation) {
        Color.hsv(hue, saturation, 0.95f)
    }
    
    // Sound FX configurations
    var isSoundEnabled by remember { mutableStateOf(true) }
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 75)
        } catch (e: Exception) {
            null
        }
    }

    // Timer States
    var totalTimeMs by remember { mutableStateOf(60000L) } // Default 1 minute
    var timeLeftMs by remember { mutableStateOf(60000L) }
    var isRunning by remember { mutableStateOf(false) }
    
    // Keep track of total time configured separately for progress calculations
    var configuredTimeMs by remember { mutableStateOf(60000L) }

    // Sound FX helper
    val playTone = { toneType: Int, duration: Int ->
        if (isSoundEnabled) {
            try {
                toneGenerator?.startTone(toneType, duration)
            } catch (e: Exception) {
                // Safely ignore failures in sandbox/emulators
            }
        }
    }

    // Dynamic timer ticker logic
    LaunchedEffect(isRunning, timeLeftMs) {
        if (isRunning && timeLeftMs > 0) {
            val startTickTime = System.currentTimeMillis()
            delay(40) // fast updates for smooth dynamic UI
            val elapsed = System.currentTimeMillis() - startTickTime
            
            val previousSecond = floor(timeLeftMs / 1000f).toInt()
            timeLeftMs = (timeLeftMs - elapsed).coerceAtLeast(0L)
            val currentSecond = floor(timeLeftMs / 1000f).toInt()

            // Play clean periodic ticking sound on each elapsed second
            if (currentSecond < previousSecond && timeLeftMs > 0) {
                playTone(ToneGenerator.TONE_CDMA_PIP, 40)
            }
        } else if (isRunning && timeLeftMs <= 0L) {
            isRunning = false
            // Play triumph finish alarm sound pattern
            playTone(ToneGenerator.TONE_CDMA_HIGH_L, 400)
        }
    }

    // Release helper
    DisposableEffect(Unit) {
        onDispose {
            try {
                toneGenerator?.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
    }

    // Material 3 Custom Theme Wrapper based on selected user dynamic color
    val darkColorScheme = darkColorScheme(
        primary = pickedColor,
        secondary = pickedColor.copy(alpha = 0.7f),
        tertiary = pickedColor.copy(alpha = 0.5f),
        background = Color(0xFF0C0E14),
        surface = Color(0xFF161924),
        onPrimary = Color.Black,
        onSurface = Color.White
    )

    MaterialTheme(colorScheme = darkColorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top Beautiful Brand Bar
                AppHeader(pickedColor)

                // Main Timer Card with Visual Radial Glow Indicator
                TimerCard(
                    timeLeftMs = timeLeftMs,
                    totalTimeMs = configuredTimeMs,
                    pickedColor = pickedColor,
                    isRunning = isRunning,
                    onTogglePlay = {
                        if (timeLeftMs <= 0L) {
                            // Reset if auto-finished
                            timeLeftMs = configuredTimeMs
                        }
                        isRunning = !isRunning
                        playTone(ToneGenerator.TONE_CONTAINER, 100)
                    },
                    onReset = {
                        isRunning = false
                        timeLeftMs = configuredTimeMs
                        playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
                    }
                )

                // Quick presets & customizable hours/minutes/seconds
                PresetControlsCard(
                    pickedColor = pickedColor,
                    isRunning = isRunning,
                    onPresetSelect = { presetMs ->
                        isRunning = false
                        configuredTimeMs = presetMs
                        timeLeftMs = presetMs
                        playTone(ToneGenerator.TONE_PROP_BEEP, 80)
                    }
                )

                // Advanced Custom Color Picker Panel
                ColorPickerCard(
                    hue = hue,
                    onHueChange = { hue = it },
                    saturation = saturation,
                    onSaturationChange = { saturation = it },
                    pickedColor = pickedColor
                )

                // Sound and System Options Card
                SettingsCard(
                    isSoundEnabled = isSoundEnabled,
                    onSoundToggle = {
                        isSoundEnabled = it
                        if (isSoundEnabled) {
                            playTone(ToneGenerator.TONE_PROP_BEEP, 120)
                        }
                    },
                    pickedColor = pickedColor
                )
            }
        }
    }
}

@Composable
fun AppHeader(accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(1.5.dp, accentColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "ChronoGlow Icon",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "ChronoGlow",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Dynamic Sound & Colors",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF1E2230))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Active Theme",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TimerCard(
    timeLeftMs: Long,
    totalTimeMs: Long,
    pickedColor: Color,
    isRunning: Boolean,
    onTogglePlay: () -> Unit,
    onReset: () -> Unit
) {
    val progress = remember(timeLeftMs, totalTimeMs) {
        if (totalTimeMs > 0) timeLeftMs.toFloat() / totalTimeMs.toFloat() else 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, ambientColor = pickedColor, spotColor = pickedColor),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161924))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Beautiful Ring & Text
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Background Shadow glow effect
                Canvas(modifier = Modifier.size(220.dp)) {
                    drawCircle(
                        color = pickedColor.copy(alpha = 0.05f),
                        radius = size.minDimension / 2.0f
                    )
                }

                // Smooth Arc Progress Rim
                Canvas(modifier = Modifier.size(200.dp)) {
                    // Base tracking ring
                    drawCircle(
                        color = Color(0xFF232838),
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Active tracking ring
                    drawArc(
                        color = pickedColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Digital Timer Counter inside Ring
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val minutes = (timeLeftMs / 1000) / 60
                    val seconds = (timeLeftMs / 1000) % 60
                    val millis = (timeLeftMs % 1000) / 10

                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    
                    Text(
                        text = String.format(".%02d", millis),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = pickedColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Control Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFF232838), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Play / Pause glowing core button
                Button(
                    onClick = onTogglePlay,
                    colors = ButtonDefaults.buttonColors(containerColor = pickedColor),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(12.dp, shape = CircleShape, ambientColor = pickedColor, spotColor = pickedColor),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Menu else Icons.Default.PlayArrow, // clean visual replacements for play/pause
                        contentDescription = "Play/Pause Timer",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PresetControlsCard(
    pickedColor: Color,
    isRunning: Boolean,
    onPresetSelect: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161924))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Preset Intervals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Rapid Duration Selectors (Row 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    "30 Sec" to 30000L,
                    "1 Min" to 60000L,
                    "3 Min" to 180000L
                )
                presets.forEach { (label, duration) ->
                    PresetButton(
                        label = label,
                        duration = duration,
                        pickedColor = pickedColor,
                        onPresetSelect = onPresetSelect,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Rapid Duration Selectors (Row 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    "5 Min" to 300000L,
                    "10 Min" to 600000L,
                    "25 Min" to 1500000L
                )
                presets.forEach { (label, duration) ->
                    PresetButton(
                        label = label,
                        duration = duration,
                        pickedColor = pickedColor,
                        onPresetSelect = onPresetSelect,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PresetButton(
    label: String,
    duration: Long,
    pickedColor: Color,
    onPresetSelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF232838))
            .border(1.dp, pickedColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable { onPresetSelect(duration) }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ColorPickerCard(
    hue: Float,
    onHueChange: (Float) -> Unit,
    saturation: Float,
    onSaturationChange: (Float) -> Unit,
    pickedColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161924))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Neon Designer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Color Display Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(pickedColor)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            // Hue Slider Container
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Hue Accent Color", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        text = "${hue.toInt()}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = pickedColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Rainbow linear gradient track behind Custom Slider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Red, Color.Yellow, Color.Green,
                                    Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                )
                            )
                        )
                )
                
                Slider(
                    value = hue,
                    onValueChange = onHueChange,
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    modifier = Modifier.offset(y = (-6).dp)
                )
            }

            // Saturation Slider Container
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Color Glow Intensity", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        text = "${(saturation * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = pickedColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Slider(
                    value = saturation,
                    onValueChange = onSaturationChange,
                    valueRange = 0.2f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = pickedColor,
                        activeTrackColor = pickedColor,
                        inactiveTrackColor = Color(0xFF232838)
                    )
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    isSoundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    pickedColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161924))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF232838)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.Default.PlayArrow else Icons.Default.Refresh, // fallback audio states
                        contentDescription = "Sound Status",
                        tint = if (isSoundEnabled) pickedColor else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "System Haptic & Sound FX",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Generates high fidelity synthetic tones",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Switch(
                checked = isSoundEnabled,
                onCheckedChange = onSoundToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = pickedColor,
                    checkedTrackColor = pickedColor.copy(alpha = 0.4f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF232838)
                )
            )
        }
    }
}
package com.example.droidcraft

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

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
    // ----------------------------------------------------
    // State & Colors Setup
    // ----------------------------------------------------
    var red by remember { mutableStateOf(0.3f) }
    var green by remember { mutableStateOf(0.6f) }
    var blue by remember { mutableStateOf(0.9f) }
    
    val selectedColor = Color(red, green, blue)
    val animatedThemeColor by animateColorAsState(
        targetValue = selectedColor,
        animationSpec = tween(durationMillis = 300),
        label = "ThemeColorAnimation"
    )

    // Dynamic Material 3 Scheme mapping
    val isDark = selectedColor.luminance() < 0.4f
    val customColorScheme = lightColorScheme(
        primary = animatedThemeColor,
        onPrimary = if (animatedThemeColor.luminance() > 0.5f) Color.Black else Color.White,
        primaryContainer = animatedThemeColor.copy(alpha = 0.2f),
        onPrimaryContainer = animatedThemeColor,
        surface = if (isDark) Color(0xFF1E1E24) else Color(0xFFF7F9FC),
        onSurface = if (isDark) Color.White else Color(0xFF1A1C1E),
        background = if (isDark) Color(0xFF121214) else Color(0xFFF0F4F8)
    )

    // Preset color templates
    val colorPresets = listOf(
        Color(0xFFE91E63), // Sunset Pink
        Color(0xFF9C27B0), // Royal Purple
        Color(0xFF2196F3), // Vivid Blue
        Color(0xFF4CAF50), // Nature Green
        Color(0xFFFF9800), // Energetic Orange
        Color(0xFF00BCD4), // Cool Cyan
        Color(0xFF3F51B5), // Indigo
        Color(0xFFE91E63)  // Coral Red
    )

    // ----------------------------------------------------
    // Sound Effects Engine
    // ----------------------------------------------------
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            null
        }
    }

    var isSoundEnabled by remember { mutableStateOf(true) }

    fun playSound(toneType: Int, duration: Int) {
        if (isSoundEnabled) {
            try {
                toneGenerator?.startTone(toneType, duration)
            } catch (e: Exception) {
                // Fallback / Silently ignore sound issues if audio buffer is busy
            }
        }
    }

    fun playTickSound() {
        playSound(ToneGenerator.TONE_PROP_BEEP, 60)
    }

    fun playCompleteSound() {
        // Play sequential positive chime sounds
        Thread {
            playSound(ToneGenerator.TONE_CDMA_PIP, 120)
            Thread.sleep(150)
            playSound(ToneGenerator.TONE_CDMA_PIP, 120)
            Thread.sleep(150)
            playSound(ToneGenerator.TONE_CDMA_HIGH_L, 350)
        }.start()
    }

    fun playClickSound() {
        playSound(ToneGenerator.TONE_PROP_ACK, 40)
    }

    // ----------------------------------------------------
    // Dynamic Countdown Timer Logic
    // ----------------------------------------------------
    var totalDurationInSeconds by remember { mutableStateOf(60) }
    var secondsRemaining by remember { mutableStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Synchronize remaining time to custom duration when timer is not active
    LaunchedEffect(totalDurationInSeconds) {
        if (!isTimerRunning) {
            secondsRemaining = totalDurationInSeconds
        }
    }

    // Timer Loop running in standard Compose Side-Effect
    LaunchedEffect(isTimerRunning, secondsRemaining) {
        if (isTimerRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
            if (secondsRemaining > 0) {
                playTickSound()
            } else {
                isTimerRunning = false
                playCompleteSound()
            }
        } else if (secondsRemaining == 0) {
            isTimerRunning = false
        }
    }

    // Progress computation
    val progress = if (totalDurationInSeconds > 0) {
        secondsRemaining.toFloat() / totalDurationInSeconds.toFloat()
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "CircularProgressAnimation"
    )

    // Format helper
    val formattedTime = remember(secondsRemaining) {
        val mins = secondsRemaining / 60
        val secs = secondsRemaining % 60
        String.format("%02d:%02d", mins, secs)
    }

    MaterialTheme(colorScheme = customColorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Block
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "DROIDCRAFT STUDIO",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Dynamic Spark Timer",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // ----------------------------------------------------
                // Countdown Circle Visualization
                // ----------------------------------------------------
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(260.dp)
                        .shadow(16.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    // Draw custom dynamic circular dial
                    val ringColor = MaterialTheme.colorScheme.primary
                    val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Track Arc
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Active Arc
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Display text details inside
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTimerRunning) "RUNNING" else "PAUSED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${((progress) * 100).roundToInt()}% Remaining",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ----------------------------------------------------
                // Main Timer Control Board
                // ----------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start / Pause Floating-style Action Button
                    Button(
                        onClick = {
                            playClickSound()
                            isTimerRunning = !isTimerRunning
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .width(160.dp)
                            .shadow(8.dp, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = if (isTimerRunning) "Pause" else "Start Timer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Reset Button
                    FilledTonalButton(
                        onClick = {
                            playClickSound()
                            isTimerRunning = false
                            secondsRemaining = totalDurationInSeconds
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(text = "Reset", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Custom Slider to change default Duration
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Set Timer Duration",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val displayMin = totalDurationInSeconds / 60
                            val displaySec = totalDurationInSeconds % 60
                            Text(
                                text = "${displayMin}m ${displaySec}s",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = totalDurationInSeconds.toFloat(),
                            onValueChange = { newValue ->
                                if (!isTimerRunning) {
                                    totalDurationInSeconds = newValue.roundToInt()
                                }
                            },
                            valueRange = 10f..600f,
                            steps = 59,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                            )
                        )
                        // Preset Quick Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30, 60, 180, 300, 600).forEach { presetSeconds ->
                                val label = when {
                                    presetSeconds < 60 -> "${presetSeconds}s"
                                    else -> "${presetSeconds / 60}m"
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (totalDurationInSeconds == presetSeconds)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                        .clickable {
                                            playClickSound()
                                            if (!isTimerRunning) {
                                                totalDurationInSeconds = presetSeconds
                                            }
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (totalDurationInSeconds == presetSeconds)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ----------------------------------------------------
                // Custom Color Picker & Theme Customizer
                // ----------------------------------------------------
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Theme Color Picker",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // RGB Sliders for Custom Palette Generation
                        // RED
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("R", color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.width(18.dp))
                            Slider(
                                value = red,
                                onValueChange = { red = it },
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red.copy(alpha = 0.5f))
                            )
                            Text(text = (red * 255).roundToInt().toString(), modifier = Modifier.width(32.dp), textAlign = TextAlign.End, fontSize = 12.sp)
                        }
                        // GREEN
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.width(18.dp))
                            Slider(
                                value = green,
                                onValueChange = { green = it },
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF2E7D32), activeTrackColor = Color(0xFF2E7D32).copy(alpha = 0.5f))
                            )
                            Text(text = (green * 255).roundToInt().toString(), modifier = Modifier.width(32.dp), textAlign = TextAlign.End, fontSize = 12.sp)
                        }
                        // BLUE
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("B", color = Color.Blue, fontWeight = FontWeight.Bold, modifier = Modifier.width(18.dp))
                            Slider(
                                value = blue,
                                onValueChange = { blue = it },
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue.copy(alpha = 0.5f))
                            )
                            Text(text = (blue * 255).roundToInt().toString(), modifier = Modifier.width(32.dp), textAlign = TextAlign.End, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Select Preset Circles
                        Text(
                            text = "Preset Palettes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            colorPresets.forEach { colorPreset ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .shadow(2.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(colorPreset)
                                        .clickable {
                                            playClickSound()
                                            red = colorPreset.red
                                            green = colorPreset.green
                                            blue = colorPreset.blue
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ----------------------------------------------------
                // Sound Effects Controller
                // ----------------------------------------------------
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tick Sound Effects",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Plays physical beep every second",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = {
                                isSoundEnabled = it
                                playClickSound()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
                
                // Branding footer
                Text(
                    text = "Crafted beautifully using Jetpack Compose",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
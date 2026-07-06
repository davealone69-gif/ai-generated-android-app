package com.example.droidcraft

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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

// Memory-safe wrapper for Android ToneGenerator
class SoundEffectsPlayer {
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            toneGen = null
        }
    }

    fun playTick() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun playComplete() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 400)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun playClick() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 50)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun playAlert() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun release() {
        toneGen?.release()
    }
}

// Preset Colors data structure
data class ColorPreset(val name: String, val r: Int, val g: Int, val b: Int)

@Composable
fun MainAppScreen() {
    // Custom color state (RGB)
    var r by remember { mutableStateOf(103) } // Indigo Default
    var g by remember { mutableStateOf(80) }
    var b by remember { mutableStateOf(164) }

    val themeColor = Color(r, g, b)
    
    // Animated theme color transitions for dynamic UI responsiveness
    val animatedThemeColor by animateColorAsState(
        targetValue = themeColor,
        animationSpec = tween(durationMillis = 400),
        label = "ThemeColorAnimation"
    )

    // Material 3 Dynamic Dark Palette based on picked color
    val customColorScheme = darkColorScheme(
        primary = animatedThemeColor,
        onPrimary = Color.White,
        secondary = animatedThemeColor.copy(alpha = 0.8f),
        onSecondary = Color.White,
        tertiary = animatedThemeColor.copy(alpha = 0.6f),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        surfaceVariant = Color(0xFF282828),
        onBackground = Color(0xFFE3E3E3),
        onSurface = Color(0xFFE3E3E3)
    )

    // Sound manager setup with safe lifecycle release
    val soundPlayer = remember { SoundEffectsPlayer() }
    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.release()
        }
    }

    // Timer state variables (Default value: 60 seconds)
    var totalTimeSeconds by remember { mutableStateOf(60) }
    var secondsLeft by remember { mutableStateOf(60) }
    var isRunning by remember { mutableStateOf(false) }
    var enableTickSound by remember { mutableStateOf(true) }

    // Logic for running countdown timer
    LaunchedEffect(key1 = isRunning, key2 = secondsLeft) {
        if (isRunning && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
            if (enableTickSound && secondsLeft > 0) {
                soundPlayer.playTick()
            }
            if (secondsLeft == 0) {
                isRunning = false
                soundPlayer.playComplete()
            }
        } else if (secondsLeft == 0) {
            isRunning = false
        }
    }

    // UI Formatting Helper
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val minStr = if (minutes < 10) "0$minutes" else "$minutes"
        val secStr = if (remainingSeconds < 10) "0$remainingSeconds" else "$remainingSeconds"
        return "$minStr:$secStr"
    }

    // Preset options
    val colorPresets = listOf(
        ColorPreset("Indigo", 103, 80, 164),
        ColorPreset("Sunset", 230, 81, 0),
        ColorPreset("Emerald", 46, 125, 50),
        ColorPreset("Teal Glow", 0, 150, 136),
        ColorPreset("Crimson", 198, 40, 40),
        ColorPreset("Neon Blue", 33, 150, 243)
    )

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
                // Header
                Text(
                    text = "DURA-COUNT",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )

                Text(
                    text = "Interactive Countdown & Dynamic Theme Engine",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // TIMER INTERFACE CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "COUNTDOWN TIMER",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Circular Progress Tracker
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(200.dp)
                        ) {
                            val progressFactor = if (totalTimeSeconds > 0) {
                                secondsLeft.toFloat() / totalTimeSeconds.toFloat()
                            } else {
                                1f
                            }
                            CircularProgressIndicator(
                                progress = progressFactor,
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 10.dp,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )

                            // Timer Numeric String
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formatTime(secondsLeft),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "REMAINING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Primary Control Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reset Button
                            OutlinedButton(
                                onClick = {
                                    soundPlayer.playClick()
                                    isRunning = false
                                    secondsLeft = totalTimeSeconds
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RESET")
                            }

                            // Play / Pause Button
                            Button(
                                onClick = {
                                    soundPlayer.playClick()
                                    if (secondsLeft <= 0) {
                                        secondsLeft = totalTimeSeconds
                                    }
                                    isRunning = !isRunning
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = if (isRunning) "Pause" else "Play"
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isRunning) "PAUSE" else "START")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick duration setup options
                        Text(
                            text = "Quick Presets",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(30, 60, 120, 300, 600).forEach { sec ->
                                val label = when {
                                    sec < 60 -> "${sec}s"
                                    else -> "${sec / 60}m"
                                }
                                SuggestionChip(
                                    onClick = {
                                        soundPlayer.playClick()
                                        totalTimeSeconds = sec
                                        secondsLeft = sec
                                        isRunning = false
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Continuous duration configuration slider
                        Text(
                            text = "Fine Adjustment: ${formatTime(totalTimeSeconds)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Slider(
                            value = totalTimeSeconds.toFloat(),
                            onValueChange = { value ->
                                val roundedValue = (value.toInt() / 5) * 5 // snap to 5s increments
                                totalTimeSeconds = if (roundedValue < 5) 5 else roundedValue
                                if (!isRunning) {
                                    secondsLeft = totalTimeSeconds
                                }
                            },
                            valueRange = 5f..1800f, // 5 seconds up to 30 mins
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // DYNAMIC COLOR ENGINE CARD (Custom Color Picker)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "THEME DESIGN ENGINE",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Mix RGB elements dynamically or select a palette signature to alter the app's visual identity instantly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Dynamic Color Preview Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .background(animatedThemeColor)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Active Signature Color",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                val hexString = String.format("#%02X%02X%02X", r, g, b)
                                Text(
                                    text = "RGB($r, $g, $b) • $hexString",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Preset quick selector
                        Text(
                            text = "Designer Palettes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // First 3 presets
                            colorPresets.take(3).forEach { preset ->
                                val isSelected = r == preset.r && g == preset.g && b == preset.b
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            soundPlayer.playClick()
                                            r = preset.r
                                            g = preset.g
                                            b = preset.b
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(preset.r, preset.g, preset.b))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = preset.name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Last 3 presets
                            colorPresets.drop(3).take(3).forEach { preset ->
                                val isSelected = r == preset.r && g == preset.g && b == preset.b
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            soundPlayer.playClick()
                                            r = preset.r
                                            g = preset.g
                                            b = preset.b
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(preset.r, preset.g, preset.b))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = preset.name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // RGB Sliders for Custom Color Manipulation
                        Text(
                            text = "RGB Custom Sliders",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Red Slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "R",
                                color = Color(239, 83, 80),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(20.dp)
                            )
                            Slider(
                                value = r.toFloat(),
                                onValueChange = { r = it.toInt() },
                                valueRange = 0f..255f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(239, 83, 80),
                                    activeTrackColor = Color(239, 83, 80)
                                )
                            )
                            Text(
                                text = r.toString(),
                                modifier = Modifier.width(35.dp),
                                textAlign = TextAlign.End,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Green Slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "G",
                                color = Color(102, 187, 106),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(20.dp)
                            )
                            Slider(
                                value = g.toFloat(),
                                onValueChange = { g = it.toInt() },
                                valueRange = 0f..255f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(102, 187, 106),
                                    activeTrackColor = Color(102, 187, 106)
                                )
                            )
                            Text(
                                text = g.toString(),
                                modifier = Modifier.width(35.dp),
                                textAlign = TextAlign.End,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Blue Slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "B",
                                color = Color(66, 165, 245),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(20.dp)
                            )
                            Slider(
                                value = b.toFloat(),
                                onValueChange = { b = it.toInt() },
                                valueRange = 0f..255f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(66, 165, 245),
                                    activeTrackColor = Color(66, 165, 245)
                                )
                            )
                            Text(
                                text = b.toString(),
                                modifier = Modifier.width(35.dp),
                                textAlign = TextAlign.End,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // SOUND CONTROL CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "AUDIO CONTROLLER",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Audio Configuration Toggle Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rhythmic Second Tick",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Produces high frequency beep on every second of count",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = enableTickSound,
                                onCheckedChange = { enableTickSound = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                        Spacer(modifier = Modifier.height(20.dp))

                        // Manual Sound Trigger Matrix (Auditory Board)
                        Text(
                            text = "Sound Library Test Board",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { soundPlayer.playTick() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Tick", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { soundPlayer.playClick() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Click", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { soundPlayer.playAlert() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Alert", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { soundPlayer.playComplete() },
                                modifier = Modifier.weight(1.f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Complete", fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
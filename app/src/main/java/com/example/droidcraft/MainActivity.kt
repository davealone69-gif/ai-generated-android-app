package com.example.droidcraft

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlin.math.PI
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainAppScreen()
        }
    }
}

// Programmatic synthesizer for audio effects without needing local resources
object SoundSynth {
    fun playTone(frequency: Double, durationMs: Int) {
        Thread {
            try {
                val sampleRate = 44100
                val numSamples = (durationMs * sampleRate / 1000)
                val sample = DoubleArray(numSamples)
                val generatedSnd = ByteArray(2 * numSamples)

                // Fill sample array with a pure sine wave
                for (i in 0 until numSamples) {
                    sample[i] = sin(2 * PI * i / (sampleRate / frequency))
                }

                // Convert to 16-bit PCM with fade-out envelope to avoid clicks
                var idx = 0
                for (i in 0 until numSamples) {
                    val dVal = sample[i]
                    val fadeFactor = if (i > numSamples * 0.8) {
                        (numSamples - i).toDouble() / (numSamples * 0.2)
                    } else {
                        1.0
                    }
                    val valShort = (dVal * 32767 * fadeFactor).toInt().toShort()
                    generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
                }

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    generatedSnd.size,
                    AudioTrack.MODE_STATIC
                )
                audioTrack.write(generatedSnd, 0, generatedSnd.size)
                audioTrack.play()
                Thread.sleep(durationMs.toLong() + 30)
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun playTick() {
        playTone(1000.0, 35)
    }

    fun playAlert() {
        Thread {
            playTone(880.0, 150)
            Thread.sleep(180)
            playTone(1100.0, 150)
            Thread.sleep(180)
            playTone(1320.0, 350)
        }.start()
    }

    fun playTap() {
        playTone(600.0, 40)
    }
}

@Composable
fun MainAppScreen() {
    // Custom Color State
    var redValue by remember { mutableStateOf(103f) }
    var greenValue by remember { mutableStateOf(80f) }
    var blueValue by remember { mutableStateOf(164f) }

    val activeColor = Color(
        red = redValue.toInt().coerceIn(0, 255),
        green = greenValue.toInt().coerceIn(0, 255),
        blue = blueValue.toInt().coerceIn(0, 255)
    )

    val animatedColor by animateColorAsState(targetValue = activeColor, label = "ThemeColorChange")

    // Timer States
    var initialSeconds by remember { mutableStateOf(60) }
    var secondsRemaining by remember { mutableStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var enableSoundTicks by remember { mutableStateOf(true) }

    // Color picker pre-sets
    val presets = listOf(
        Color(0xFFE91E63) to "Rose",
        Color(0xFF9C27B0) to "Orchid",
        Color(0xFF3F51B5) to "Indigo",
        Color(0xFF00BCD4) to "Cyan",
        Color(0xFF4CAF50) to "Emerald",
        Color(0xFFFF9800) to "Amber"
    )

    // Countdown logic
    LaunchedEffect(isTimerRunning, secondsRemaining) {
        if (isTimerRunning && secondsRemaining > 0) {
            delay(1000)
            secondsRemaining -= 1
            if (enableSoundTicks && secondsRemaining > 0) {
                SoundSynth.playTick()
            }
            if (secondsRemaining == 0) {
                isTimerRunning = false
                SoundSynth.playAlert()
            }
        } else if (secondsRemaining == 0) {
            isTimerRunning = false
        }
    }

    // Material 3 App Container with local styling
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = animatedColor,
            secondary = animatedColor.copy(alpha = 0.8f),
            surface = Color(0xFF121214)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Title Header
                Text(
                    text = "AURA CHRONO",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    color = animatedColor,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )

                Text(
                    text = "Dynamic Timer & Color Synthesizer",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Main Circular Countdown Widget
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp)
                ) {
                    val progress = if (initialSeconds > 0) {
                        secondsRemaining.toFloat() / initialSeconds.toFloat()
                    } else {
                        0f
                    }
                    val animatedProgress by animateFloatAsState(targetValue = progress, label = "ProgressArc")

                    // Inner radial background glow
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        animatedColor.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Track Ring
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFF232328),
                            radius = size.minDimension / 2.2f,
                            style = Stroke(width = 10.dp.toPx())
                        )
                    }

                    // Active Glowing Progress Ring
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = animatedColor,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Digital Clock Timer Display
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val minutes = secondsRemaining / 60
                        val secs = secondsRemaining % 60
                        val timerText = String.format("%02d:%02d", minutes, secs)

                        Text(
                            text = timerText,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTimerRunning) "RUNNING" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isTimerRunning) animatedColor else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Timer Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    Button(
                        onClick = {
                            SoundSynth.playTap()
                            isTimerRunning = false
                            secondsRemaining = initialSeconds
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E22)),
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Main Play/Pause Accent Button
                    Button(
                        onClick = {
                            SoundSynth.playTap()
                            if (secondsRemaining <= 0) {
                                secondsRemaining = initialSeconds
                            }
                            isTimerRunning = !isTimerRunning
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = animatedColor),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .height(60.dp)
                            .width(140.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "PlayPause",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTimerRunning) "PAUSE" else "START",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Toggle Sound Button
                    Button(
                        onClick = {
                            SoundSynth.playTap()
                            enableSoundTicks = !enableSoundTicks
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (enableSoundTicks) animatedColor.copy(alpha = 0.2f) else Color(0xFF1E1E22)
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Sound Configuration",
                            tint = if (enableSoundTicks) animatedColor else Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Time Presets & Custom Multipliers
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF19191C)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "QUICK TIME PRESETS",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(10, 30, 60, 120, 300).forEach { seconds ->
                                val mins = seconds / 60
                                val label = if (mins > 0) "${mins}m" else "${seconds}s"
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (initialSeconds == seconds) animatedColor.copy(alpha = 0.25f)
                                            else Color(0xFF232328)
                                        )
                                        .clickable {
                                            SoundSynth.playTap()
                                            initialSeconds = seconds
                                            secondsRemaining = seconds
                                            isTimerRunning = false
                                        }
                                        .padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (initialSeconds == seconds) animatedColor else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Aura Color Picker
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF19191C)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CHROME AURA ENGINE (RGB)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Red Slider
                        ColorSliderRow(
                            label = "R",
                            value = redValue,
                            colorTint = Color.Red,
                            onValueChange = { redValue = it }
                        )

                        // Green Slider
                        ColorSliderRow(
                            label = "G",
                            value = greenValue,
                            colorTint = Color.Green,
                            onValueChange = { greenValue = it }
                        )

                        // Blue Slider
                        ColorSliderRow(
                            label = "B",
                            value = blueValue,
                            colorTint = Color.Blue,
                            onValueChange = { blueValue = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Preset Color Swatches
                        Text(
                            text = "PRESET AURAS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            presets.forEach { (color, name) ->
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable {
                                            SoundSynth.playTap()
                                            redValue = color.red * 255f
                                            greenValue = color.green * 255f
                                            blueValue = color.blue * 255f
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer audio test area
                Text(
                    text = "Tap presets or slide variables to customize sound synthesis & colors. Sounds are generated in real-time through mathematical wave models.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ColorSliderRow(
    label: String,
    value: Float,
    colorTint: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = colorTint,
            modifier = Modifier.width(24.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = colorTint,
                activeTrackColor = colorTint.copy(alpha = 0.6f),
                inactiveTrackColor = Color(0xFF2E2E34)
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.toInt().toString(),
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}
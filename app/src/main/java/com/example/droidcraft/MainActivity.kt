package com.example.droidcraft

import android.os.Bundle
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

// Programmatic Sound Effects Manager using ToneGenerator to avoid asset dependencies
object ProgrammaticSoundEffects {
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            toneGen = null
        }
    }

    fun playTick() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (_: Exception) {}
    }

    fun playComplete() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 400)
        } catch (_: Exception) {}
    }

    fun playAlert() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250)
        } catch (_: Exception) {}
    }

    fun playClick() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 40)
        } catch (_: Exception) {}
    }
}

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
    // Custom Color State (RGB)
    var redValue by remember { mutableStateOf(120f) }
    var greenValue by remember { mutableStateOf(90f) }
    var blueValue by remember { mutableStateOf(245f) }
    
    val pickedColor = Color(
        red = redValue.toInt().coerceIn(0, 255),
        green = greenValue.toInt().coerceIn(0, 255),
        blue = blueValue.toInt().coerceIn(0, 255)
    )

    // Animated color transition for smooth visual feedback
    val animatedThemeColor by animateColorAsState(
        targetValue = pickedColor,
        animationSpec = tween(durationMillis = 300)
    )

    // Timer States
    var totalSeconds by remember { mutableStateOf(60) }
    var secondsRemaining by remember { mutableStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Audio options
    var isTickSoundEnabled by remember { mutableStateOf(true) }
    var isChimeSoundEnabled by remember { mutableStateOf(true) }

    // Synchronize total duration adjustment when not running
    LaunchedEffect(totalSeconds) {
        if (!isTimerRunning) {
            secondsRemaining = totalSeconds
        }
    }

    // Dynamic Timer Countdown Loop
    LaunchedEffect(isTimerRunning, secondsRemaining) {
        if (isTimerRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
            if (isTickSoundEnabled && secondsRemaining > 0) {
                ProgrammaticSoundEffects.playTick()
            }
        } else if (isTimerRunning && secondsRemaining == 0) {
            isTimerRunning = false
            if (isChimeSoundEnabled) {
                ProgrammaticSoundEffects.playComplete()
            }
        }
    }

    // Pulse Scale animation when running
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isTimerRunning) 1.04f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Layout structure wrapping standard Dark/Dynamic M3 Theme
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = animatedThemeColor,
            onPrimary = Color.Black,
            surface = Color(0xFF1E1E2E),
            background = Color(0xFF12121A),
            primaryContainer = animatedThemeColor.copy(alpha = 0.2f),
            secondary = animatedThemeColor.copy(alpha = 0.8f)
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Header Area
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "App Icon",
                        tint = animatedThemeColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DroidCraft Chrono",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.White
                    )
                }
                Text(
                    text = "Dynamic Timer & Color Synthesizer",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Beautiful Circular Progress Visualizer Card
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(240.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    animatedThemeColor.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    // Custom Draw Canvas for smooth Ring Progress
                    Canvas(modifier = Modifier.size(190.dp)) {
                        // Track Arc
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            style = Stroke(width = 14f)
                        )
                        // Progress Arc
                        val sweepAngle = if (totalSeconds > 0) {
                            (secondsRemaining.toFloat() / totalSeconds.toFloat()) * 360f
                        } else {
                            0f
                        }
                        drawArc(
                            color = animatedThemeColor,
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 14f, cap = StrokeCap.Round)
                        )
                    }

                    // Numeric Countdown details inside Ring
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val minutes = secondsRemaining / 60
                        val seconds = secondsRemaining % 60
                        val timerString = String.format("%02d:%02d", minutes, seconds)

                        Text(
                            text = timerString,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 42.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White
                        )
                        Text(
                            text = if (isTimerRunning) "RUNNING" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = animatedThemeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick presets & Custom adjusting Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            ProgrammaticSoundEffects.playClick()
                            if (secondsRemaining > 10) secondsRemaining -= 10 else secondsRemaining = 0
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    ) {
                        Text("-10s")
                    }
                    Button(
                        onClick = {
                            ProgrammaticSoundEffects.playClick()
                            secondsRemaining += 10
                            totalSeconds = maxOf(totalSeconds, secondsRemaining)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    ) {
                        Text("+10s")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    FilledIconButton(
                        onClick = {
                            ProgrammaticSoundEffects.playClick()
                            isTimerRunning = false
                            secondsRemaining = totalSeconds
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Big Start / Pause Play Toggle button
                    Button(
                        onClick = {
                            ProgrammaticSoundEffects.playClick()
                            if (secondsRemaining == 0) {
                                secondsRemaining = totalSeconds
                            }
                            isTimerRunning = !isTimerRunning
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedThemeColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .width(160.dp)
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTimerRunning) "PAUSE" else "START",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: Custom Color Synthesizer Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Palette icon",
                                tint = animatedThemeColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Color Synthesizer",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Mix custom RGB channels to dynamically restyle the entire app layout interface instantly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Red Slider Control
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "R",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(20.dp)
                            )
                            Slider(
                                value = redValue,
                                onValueChange = { redValue = it },
                                valueRange = 0f..255f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Red,
                                    activeTrackColor = Color.Red.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = redValue.toInt().toString(),
                                color = Color.White,
                                modifier = Modifier.width(32.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        // Green Slider Control
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "G",
                                color = Color.Green,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(20.dp)
                            )
                            Slider(
                                value = greenValue,
                                onValueChange = { greenValue = it },
                                valueRange = 0f..255f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Green,
                                    activeTrackColor = Color.Green.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = greenValue.toInt().toString(),
                                color = Color.White,
                                modifier = Modifier.width(32.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        // Blue Slider Control
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "B",
                                color = Color(0xFF4169E1),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(20.dp)
                            )
                            Slider(
                                value = blueValue,
                                onValueChange = { blueValue = it },
                                valueRange = 0f..255f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF4169E1),
                                    activeTrackColor = Color(0xFF4169E1).copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = blueValue.toInt().toString(),
                                color = Color.White,
                                modifier = Modifier.width(32.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Palette Preset Quick-Pick Chips
                        Text(
                            text = "Preset Swatches:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val presets = listOf(
                            Triple(255f, 94f, 94f),    // Sunburst Red
                            Triple(46f, 204f, 113f),   // Emerald Green
                            Triple(52f, 152f, 219f),   // Ocean Blue
                            Triple(155f, 89f, 182f),   // Royal Purple
                            Triple(255f, 0f, 127f)     // Neon Pink
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            presets.forEach { (r, g, b) ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(r.toInt(), g.toInt(), b.toInt()))
                                        .clickable {
                                            ProgrammaticSoundEffects.playClick()
                                            redValue = r
                                            greenValue = g
                                            blueValue = b
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Sounds Configuration & Sound Board Testing Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Audio Icon",
                                tint = animatedThemeColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Chime & Sound Customization",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Audio Settings Toggle Rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Play tick on each second elapsed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                            Switch(
                                checked = isTickSoundEnabled,
                                onCheckedChange = { isTickSoundEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = animatedThemeColor,
                                    checkedTrackColor = animatedThemeColor.copy(alpha = 0.4f)
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Trigger sound upon completion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                            Switch(
                                checked = isChimeSoundEnabled,
                                onCheckedChange = { isChimeSoundEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = animatedThemeColor,
                                    checkedTrackColor = animatedThemeColor.copy(alpha = 0.4f)
                                )
                            )
                        }

                        Divider(
                            color = Color.White.copy(alpha = 0.08f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // Sound test trigger board
                        Text(
                            text = "Manual Soundboard Tester:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { ProgrammaticSoundEffects.playTick() },
                                colors = ButtonDefaults.tonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                            ) {
                                Text("Tick", color = Color.White, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { ProgrammaticSoundEffects.playAlert() },
                                colors = ButtonDefaults.tonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                            ) {
                                Text("Alert", color = Color.White, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { ProgrammaticSoundEffects.playComplete() },
                                colors = ButtonDefaults.tonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                            ) {
                                Text("Chime", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
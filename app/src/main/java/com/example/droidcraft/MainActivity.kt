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
    // Tone Generator for sound effects
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            null
        }
    }

    // Clean up sound resources on disposal
    DisposableEffect(Unit) {
        onDispose {
            toneGenerator?.release()
        }
    }

    // Custom Color State (RGB)
    var redChannel by remember { mutableFloatStateOf(0.29f) }
    var greenChannel by remember { mutableFloatStateOf(0.56f) }
    var blueChannel by remember { mutableFloatStateOf(0.89f) }

    val pickedColor = Color(redChannel, greenChannel, blueChannel)
    val animatedPickedColor by animateColorAsState(targetValue = pickedColor, label = "PickedColorAnimation")

    // Ambient theme background (very dark tinted version of selected color)
    val ambientBgColor = Color(
        redChannel * 0.12f,
        greenChannel * 0.12f,
        blueChannel * 0.12f,
        1f
    )
    val animatedBgColor by animateColorAsState(targetValue = ambientBgColor, label = "BgColorAnimation")

    // Timer States
    var totalDurationMillis by remember { mutableLongStateOf(60000L) }
    var remainingTimeMillis by remember { mutableLongStateOf(60000L) }
    var isRunning by remember { mutableStateOf(false) }
    var isSoundEnabled by remember { mutableStateOf(true) }

    // Audio functions
    val playTick = {
        if (isSoundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    val playAlert = {
        if (isSoundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    val playClick = {
        if (isSoundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 25)
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    // Countdown Timer Loop Effect
    LaunchedEffect(isRunning, totalDurationMillis) {
        if (isRunning) {
            var lastLoggedSecond = remainingTimeMillis / 1000
            val tickInterval = 40L // Tick every 40ms for high visual precision

            while (isRunning && remainingTimeMillis > 0) {
                delay(tickInterval)
                remainingTimeMillis = (remainingTimeMillis - tickInterval).coerceAtLeast(0L)

                val currentSecond = remainingTimeMillis / 1000
                if (currentSecond < lastLoggedSecond) {
                    if (currentSecond > 0) {
                        playTick()
                    }
                    lastLoggedSecond = currentSecond
                }
            }

            if (remainingTimeMillis <= 0) {
                isRunning = false
                playAlert()
            }
        }
    }

    // Percentage for Progress Arc
    val progressFraction = if (totalDurationMillis > 0) {
        (remainingTimeMillis.toFloat() / totalDurationMillis.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    // Scroll state for accessibility
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "App Icon",
                            tint = animatedPickedColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ChronoCraft",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSoundEnabled = !isSoundEnabled
                        playClick()
                    }) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Sound",
                            tint = if (isSoundEnabled) animatedPickedColor else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = animatedBgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // TIMER CONTAINER CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(16.dp, shape = RoundedCornerShape(32.dp))
                    .background(Color(0xFF15151A), RoundedCornerShape(32.dp))
                    .border(1.dp, animatedPickedColor.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Canvas visual elements
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Inner glowing background ring
                    drawArc(
                        color = animatedPickedColor.copy(alpha = 0.05f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Dynamic Active Countdown Arc
                    drawArc(
                        color = animatedPickedColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progressFraction,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inside Arc: Dynamic Digits
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val minutes = (remainingTimeMillis / 1000) / 60
                    val seconds = (remainingTimeMillis / 1000) % 60
                    val centiseconds = (remainingTimeMillis % 1000) / 10

                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    )

                    Text(
                        text = String.format(".%02d", centiseconds),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = animatedPickedColor
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isRunning) "BURNING TIME" else "READY",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray,
                        letterSpacing = 2.sp
                    )
                }
            }

            // CONTROLS BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = {
                        playClick()
                        isRunning = false
                        remainingTimeMillis = totalDurationMillis
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF23232A), CircleShape)
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = Color.LightGray
                    )
                }

                // Play / Pause Button
                Button(
                    onClick = {
                        playClick()
                        isRunning = !isRunning
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = animatedPickedColor,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .height(64.dp)
                        .width(160.dp)
                        .shadow(8.dp, CircleShape),
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "PAUSE" else "START",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Add Time Quick Action (+15s)
                IconButton(
                    onClick = {
                        playClick()
                        val newTotal = totalDurationMillis + 15000L
                        totalDurationMillis = newTotal
                        remainingTimeMillis = remainingTimeMillis + 15000L
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF23232A), CircleShape)
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add 15 seconds",
                        tint = Color.LightGray
                    )
                }
            }

            // TIMER PRESETS PANEL
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15151A)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(30, 60, 180, 300).forEach { seconds ->
                            val isSelected = totalDurationMillis == seconds * 1000L
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) animatedPickedColor else Color(
                                            0xFF22222B
                                        )
                                    )
                                    .clickable {
                                        playClick()
                                        isRunning = false
                                        totalDurationMillis = seconds * 1000L
                                        remainingTimeMillis = seconds * 1000L
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (seconds >= 60) "${seconds / 60}m" else "${seconds}s",
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // CUSTOM COLOR PICKER CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15151A)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom Theme Color",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp, 20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(animatedPickedColor)
                        )
                    }

                    // Red Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Red", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                text = (redChannel * 255).toInt().toString(),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Slider(
                            value = redChannel,
                            onValueChange = { redChannel = it },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Red,
                                activeTrackColor = Color.Red.copy(alpha = 0.8f),
                                inactiveTrackColor = Color.Red.copy(alpha = 0.15f)
                            )
                        )
                    }

                    // Green Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Green", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                text = (greenChannel * 255).toInt().toString(),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Slider(
                            value = greenChannel,
                            onValueChange = { greenChannel = it },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Green,
                                activeTrackColor = Color.Green.copy(alpha = 0.8f),
                                inactiveTrackColor = Color.Green.copy(alpha = 0.15f)
                            )
                        )
                    }

                    // Blue Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Blue", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                text = (blueChannel * 255).toInt().toString(),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Slider(
                            value = blueChannel,
                            onValueChange = { blueChannel = it },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF007BFF),
                                activeTrackColor = Color(0xFF007BFF).copy(alpha = 0.8f),
                                inactiveTrackColor = Color(0xFF007BFF).copy(alpha = 0.15f)
                            )
                        )
                    }

                    // Color Palettes Quick Picks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Color(0xFFFF5252), // Passion Red
                            Color(0xFFFF9F0A), // Sunrise Orange
                            Color(0xFFFFCC00), // Vibrant Yellow
                            Color(0xFF34C759), // Green
                            Color(0xFF007AFF), // Blue
                            Color(0xFFAF52DE)  // Purple
                        ).forEach { paletteColor ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(paletteColor)
                                    .border(
                                        width = if (pickedColor == paletteColor) 2.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        playClick()
                                        redChannel = paletteColor.red
                                        greenChannel = paletteColor.green
                                        blueChannel = paletteColor.blue
                                    }
                            )
                        }
                    }
                }
            }

            // Info and instructions
            Text(
                text = "Tap presets, customize elements, and experience the smooth generated synthesized ticks.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
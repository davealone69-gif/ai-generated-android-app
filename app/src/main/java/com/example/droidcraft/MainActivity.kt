package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoMakerStudio()
                }
            }
        }
    }
}

@Composable
fun VideoMakerStudio() {
    var projectName by remember { mutableStateOf("New AU Project") }
    var isProcessing by remember { mutableStateOf(false) }
    val projectSettings = remember { mutableStateListOf("Realistic Rendering", "High Frame Rate", "4K Output") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AU Video Maker Studio",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = projectName,
            onValueChange = { projectName = it },
            label = { Text("Project Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Settings:", style = MaterialTheme.typography.titleMedium)

        LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
            items(projectSettings.size) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = projectSettings[index],
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        Button(
            onClick = { isProcessing = !isProcessing },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (isProcessing) "Generating Video..." else "Start Realistic Rendering")
        }

        if (isProcessing) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}
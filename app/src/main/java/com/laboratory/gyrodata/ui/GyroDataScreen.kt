package com.laboratory.gyrodata.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SensorWindow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun GyroDataScreen(
    roll: Float,
    pitch: Float,
    yaw: Float,
    bleActive: Boolean,
    onRefresh: () -> Unit,
    onThemeSelected: (String) -> Unit
) {
    // State for showing the theme selection dialog.
    var showThemeDialog by remember { mutableStateOf(false) }

    // Blinking dot for BLE status.
    val infiniteTransition = rememberInfiniteTransition()
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Helper functions to choose icons based on sensor value.
    fun rollIcon(value: Float): ImageVector =
        when {
            value < -1f -> Icons.Default.ArrowBack
            value > 1f -> Icons.Default.ArrowForward
            else -> Icons.Default.SensorWindow
        }

    fun pitchIcon(value: Float): ImageVector =
        when {
            value < -0.5f -> Icons.Default.KeyboardArrowDown
            value > 0.5f -> Icons.Default.KeyboardArrowUp
            else -> Icons.Default.SensorWindow
        }

    fun yawIcon(value: Float): ImageVector =
        when {
            value < -0.5f -> Icons.Default.ArrowBack
            value > 0.5f -> Icons.Default.ArrowForward
            else -> Icons.Default.SensorWindow
        }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = { Text("Choose your preferred theme mode.") },
            confirmButton = {
                TextButton(onClick = {
                    onThemeSelected("Light")
                    showThemeDialog = false
                }) { Text("Light") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        onThemeSelected("Dark")
                        showThemeDialog = false
                    }) { Text("Dark") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        onThemeSelected("System")
                        showThemeDialog = false
                    }) { Text("System") }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gyroscope Overview") },
                actions = {
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // BLE status indicator: blinking dot if active.
                        val dotColor = if (bleActive) MaterialTheme.colors.secondary.copy(alpha = dotAlpha) else MaterialTheme.colors.error
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (bleActive) "BLE Active" else "BLE Inactive",
                            style = MaterialTheme.typography.body2
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh BLE")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Sensor Readings",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            // Card displaying sensor data in a table-like layout.
            Card(
                elevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SensorDataItem(label = "Roll", value = roll, icon = rollIcon(roll))
                    SensorDataItem(label = "Pitch", value = pitch, icon = pitchIcon(pitch))
                    SensorDataItem(label = "Yaw", value = yaw, icon = yawIcon(yaw))
                }
            }
            // Card with detailed sensor information.
            Card(
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Sensor Details", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Roll: rotation around the front-to-back axis.\n" +
                                "• Pitch: tilt from side-to-side.\n" +
                                "• Yaw: rotation around the vertical axis.",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}


@Composable
fun SensorDataItem(label: String, value: Float, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.subtitle2)
        Text(text = "%.2f".format(value), style = MaterialTheme.typography.h6)
    }
}

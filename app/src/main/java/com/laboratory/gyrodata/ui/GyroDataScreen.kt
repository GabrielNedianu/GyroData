package com.laboratory.gyrodata.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GyroDataScreen(roll: Float, pitch: Float, yaw: Float) {
    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(Modifier.height(32.dp))
        Text(text = "Roll: %.2f".format(roll), style = MaterialTheme.typography.h5)
        Text(text = "Pitch: %.2f".format(pitch), style = MaterialTheme.typography.h5)
        Text(text = "Yaw: %.2f".format(yaw), style = MaterialTheme.typography.h5)
    }
}

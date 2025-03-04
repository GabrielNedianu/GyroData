package com.laboratory.gyrodata

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.laboratory.gyrodata.ui.GyroDataScreen
import com.laboratory.gyrodata.ui.theme.ComposeGyroSensorTheme
import com.laboratory.gyrodata.viewmodel.GyroViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GyroViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request required Bluetooth permissions.
        val permissions = arrayOf(
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN
        )
        if (permissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissions(permissions, 1)
        }

        // Set the Compose UI, passing state and callbacks from the ViewModel.
        setContent {
            ComposeGyroSensorTheme {
                GyroDataScreen(
                    roll = viewModel.roll,
                    pitch = viewModel.pitch,
                    yaw = viewModel.yaw,
                    bleActive = viewModel.bleActive,
                    onRefresh = { viewModel.refreshBle() },
                    onThemeSelected = { /*  */ }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startSensors()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopSensors()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopBle()
    }
}

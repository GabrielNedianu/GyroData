package com.laboratory.gyrodata

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableFloatStateOf
import com.laboratory.gyrodata.ble.BlePeripheralService
import com.laboratory.gyrodata.sensor.GyroDataListener
import com.laboratory.gyrodata.sensor.GyroSensorManager
import com.laboratory.gyrodata.ui.GyroDataScreen
import com.laboratory.gyrodata.ui.theme.ComposeGyroSensorTheme

class MainActivity : ComponentActivity() {

    private lateinit var gyroSensorManager: GyroSensorManager
    private lateinit var blePeripheralService: BlePeripheralService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions
        val permissions = arrayOf(
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN
        )

        if (permissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissions(permissions, 1)
        }

        // Initialize sensor manager and BLE peripheral service.
        gyroSensorManager = GyroSensorManager(this)
        blePeripheralService = BlePeripheralService(this)
        blePeripheralService.start()

        // Mutable states for Compose UI.
        val rollState = mutableFloatStateOf(0f)
        val pitchState = mutableFloatStateOf(0f)
        val yawState = mutableFloatStateOf(0f)

        // Set up the sensor listener.
        gyroSensorManager.setGyroDataListener(object : GyroDataListener {
            override fun onGyroData(roll: Float, pitch: Float, yaw: Float) {
                rollState.floatValue = roll
                pitchState.floatValue = pitch
                yawState.floatValue = yaw

                // Send sensor data as a comma-separated string over BLE.
                val dataString = "$roll,$pitch,$yaw\n"
                blePeripheralService.updateGyroData(dataString)
            }
        })

        // Set the Compose UI content.
        setContent {
            ComposeGyroSensorTheme {
                GyroDataScreen(
                    roll = rollState.floatValue,
                    pitch = pitchState.floatValue,
                    yaw = yawState.floatValue
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gyroSensorManager.start()
    }

    override fun onPause() {
        super.onPause()
        gyroSensorManager.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        blePeripheralService.stop()
    }
}

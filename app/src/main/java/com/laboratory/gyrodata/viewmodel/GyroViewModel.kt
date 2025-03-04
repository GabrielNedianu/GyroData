package com.laboratory.gyrodata.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.laboratory.gyrodata.ble.BlePeripheralService
import com.laboratory.gyrodata.sensor.GyroDataListener
import com.laboratory.gyrodata.sensor.GyroSensorManager

/**
 * GyroViewModel encapsulates the sensor and BLE peripheral logic.
 * It exposes sensor readings and BLE status as state to the UI.
 */
class GyroViewModel(application: Application) : AndroidViewModel(application) {

    var roll by mutableStateOf(0f)
        private set
    var pitch by mutableStateOf(0f)
        private set
    var yaw by mutableStateOf(0f)
        private set
    var bleActive by mutableStateOf(false)
        private set

    private val context = application.applicationContext
    private val gyroSensorManager = GyroSensorManager(context)
    private val blePeripheralService = BlePeripheralService(context)

    init {
        // Setup sensor listener to update state and BLE data.
        gyroSensorManager.setGyroDataListener(object : GyroDataListener {
            override fun onGyroData(r: Float, p: Float, y: Float) {
                roll = r
                pitch = p
                yaw = y
                // Update BLE characteristic with sensor data.
                val dataString = "$r,$p,$y\n"
                blePeripheralService.updateGyroData(dataString)
            }
        })
        // Start BLE service.
        blePeripheralService.start()
        bleActive = true
    }

    fun startSensors() {
        gyroSensorManager.start()
    }

    fun stopSensors() {
        gyroSensorManager.stop()
    }

    fun stopBle() {
        blePeripheralService.stop()
        bleActive = false
    }

    fun refreshBle() {
        // Refresh the BLE peripheral service.
        stopBle()
        blePeripheralService.start()
        bleActive = true
    }
}

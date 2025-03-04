package com.laboratory.gyrodata.sensor

interface GyroDataListener {
    fun onGyroData(roll: Float, pitch: Float, yaw: Float)
}

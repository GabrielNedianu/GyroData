package com.laboratory.gyrodata.ble

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.util.*
import android.os.ParcelUuid
import androidx.core.app.ActivityCompat

class BlePeripheralService(private val context: Context) {

    companion object {
        private const val TAG = "BlePeripheralService"
        // Custom UUIDs for service and characteristic.
        val SERVICE_UUID: UUID = UUID.fromString("0000a000-0000-1000-8000-00805f9b34fb")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("0000a001-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private var gattServer: BluetoothGattServer? = null
    private var advertiser: BluetoothLeAdvertiser? = bluetoothAdapter.bluetoothLeAdvertiser
    private val connectedDevices = mutableListOf<BluetoothDevice>()

    // Create the characteristic that will hold the gyro data.
    private lateinit var gyroCharacteristic: BluetoothGattCharacteristic

    // GATT server callback to manage connections and read requests.
    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedDevices.add(device)
                Log.d(TAG, "Device connected: ${device.address}")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevices.remove(device)
                Log.d(TAG, "Device disconnected: ${device.address}")
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice, requestId: Int, offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == gyroCharacteristic.uuid) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                gattServer?.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS, 0, characteristic.value
                )
            } else {
                gattServer?.sendResponse(
                    device, requestId, BluetoothGatt.GATT_FAILURE, 0, null
                )
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (descriptor.uuid == UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")) {
                // Set the descriptor's value (typically ENABLE_NOTIFICATION_VALUE or ENABLE_INDICATION_VALUE)
                descriptor.value = value
                if (responseNeeded) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
                Log.d(TAG, "Descriptor write request handled for device: ${device.address}")
            } else {
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                }
            }
        }

    }

    fun start() {
        // Open GATT server.
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        // Create a custom service.
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        // Create a characteristic that supports read and notify.
        gyroCharacteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        val configDescriptor = BluetoothGattDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        gyroCharacteristic.addDescriptor(configDescriptor)
        service.addCharacteristic(gyroCharacteristic)
        gattServer?.addService(service)
        // Start BLE advertising.
        startAdvertising()
    }

    private fun startAdvertising() {
        if (advertiser == null) {
            Log.e(TAG, "BLE advertising not supported on this device.")
            return
        }
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        advertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "BLE Advertising started successfully")
        }
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "BLE Advertising failed with error: $errorCode")
        }
    }

    fun updateGyroData(data: String) {
        // Convert the data string to bytes.
        val dataBytes = data.toByteArray()
        gyroCharacteristic.value = dataBytes
        // Notify all connected devices of the updated characteristic value.
        for (device in connectedDevices) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            gattServer?.notifyCharacteristicChanged(device, gyroCharacteristic, false)
        }
    }

    fun stop() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        advertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close()
    }
}

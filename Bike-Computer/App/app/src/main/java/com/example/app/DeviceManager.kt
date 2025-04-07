package com.example.app.utils

import android.bluetooth.BluetoothManager
import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.util.Log

class DeviceManager(private val context: Context) {
    private val TAG = "Device Manager"
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    fun getPairedDevices(): List<ConnectedDevice> {
        // Check for Bluetooth permissions
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Permission failed")
            return emptyList()
        }

        return try {
            Log.d(TAG, "Permission granted")
            bluetoothAdapter?.bondedDevices?.map { device ->
                ConnectedDevice(
                    name = device.name ?: "Unknown Device",
                    address = device.address,
                    lastConnected = System.currentTimeMillis() // Note: Android doesn't provide last connection time
                )
            } ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }
}

data class ConnectedDevice(
    val name: String,
    val address: String,
    val lastConnected: Long
)
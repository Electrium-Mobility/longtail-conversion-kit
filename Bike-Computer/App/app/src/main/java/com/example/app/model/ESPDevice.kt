package com.example.app.model

import android.bluetooth.BluetoothDevice

data class ESPDevice(
    val name: String,
    val address: String,
    val type: DeviceType,
    val rssi: Int,
    val bluetoothDevice: BluetoothDevice
)

enum class DeviceType {
    D1_MINI,
    D1_MINI_PRO,
    ESP32,
    UNKNOWN
} 
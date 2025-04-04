package com.example.app.service

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.core.app.ActivityCompat
import com.example.app.model.ESPDevice
import com.example.app.model.DeviceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class BluetoothService(private val context: Context) {
    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner = bluetoothAdapter?.bluetoothLeScanner

    private val _discoveredDevices = MutableStateFlow<List<ESPDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<ESPDevice>> = _discoveredDevices

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            
            val device = result.device
            val deviceName = device.name ?: "Unknown Device"
            val deviceType = determineDeviceType(deviceName)
            
            val espDevice = ESPDevice(
                name = deviceName,
                address = device.address,
                type = deviceType,
                rssi = result.rssi,
                bluetoothDevice = device
            )
            
            updateDiscoveredDevices(espDevice)
        }
    }

    private fun determineDeviceType(deviceName: String): DeviceType {
        return when {
            deviceName.contains("D1 Mini", ignoreCase = true) -> DeviceType.D1_MINI
            deviceName.contains("D1 Mini Pro", ignoreCase = true) -> DeviceType.D1_MINI_PRO
            deviceName.contains("ESP32", ignoreCase = true) -> DeviceType.ESP32
            else -> DeviceType.UNKNOWN
        }
    }

    private fun updateDiscoveredDevices(newDevice: ESPDevice) {
        val currentDevices = _discoveredDevices.value.toMutableList()
        val existingIndex = currentDevices.indexOfFirst { it.address == newDevice.address }
        
        if (existingIndex >= 0) {
            currentDevices[existingIndex] = newDevice
        } else {
            currentDevices.add(newDevice)
        }
        
        _discoveredDevices.value = currentDevices
    }

    fun startScanning() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        scanner?.startScan(scanCallback)
    }

    fun stopScanning() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        scanner?.stopScan(scanCallback)
    }

    fun connectToDevice(device: ESPDevice) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // Implement connection logic here
        device.bluetoothDevice.connectGatt(context, false, null)
    }
} 
package com.example.app

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat

class BLEScanner(private val context: Context) {
    private val bluetoothManager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    private val bluetoothAdapter = bluetoothManager?.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private val scanResults = mutableListOf<BluetoothDevice>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            if (!scanResults.contains(device)) {
                scanResults.add(device)
                val deviceName = getDeviceName(device)
                Log.d("BLEScanner", "Found device: $deviceName - ${device.address}")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLEScanner", "Scan failed with error code: $errorCode")
        }
    }

    fun startScan() {
        if (bluetoothLeScanner == null || scanning) return
        if (!hasPermissions()) {
            Log.e("BLEScanner", "Missing Bluetooth permissions")
            return
        }
        try {
            scanning = true
            scanResults.clear()
            bluetoothLeScanner.startScan(scanCallback)
            Log.d("BLEScanner", "Scanning for BLE devices...")
        } catch (e: SecurityException) {
            Log.e("BLEScanner", "SecurityException: Missing Bluetooth permissions")
        }
    }

    fun stopScan() {
        if (bluetoothLeScanner == null || !scanning) return
        try {
            scanning = false
            bluetoothLeScanner.stopScan(scanCallback)
            Log.d("BLEScanner", "Stopped scanning.")
        } catch (e: SecurityException) {
            Log.e("BLEScanner", "SecurityException: Missing Bluetooth permissions")
        }
    }

    fun getScanResults(): List<BluetoothDevice> = scanResults

    private fun hasPermissions(): Boolean {
        return PermissionUtils.permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getDeviceName(device: BluetoothDevice): String {
        return if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            device.name ?: "Unknown Device"
        } else {
            "Unknown Device (Permission Denied)"
        }
    }
}

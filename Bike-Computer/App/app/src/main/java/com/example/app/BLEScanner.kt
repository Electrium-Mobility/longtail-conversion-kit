package com.example.app

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import com.example.app.PermissionUtils
import com.google.accompanist.permissions.rememberMultiplePersmissionState

class BLEScanner(context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private val scanResults = mutableListOf<BluetoothDevice>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            if (!scanResults.contains(device)) {
                scanResults.add(device)
                Log.d("BLEScanner", "Found device: ${device.name} - ${device.address}")
            }
        }
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLEScanner", "Scan failed with error code: $errorCode")
        }
    }

    fun startScan() {
        if (bluetoothLeScanner == null || scanning) return
        scanning = true
        scanResults.clear()
        bluetoothLeScanner.startScan(scanCallback)
        Log.d("BLEScanner", "Scanning for BLE devices...")
    }

    fun stopScan() {
        if (bluetoothLeScanner == null || !scanning) return
        scanning = false
        bluetoothLeScanner.stopScan(scanCallback)
        Log.d("BLEScanner", "Stopped scanning.")
    }

    fun getScanResults(): List<BluetoothDevice> {
        return scanResults
    }
}
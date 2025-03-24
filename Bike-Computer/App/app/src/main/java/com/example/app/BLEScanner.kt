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
import android.bluetooth.*
import java.io.IOException
import java.util.UUID

class BLEScanner(private val context: Context) {
    private val bluetoothManager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    private val bluetoothAdapter = bluetoothManager?.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private val scanResults = mutableListOf<BluetoothDevice>()
    private val targetMacAddress = "98:3D:AE:E9:2A:A8"
    private var bluetoothGatt: BluetoothGatt? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            if (device.address.equals(targetMacAddress, ignoreCase = true)) {
                val deviceName = getDeviceName(device)
                Log.d("BLEScanner", "Found target device: $deviceName - ${device.address}")
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

    private fun connectToDevice(device: BluetoothDevice) {
        if (!hasPermissions()) {
            Log.e("BLEScanner", "Missing Bluetooth permissions to connect")
            return
        }
        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (!hasPermissions()) return
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLEScanner", "Connected to GATT server.")
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLEScanner", "Disconnected from GATT server.")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (!hasPermissions()) return
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    for (service in gatt.services) {
                        for (characteristic in service.characteristics) {
                            if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                                sendData(gatt, characteristic, "Hello BLE")
                                return
                            }
                        }
                    }
                } else {
                    Log.e("BLEScanner", "Service discovery failed with status: $status")
                }
            }
        })
    }

    private fun sendData(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, data: String) {
        if (!hasPermissions()) {
            Log.e("BLEScanner", "Missing Bluetooth permissions to send data")
            return
        }
        val value = data.toByteArray()
        gatt.writeCharacteristic(characteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        Log.d("BLEScanner", "Data sent: $data")
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
            device.address ?: "Unknown Device"
        } else {
            "Unknown Device (Permission Denied)"
        }
    }
}

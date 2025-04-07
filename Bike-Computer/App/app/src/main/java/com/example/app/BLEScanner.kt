package com.example.app

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.UUID

class BLEScanner(private val context: Context) {
    private val bluetoothManager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    private val bluetoothAdapter = bluetoothManager?.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private val scanResults = mutableListOf<BluetoothDevice>()
    private val targetMacAddress = "98:3d:ae:e9:2a:aa"
    private var bluetoothGatt: BluetoothGatt? = null

    private val SERVICE_UUID = UUID.fromString("c8a19548-8efa-4143-87eb-5e85ecefc852")
    private val ETA_UUID = UUID.fromString("9af73d89-bc02-4c61-ba43-9d65fa7fc86c")
    private val DIRECTION_UUID = UUID.fromString("51b844f5-72ea-29bb-1248-cf98be98eeb2")

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            Log.d("BLEScanner", "Scan result: ${device.address}, RSSI: ${result.rssi}")
            if (device.address.equals(targetMacAddress, ignoreCase = true)) {
                val deviceName = getDeviceName(device)
                Log.d("BLEScanner", "Found target device: $deviceName - ${device.address}")
                scanResults.add(device)
                stopScan()
                connectToDevice(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            scanning = false
            Log.e("BLEScanner", "Scan failed with error code: $errorCode")
        }
    }

    fun startScan() {
        if (bluetoothLeScanner == null) {
            Log.e("BLEScanner", "Bluetooth LE scanner not available")
            return
        }
        if (scanning) {
            Log.d("BLEScanner", "Already scanning")
            return
        }
        if (!hasPermissions()) {
            Log.e("BLEScanner", "Missing permissions: ${PermissionUtils.permissions.joinToString()}")
            return
        }
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            Log.e("BLEScanner", "Bluetooth is not enabled or unavailable")
            return
        }
        try {
            scanning = true
            scanResults.clear()
            bluetoothLeScanner.startScan(scanCallback)
            Log.d("BLEScanner", "Scan started successfully")
        } catch (e: SecurityException) {
            scanning = false
            Log.e("BLEScanner", "SecurityException: ${e.message}")
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (!hasPermissions()) {
            Log.e("BLEScanner", "Missing permissions for GATT connection")
            return
        }
        try {
            bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (!hasPermissions()) return
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            Log.d("BLEScanner", "Connected to ${device.address}")
                            gatt.discoverServices()
                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            Log.d("BLEScanner", "Disconnected from ${device.address}")
                        }
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (!hasPermissions()) return
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("BLEScanner", "Services discovered: ${gatt.services.size} services")
                        val service = gatt.getService(SERVICE_UUID)
                        if (service != null) {
                            // Example data to send
                            val etaData = "EstimatedTimeArrival:12:30PM"
                            val directionData = "Northbound"

                            // Send data to each characteristic
                            sendDataToCharacteristic(gatt, service, ETA_UUID, etaData)
                            sendDataToCharacteristic(gatt, service, DIRECTION_UUID, directionData)
                        } else {
                            Log.e("BLEScanner", "Service $SERVICE_UUID not found")
                        }
                    } else {
                        Log.e("BLEScanner", "Service discovery failed: $status")
                    }
                }

                override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("BLEScanner", "Write successful to ${characteristic?.uuid}")
                    } else {
                        Log.e("BLEScanner", "Write failed: $status")
                    }
                }
            })
        } catch (e: SecurityException) {
            Log.e("BLEScanner", "SecurityException in connectGatt: ${e.message}")
        }
    }

    private fun sendDataToCharacteristic(gatt: BluetoothGatt, service: BluetoothGattService, uuid: UUID, data: String) {
        val characteristic = service.getCharacteristic(uuid)
        if (characteristic == null) {
            Log.e("BLEScanner", "Characteristic $uuid not found")
            return
        }
        if (!hasPermissions()) {
            Log.e("BLEScanner", "Missing permissions for data send")
            return
        }
        if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
            Log.e("BLEScanner", "Characteristic $uuid does not support write")
            return
        }

        val maxChunkSize = 20 - 1
        val dataWithDelimiter = "$data$"
        val byteData = dataWithDelimiter.toByteArray()

        try {
            if (byteData.size <= maxChunkSize) {
                characteristic.setValue(byteData)
                val success = gatt.writeCharacteristic(characteristic)
                Log.d("BLEScanner", "Single write initiated: $dataWithDelimiter (${byteData.size} bytes)")
            } else {
                var offset = 0
                while (offset < byteData.size) {
                    val chunkSize = minOf(maxChunkSize, byteData.size - offset)
                    val chunk = ByteArray(chunkSize + 1)
                    System.arraycopy(byteData, offset, chunk, 0, chunkSize)
                    chunk[chunkSize] = '$'.code.toByte()
                    characteristic.setValue(chunk)
                    val success = gatt.writeCharacteristic(characteristic)
                    if (!success) {
                        Log.e("BLEScanner", "Failed to initiate chunk write at offset $offset")
                        break
                    }
                    Log.d("BLEScanner", "Chunk write initiated: ${String(chunk)} (${chunk.size} bytes)")
                    offset += chunkSize
                    Thread.sleep(100)
                }
            }
        } catch (e: SecurityException) {
            Log.e("BLEScanner", "SecurityException in sendData: ${e.message}")
        } catch (e: InterruptedException) {
            Log.e("BLEScanner", "Interrupted during chunked write: ${e.message}")
        }
    }

    fun stopScan() {
        if (bluetoothLeScanner == null || !scanning) {
            Log.d("BLEScanner", "No scan to stop")
            return
        }
        try {
            scanning = false
            bluetoothLeScanner.stopScan(scanCallback)
            Log.d("BLEScanner", "Scan stopped")
        } catch (e: SecurityException) {
            Log.e("BLEScanner", "SecurityException in stopScan: ${e.message}")
        }
    }

    fun disconnect() {
        bluetoothGatt?.let { gatt ->
            if (!hasPermissions()) {
                Log.e("BLEScanner", "Missing permissions to disconnect")
                bluetoothGatt = null
                return
            }
            try {
                gatt.disconnect()
                gatt.close()
                bluetoothGatt = null
                Log.d("BLEScanner", "GATT disconnected and closed")
            } catch (e: SecurityException) {
                Log.e("BLEScanner", "SecurityException during disconnect: ${e.message}")
                bluetoothGatt = null
            }
        }
    }

    fun getScanResults(): List<BluetoothDevice> = scanResults.toList()

    private fun hasPermissions(): Boolean {
        val missing = PermissionUtils.permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            Log.w("BLEScanner", "Missing permissions: ${missing.joinToString()}")
        }
        return missing.isEmpty()
    }

    private fun getDeviceName(device: BluetoothDevice): String {
        return if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            device.name ?: "Unnamed (${device.address})"
        } else {
            "Permission Denied (${device.address})"
        }
    }

    companion object {
        private const val SCAN_FAILED_ALREADY_STARTED = 1
        private const val SCAN_FAILED_APPLICATION_REGISTRATION_FAILED = 2
        private const val SCAN_FAILED_INTERNAL_ERROR = 3
        private const val SCAN_FAILED_FEATURE_UNSUPPORTED = 4
    }
}
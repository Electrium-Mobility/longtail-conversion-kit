package com.example.app

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app.service.BluetoothService
import com.example.app.ui.theme.AppTheme

val DEVICES_LIST = listOf(
            "JBL Speaker",
            "iPhone 3",
            "Someone's Laptop",
            "AirPods",
            "iPhone 4",
            "iPhone 5",
            "Someone's Bose Headphones",
            "Computer",
            "Smart Fridge",
            "JBL Speaker",
            "iPhone 3",
            "Someone's Laptop",
            "AirPods",
            "iPhone 4",
            "iPhone 5",
            "Someone's Bose Headphones",
            "Computer",
            "Smart Fridge"
)

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothService: BluetoothService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            // All permissions granted
            checkBluetoothEnabled()
        } else {
            Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_LONG).show()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startBluetoothScanning()
        } else {
            Toast.makeText(this, "Bluetooth must be enabled to scan for devices", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        
        // Request notification access if not granted
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        
        
        bluetoothService = BluetoothService(this)
        checkBluetoothPermissions()
    }

    private fun checkBluetoothPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            checkBluetoothEnabled()
        }
    }

    private fun checkBluetoothEnabled() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter?.isEnabled == true) {
            startBluetoothScanning()
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }
    }

    private fun startBluetoothScanning() {
        bluetoothService.startScanning()
        setContent {
            val navController = rememberNavController()
            val bleScanner = BLEScanner(this)
            NavHost(navController = navController, startDestination = "HomeScreen", builder = {
                composable(route = "HomeScreen", content = { 
                    HomeScreen(
                        navController = navController,
                        bluetoothService = bluetoothService
                    ) 
                })
                composable(route = "AllRPDevicesScreen", content = { 
                    AllRPDevicesScreen(
                        navController = navController,
                        bluetoothService = bluetoothService
                    ) 
                })
            })
        }
        lifecycleScope.launch {
            MapNotificationService.directionIcon.collect { bitmap ->
                if (bitmap != null) {
                    BitmapSaver.saveBitmapToExternalStorage(this@MainActivity, bitmap)
                }
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val componentName = ComponentName(this, NotificationListenerService::class.java)
        val flat = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return flat?.contains(componentName.flattenToString()) == true
    }
}
    override fun onDestroy() {
        super.onDestroy()
        bluetoothService.stopScanning()
    }
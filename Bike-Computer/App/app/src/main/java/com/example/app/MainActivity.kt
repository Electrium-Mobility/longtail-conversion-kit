package com.example.app

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.content.ContextCompat
import android.util.Log
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app.utils.DeviceManager
import androidx.activity.result.contract.ActivityResultContracts

import kotlinx.coroutines.launch

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
    private val TAG = "MAIN"
    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "BLUETOOTH_CONNECT permission granted")
            // Retry the operation that needs permission
        } else {
            Log.d(TAG, "BLUETOOTH_CONNECT permission denied")
        }
    }

    private lateinit var deviceManager: DeviceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceManager = DeviceManager(this)
        enableEdgeToEdge()

        // Request notification access if not granted
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        // Request Bluetooth connect permission so we can get previous devices
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }

        setContent {
            val navController = rememberNavController()
            val bleScanner = BLEScanner(this)
            NavHost(navController = navController, startDestination = "HomeScreen", builder = {
                composable(route = "HomeScreen", content = { HomeScreen(navController = navController, deviceManager = deviceManager) })
                composable(route = "AllRPDevicesScreen", content = { AllRPDevicesScreen(navController = navController, deviceManager = deviceManager) })
                composable(route = "DisplayNotificationsScreen", content = { DisplayNotificationsScreen(navController = navController) })
                composable("DeviceListScreen") { DeviceListScreen(navController, bleScanner) }
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
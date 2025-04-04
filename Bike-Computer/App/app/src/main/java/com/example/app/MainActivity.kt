package com.example.app

<<<<<<< HEAD
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
=======
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
>>>>>>> testbitmap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
<<<<<<< HEAD
        startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
=======
        
        // Request notification access if not granted
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        
>>>>>>> testbitmap
        setContent {
            val navController = rememberNavController()
            val bleScanner = BLEScanner(this)
            NavHost(navController = navController, startDestination = "HomeScreen", builder = {
                composable(route = "HomeScreen", content = { HomeScreen(navController = navController, devicesList = DEVICES_LIST) })
                composable(route = "AllRPDevicesScreen", content = { AllRPDevicesScreen(navController = navController, devicesList = DEVICES_LIST) })
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

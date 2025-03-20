package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
    }
}

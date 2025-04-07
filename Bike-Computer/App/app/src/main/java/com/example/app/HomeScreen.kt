package com.example.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app.service.BluetoothService
import com.example.app.model.ESPDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(devicesList: List<String>, navController: NavController) {
    Scaffold(Modifier.fillMaxWidth()) { innerPadding ->

        // Add Electrium Mobility Logo Header Here

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RPDevices(
                devicesList,
                modifier = Modifier.padding(innerPadding),
                amount = 5, // max amount of devices to display on home screen
                navController = navController,
                onDeviceClick = { _ -> }
            )

            NotificationHubButton(navController)
            ScanBLEDevicesButton(navController)
        }

        // Add Bluetooth Button Here
    }
}

@Composable
fun ScanBLEDevicesButton(navController: NavController = rememberNavController()) {
    Button(
        onClick = { navController.navigate("DeviceListScreen") },
        modifier = Modifier.padding(top = 20.dp).height(100.dp).width(300.dp),
    ) {
        Text(
            text = "Scan BLE Devices",
            fontSize = 30.sp
        )
    }
}

@Composable
fun NotificationHubButton(navController: NavController = rememberNavController()) {
    Button(
        onClick = { navController.navigate("DisplayNotificationsScreen") },
        modifier = Modifier.padding(top = 20.dp).height(100.dp).width(300.dp),
    ) {
        Text(
            text = "Notification Hub",
            fontSize = 30.sp
        )

    }
}

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app.service.BluetoothService
import com.example.app.model.ESPDevice

@Composable
fun HomeScreen(
    navController: NavController,
    bluetoothService: BluetoothService
) {
    val discoveredDevices by bluetoothService.discoveredDevices.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("ESP Device Scanner") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            RPDevices(
                devicesList = discoveredDevices.map { it.name },
                modifier = Modifier.padding(16.dp),
                amount = 5,
                navController = navController,
                onDeviceClick = { index ->
                    if (index < discoveredDevices.size) {
                        bluetoothService.connectToDevice(discoveredDevices[index])
                    }
                }
            )
        }
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

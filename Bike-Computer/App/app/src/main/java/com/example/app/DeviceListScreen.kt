package com.example.app

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.bluetooth.BluetoothDevice
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*

@Composable
fun DeviceListScreen(navController: NavController, bleScanner: BLEScanner) {
    var scannedDevices by remember { mutableStateOf(emptyList<BluetoothDevice>()) }

    LaunchedEffect(bleScanner) {
        bleScanner.startScan()
    }

    LaunchedEffect(Unit) {
        while (true) {
            scannedDevices = bleScanner.getScanResults()
            kotlinx.coroutines.delay(2000) // Refresh every 2 seconds
        }
    }

    Scaffold(Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Header(navController)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (scannedDevices.isEmpty()) {
                    Text("No devices found", fontSize = 18.sp, color = Color.Gray)
                } else {
                    scannedDevices.forEach { device ->
                        val deviceName = try {
                            device.name ?: "Unknown Device"
                        } catch (e: SecurityException) {
                            "Unknown Device (Permission Denied)"
                        }
                        DeviceItem(deviceName = deviceName, isConnected = false)
                    }
                }
            }
        }
    }
}



@Composable
private fun Header(navController: NavController) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .background(Color(red = 50, green = 200, blue = 50))) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = {
                    navController.navigate("HomeScreen")
                },
                modifier = Modifier.width(40.dp)
            ) {
            Text(
                    text = "<",
                    color = Color.White,
                    fontSize = 34.sp
            )
        }
            Text(
                text = "Connected Devices",
                color = Color.White,
                fontSize = 28.sp,
                modifier = Modifier
                    .padding(start = 15.dp)
            )
    }
}
}

@Composable
fun DeviceItem(deviceName: String, isConnected: Boolean) {
    Button(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = deviceName,
                fontSize = 20.sp
            )
            Text(
                text = if (isConnected) "Connected" else "Disconnected",
                fontSize = 16.sp
            )
        }
    }
}

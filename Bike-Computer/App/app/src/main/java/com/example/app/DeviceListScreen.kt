package com.example.app

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

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
                        val deviceAddress = try {
                            device.address ?: "Unknown Address"
                        } catch (e: SecurityException) {
                            "Unknown Device (Permission Denied)"
                        }
                        DeviceItem(deviceAddress = deviceAddress, isConnected = false)
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
        .background(color = Color(ContextCompat.getColor(LocalContext.current, R.color.green_main)))) {
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
fun DeviceItem(deviceAddress: String, isConnected: Boolean) {
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
                text = deviceAddress,
                fontSize = 20.sp
            )
            Text(
                text = if (isConnected) "Connected" else "Disconnected",
                fontSize = 16.sp
            )
        }
    }
}

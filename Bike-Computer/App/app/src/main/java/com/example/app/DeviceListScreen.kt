package com.example.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun DeviceListScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val devices = listOf(
        "JBL Speaker" to true,
        "iPhone 3" to false,
        "Someone's Laptop" to true,
        "AirPods" to false
    )

    Scaffold(Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = modifier.padding(innerPadding)) {
            Header(navController)
            Column(
            modifier = Modifier
            .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
    ) {
                devices.forEach { (deviceName, isConnected) ->
                    DeviceItem(deviceName = deviceName, isConnected = isConnected)
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
fun DeviceItem(deviceName: String, isConnected: Boolean) {
    Button(
        onClick = { }, // configure when BLE is set up
        elevation = ButtonDefaults.buttonElevation(pressedElevation = 3.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = deviceName,
                color = Color.Black,
                textAlign = TextAlign.Left,
                fontSize = 20.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            Text(
                text = if (isConnected) "Connected" else "Disconnected",
                color = if (isConnected) Color(red = 50, green = 200, blue = 50) else Color.Red,
                fontSize = 16.sp
            )
        }
    }
}
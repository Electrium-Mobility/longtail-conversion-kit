package com.example.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.app.utils.DeviceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(deviceManager: DeviceManager, navController: NavController) {
    val pairedDevices by remember{
        mutableStateOf(deviceManager.getPairedDevices())
    }

    Scaffold(Modifier.fillMaxWidth()) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(color = Color(ContextCompat.getColor(LocalContext.current, R.color.green_main)))
            ) {
                Image(painter = painterResource(id = R.drawable.electrium_logo),
                    contentDescription = "Electrium Logo",
                    modifier = Modifier.fillMaxWidth().padding(16.dp, top = 24.dp, end = 18.dp),
                    contentScale = ContentScale.FillWidth
                )
            }

            RPDevices(
                devices = pairedDevices,
                modifier = Modifier.padding(2.dp),
                amount = 5, // max amount of devices to display on home screen
                navController = navController,
                onDeviceClick = { _ -> }
            )

            NotificationHubButton(navController)
            ScanBLEDevicesButton(navController)
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

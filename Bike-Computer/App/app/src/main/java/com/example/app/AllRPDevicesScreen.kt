package com.example.app

import android.content.Context
import android.content.res.Resources
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.app.utils.ConnectedDevice
import com.example.app.utils.DeviceManager

@Composable
fun AllRPDevicesScreen(deviceManager: DeviceManager,
                       navController: NavController
) {
    val devices by remember( {
        mutableStateOf(deviceManager.getPairedDevices())
    } )
    Scaffold(Modifier.fillMaxSize()) { innerPadding ->
        Column() {
            Header(navController)
            DisplayDevices(
                devices,
                modifier = Modifier.verticalScroll(rememberScrollState()),
                amount = devices.size,
                onDeviceClick = { _ -> }
            )
        }
    }
}

@Composable
private fun Header(navController: NavController) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
        .background(color = Color(ContextCompat.getColor(LocalContext.current, R.color.green_main)))) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            TextButton (
                onClick = {
                    navController.navigate("HomeScreen")
                }, // go back to home screen
                modifier = Modifier
                    .padding(top = 30.dp)
                    .size(70.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Text(
                text = "Recently Paired",
                color = Color.White,
                fontSize = 28.sp,
                modifier = Modifier
                    .padding(start = 10.dp, top = 30.dp)
            )
        }
    }
}


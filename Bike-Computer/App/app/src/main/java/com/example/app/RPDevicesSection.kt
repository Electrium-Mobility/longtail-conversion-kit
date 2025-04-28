package com.example.app

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.app.utils.ConnectedDevice

@Composable
fun RPDevices(
    devices: List<ConnectedDevice>,
    modifier: Modifier = Modifier,
    amount: Int = devices.size,
    navController: NavController,
    onDeviceClick: (ConnectedDevice) -> Unit
) {
    Column(modifier = modifier) {
        DisplayDevices(devices = devices, amount = amount, onDeviceClick = onDeviceClick)
        ViewAllButton(navController)
    }
}

@Composable
fun DisplayDevices(
    devices: List<ConnectedDevice>,
    modifier: Modifier = Modifier,
    amount: Int = devices.size,
    onDeviceClick: (ConnectedDevice) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = modifier) {
        if (devices.isEmpty()) {
            Text(
                text = "No devices connected yet",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        else {
            for (i in 0..<minOf(amount, devices.size)) {
                val device = devices[i]
                Button(
                    onClick = { onDeviceClick(device) },
                    elevation = ButtonDefaults.buttonElevation(pressedElevation = 3.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = device.name,
                        color = Color.Black,
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        fontSize = 20.sp,
                        )
                }
                if (i < minOf(amount, devices.size) - 1) {
                    HorizontalDivider(
                        color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewAllButton(navController: NavController) {
    Box(Modifier.fillMaxWidth()) {
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(ContextCompat.getColor(LocalContext.current, R.color.green_main))
            ),
            modifier = Modifier
                .padding(10.dp)
                .height(60.dp)
                .width(180.dp)
                .align(Alignment.Center)
                .wrapContentHeight(Alignment.CenterVertically),
            onClick = {
                navController.navigate("AllRPDevicesScreen")
            },
        ) {
            Text(
                text = "View All",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .wrapContentHeight(align = Alignment.CenterVertically),
                fontSize = 24.sp,
            )
        }
    }
}
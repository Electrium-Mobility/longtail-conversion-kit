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

@Composable
fun RPDevices(devicesList: List<String>, modifier: Modifier = Modifier, amount: Int = devicesList.size, navController: NavController) {
    Column(modifier = modifier) {
        Title()
        DisplayDevices(devicesList, amount = amount)
        ViewAllButton(navController)
    }
}

@Composable
private fun Title() {
    Box(Modifier.fillMaxWidth()) {
        Text(
            text = "Recently Paired Devices",
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 15.dp).align(Alignment.Center)
        )
    }
}

@Composable
fun DisplayDevices(devicesList: List<String>, modifier: Modifier = Modifier, amount: Int = devicesList.size) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = modifier) {
        for (i in 0..< minOf(amount, devicesList.size)) {
            Button(
                onClick = {  }, // configure when BLE is set up
                elevation = ButtonDefaults.buttonElevation(pressedElevation = 3.dp),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
                    .height(60.dp)

            ) {
                Text(
                    text = devicesList[i],
                    color = Color.Black,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    fontSize = 20.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
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
                navController.navigate("AllRPDevicesScreen") // navigate to screen that displays all RP devices
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

@Preview(showBackground = true)
@Composable
fun RPDevicesPreview() {
    RPDevices(
        devicesList = listOf(
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
        ),
        amount = 5,
        modifier = Modifier.padding(16.dp), // mocks innerPadding from Scaffold
        navController = rememberNavController() // mock navController for Previews
    )
}


package com.example.app

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun DisplayNotificationsScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Scaffold(Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Red),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(navController)
            DirectionsDisplay()
        }
    }
}

@Composable
private fun DirectionsDisplay() {
    val directions by MapNotificationService.latestDirections.collectAsState()
    val directionIcon by MapNotificationService.latestArrows.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large Arrow
        directionIcon?.let {
            Image (
                bitmap = it.asImageBitmap(),
                modifier = Modifier.size(200.dp),
                contentDescription = "Direction Arrow",
            )
        }
        
        // Direction Text
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text(
                text = directions ?: "No active navigation",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
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
            .fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            TextButton (
                onClick = {
                    navController.navigate("HomeScreen")
                }, // go back to home screen
                modifier = Modifier.width(40.dp)
            ) {
                Text(
                    text = "<",
                    color = Color.White,
                    fontSize = 34.sp
                )
            }
            Text(
                text = "Notification Hub",
                color = Color.White,
                fontSize = 28.sp,
                modifier = Modifier
                    .padding(start = 70.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DisplayNotificationsScreenPreview() {
    DisplayNotificationsScreen(
        modifier = Modifier.padding(16.dp), // mocks innerPadding from Scaffold
        navController = rememberNavController() // mock navController for Previews
    )
}
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
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
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(navController)
            DirectionsDisplay()
        }
    }
}

@Composable
private fun DirectionsDisplay() {
    val directionDistance by MapNotificationService.directionDistance.collectAsState()
    val directionText by MapNotificationService.directionText.collectAsState()
    val directionIcon by MapNotificationService.directionIcon.collectAsState()
    val etaInDuration by MapNotificationService.etaInDuration.collectAsState()
    val etaInDistance by MapNotificationService.etaInDistance.collectAsState()
    val etaInTime by MapNotificationService.etaInTime.collectAsState()
    val iconType by MapNotificationService.iconType.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 50.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(ContextCompat.getColor(LocalContext.current, R.color.green_main)),
            contentColor = Color.White
        )

    ) {

        // Direction Text
        Text(
            text = directionText?: "No active navigation",
            modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        // Large Arrow
        directionIcon?.let {
            Image(
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).size(100.dp),
                bitmap = it.asImageBitmap(),
                contentDescription = "Direction Arrow",
            )
            Text(
                text = "Direction: $iconType",
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }

        // Direction Distance
        directionDistance?.let {
            Text(
                text = it,
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }

        // ETA in Duration
        etaInDuration?.let{
            Text(
                text = it,
                modifier = Modifier.padding(start = 12.dp, top = 5.dp, bottom = 5.dp),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }

        // ETA in Distance
        etaInDistance?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = 12.dp, top = 5.dp, bottom = 5.dp),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }

        // ETA in Time
        etaInTime?.let{
            Text(
                text = it,
                modifier = Modifier.padding(start = 12.dp, top = 5.dp, bottom = 12.dp),
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
        .background(color = Color(ContextCompat.getColor(LocalContext.current, R.color.green_main)))) {
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
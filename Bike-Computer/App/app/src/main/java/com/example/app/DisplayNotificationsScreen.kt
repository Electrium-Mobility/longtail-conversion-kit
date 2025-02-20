package com.example.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val directions by MapNotificationService.latestDirections.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large Arrow
        Text(
            text = getDirectionArrow(directions),
            fontSize = 120.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
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

private fun getDirectionArrow(direction: String?): String {
    if (direction == null) return "⚡" // Default when no direction
    
    return when {
        direction.contains("right", ignoreCase = true) -> "➡"
        direction.contains("left", ignoreCase = true) -> "⬅"
        direction.contains("straight", ignoreCase = true) || 
        direction.contains("continue", ignoreCase = true) -> "⬆"
        direction.contains("u-turn", ignoreCase = true) || 
        direction.contains("turn around", ignoreCase = true) -> "⬇"
        direction.contains("merge", ignoreCase = true) -> "↗"
        direction.contains("exit", ignoreCase = true) -> "↘"
        else -> "⚡" // Default for unknown directions
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
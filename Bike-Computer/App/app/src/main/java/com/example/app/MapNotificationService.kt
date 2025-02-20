package com.example.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapNotificationService : NotificationListenerService() {
    
    companion object {
        private const val MAPS_PACKAGE = "com.google.android.apps.maps"
        private val _latestDirections = MutableStateFlow<String?>(null)
        val latestDirections: StateFlow<String?> = _latestDirections.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == MAPS_PACKAGE) {
            val notification = sbn.notification
            val title = notification.extras.getString("android.title") ?: return
            val text = notification.extras.getString("android.text") ?: return
            
            // Only process navigation notifications
            if (title.contains("Navigation") || text.contains("Turn") || text.contains("Continue")) {
                _latestDirections.value = text
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "map_directions",
            "Map Directions",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Shows navigation directions from Google Maps"
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
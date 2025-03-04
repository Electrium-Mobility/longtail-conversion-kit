package com.example.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationService"
        private const val MAPS_PACKAGE = "com.google.android.apps.maps"
        private val _latestDirections = MutableStateFlow<String?>(null)
        val latestDirections: StateFlow<String?> = _latestDirections.asStateFlow()
    }


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationService created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        Log.d(TAG, "Notification posted: ${sbn.notification.tickerText}")
        if (sbn.packageName == MAPS_PACKAGE) {
            val notification = sbn.notification
            val title = notification.extras.getCharSequence("android.title").toString() ?: return
            val text = notification.extras.getCharSequence("android.text").toString() ?: return

            Log.d(TAG, "Notification title: $title")
            Log.d(TAG, "Notification text: $text")
            Log.d(TAG, "Notification Package: ${sbn.packageName}")

            _latestDirections.value = "$title $text"
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

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "Notification removed: ${sbn.notification.tickerText}")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "NotificationService onBind")
        return super.onBind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NotificationService destroyed")
    }


}
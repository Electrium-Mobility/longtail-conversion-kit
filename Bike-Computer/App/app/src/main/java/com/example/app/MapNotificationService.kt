package com.example.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationService"
        private const val MAPS_PACKAGE = "com.google.android.apps.maps"

        private val _directionDistance = MutableStateFlow<String?>(null)
        private val _directionText = MutableStateFlow<String?>(null)
        private val _directionIcon = MutableStateFlow<Bitmap?>(null)
        private val _etaInDuration = MutableStateFlow<String?>(null)
        private val _etaInDistance = MutableStateFlow<String?>(null)
        private val _etaInTime = MutableStateFlow<String?>(null)

        val directionDistance: StateFlow<String?> = _directionDistance.asStateFlow()
        val directionText: StateFlow<String?> = _directionText.asStateFlow()
        val directionIcon: StateFlow<Bitmap?> = _directionIcon.asStateFlow()
        val etaInDuration: StateFlow<String?> = _etaInDuration.asStateFlow()
        val etaInDistance: StateFlow<String?> = _etaInDistance.asStateFlow()
        val etaInTime: StateFlow<String?> = _etaInTime.asStateFlow()
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
            _directionDistance.value = notification.extras.getCharSequence("android.title").toString()
            _directionText.value = notification.extras.getCharSequence("android.text").toString()
            _directionIcon.value = notification.getLargeIcon().loadDrawable(this)?.toBitmap()

            val etaInfo = notification.extras.getCharSequence("android.subText").toString().split("·")
            _etaInDuration.value = etaInfo[0].trim()
            _etaInDistance.value = etaInfo[1].trim()
            _etaInTime.value = etaInfo[2].trim().split(" ").subList(0, 2).joinToString(" ")
        }
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
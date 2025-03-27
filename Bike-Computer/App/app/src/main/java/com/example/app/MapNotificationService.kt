package com.example.app

import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class MapNotificationService : NotificationListenerService() {

    companion object {

        object stopper {
            var stop = 0
        }

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
            val icon = notification.getLargeIcon().loadDrawable(this)?.toBitmap()
            Log.d(TAG, "Notification icon: $icon")
            _directionIcon.value = icon
            if (icon != null && stopper.stop == 0) {
                val iconScaled40 = Bitmap.createScaledBitmap(icon, 40, 40, false)
                stopper.stop = 1
                val width = iconScaled40.width
                val height = iconScaled40.height
                val pixelCount = width * height
                val pixels = IntArray(pixelCount)

                // Copy the pixel data into the array
                iconScaled40.getPixels(pixels, 0, width, 0, 0, width, height)
                val directory = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val file = File(directory, "straightarrowbitmap.txt")
                file.writeText("")
                for (px in pixels) {
                    file.appendText(String.format("%08X\n", px))
                }
            }

            val etaInfo = notification.extras.getCharSequence("android.subText").toString().split("·")
            _etaInDuration.value = etaInfo[0].trim()
            _etaInDistance.value = etaInfo[1].trim()
            _etaInTime.value = etaInfo[2].dropLast(7).trim()
            if (icon != null) {
                BitmapSaver.saveBitmapToExternalStorage(this, icon)
            }
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
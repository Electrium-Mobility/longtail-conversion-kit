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
        private val _iconType = MutableStateFlow<String?>(null)

        val directionDistance: StateFlow<String?> = _directionDistance.asStateFlow()
        val directionText: StateFlow<String?> = _directionText.asStateFlow()
        val directionIcon: StateFlow<Bitmap?> = _directionIcon.asStateFlow()
        val etaInDuration: StateFlow<String?> = _etaInDuration.asStateFlow()
        val etaInDistance: StateFlow<String?> = _etaInDistance.asStateFlow()
        val etaInTime: StateFlow<String?> = _etaInTime.asStateFlow()
        val iconType: StateFlow<String?> = _iconType.asStateFlow()
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
            if (icon != null) {
                val iconScaled40 = Bitmap.createScaledBitmap(icon, 40, 40, false)

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
                BitmapSaver.saveBitmapToExternalStorage(this, iconScaled40)
                val iconType = identifyDirection(pixels)
                _iconType.value = iconType
            }

            val etaInfo = notification.extras.getCharSequence("android.subText").toString().split("·")
            _etaInDuration.value = etaInfo[0].trim()
            _etaInDistance.value = etaInfo[1].trim()
            _etaInTime.value = etaInfo[2].trim().split(" ").subList(0, 2).joinToString(" ")
        }

    }

    private fun identifyDirection(pxArray: IntArray): String {
        val straight = arrayOf(379, 380, 381, 382, 819, 820, 821, 822, 859, 860, 861, 862, 899, 900, 901, 902, 939, 940, 941, 942)
        val left = arrayOf(292, 293, 294, 295, 296, 333, 334, 335, 336, 373, 374, 375, 376, 411, 412, 413, 414, 450, 451, 452, 453)
        val right = arrayOf(266, 267, 268, 269, 306, 307, 308, 309, 346, 347, 348, 349, 470, 471, 472, 473, 511, 512, 513, 514)
        val slightleft = arrayOf(211, 212, 213, 214, 215, 1228, 1229, 1230, 1231, 1268, 1269, 1270, 1271, 1308, 1309, 1310, 1311, 1348, 1349, 1350, 1351)
        val slightright = arrayOf(226, 227, 228, 229, 230, 1210, 1211, 1212, 1213, 1250, 1251, 1252, 1253, 1290, 1291, 1292, 1293, 1330, 1331, 1332, 1333)
        val sharpleft = arrayOf(391, 392, 393, 1055, 1056, 1057, 1058, 1059, 1060, 1094, 1095, 1096, 1097, 1098, 1134, 1135, 1136, 1137, 1138, 1174, 1175, 1176, 1177, 1178)
        val sharpright = arrayOf(368, 369, 370, 1061, 1062, 1063, 1064, 1065, 1066, 1103, 1104, 1105, 1106, 1107, 1143, 1144, 1145, 1146, 1147, 1183, 1184, 1185, 1186, 1187)

        var matchAccuracy = 0
        for (i in slightleft) {
            if (pxArray[i-1] != 0) matchAccuracy += 1
        }
        Log.d("SlightLeft", matchAccuracy.toString())
        if (matchAccuracy > slightleft.size / 2) return "SLIGHT_LEFT"

        matchAccuracy = 0
        for (i in slightright) {
            if (pxArray[i-1] != 0) matchAccuracy += 1
        }
        Log.d("SlightRight", matchAccuracy.toString())
        if (matchAccuracy > slightright.size / 2) return "SLIGHT_RIGHT"

        matchAccuracy = 0
        for (i in sharpleft) {
            if (pxArray[i-1] != 0) matchAccuracy += 1
        }
        Log.d("SharpLeft", matchAccuracy.toString())
        if (matchAccuracy > sharpleft.size / 2) return "SHARP_LEFT"

        matchAccuracy = 0
        for (i in sharpright) {
            if (pxArray[i-1] != 0) matchAccuracy += 1
        }
        Log.d("SharpRight", matchAccuracy.toString())
        if (matchAccuracy > sharpright.size / 2) return "SHARP_RIGHT"

        matchAccuracy = 0
        for (i in left) {
            if (pxArray[i-1] != 0) matchAccuracy += 1
        }
        Log.d("Left", matchAccuracy.toString())
        if (matchAccuracy > left.size / 2) return "LEFT"

        matchAccuracy = 0
        for (i in right) {
            if (pxArray[i-1] != 0) matchAccuracy += 1
        }
        Log.d("Right", matchAccuracy.toString())
        if (matchAccuracy > right.size / 2) return "RIGHT"

        matchAccuracy = 0
        for (i in straight) {
            if (pxArray[i-1] != 0) matchAccuracy += 1
        }
        Log.d("Straight", matchAccuracy.toString())
        if (matchAccuracy > straight.size / 2) return "STRAIGHT"

        return "UNKNOWN"
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
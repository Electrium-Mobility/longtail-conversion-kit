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

    override fun onCreate() {
        super.onCreate()
        Log.d(NavigationDataManager.TAG, "NotificationService created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        if (sbn.packageName == NavigationDataManager.MAPS_PACKAGE) {
            Log.d(NavigationDataManager.TAG, "Notification posted: ${sbn.notification.tickerText}")
            val notification = sbn.notification

            //Update nav direction info
            val directionDistance = notification.extras.getCharSequence("android.title")?.toString()
            if (directionDistance == "Starting navigation...") {
                return
            }
            NavigationDataManager.setDirectionDistance(directionDistance)

            val directionText = notification.extras.getCharSequence("android.text")?.toString()
            if (directionText == null) {
                return
            }
            NavigationDataManager.setDirectionText(directionText)

            val icon = notification.getLargeIcon().loadDrawable(this)?.toBitmap()
            if (icon == null) {
                return
            }
            NavigationDataManager.setDirectionIcon(icon)

            Log.d(NavigationDataManager.TAG, "Notification icon: $icon")
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
                NavigationDataManager.setIconType(iconType)
            }

            //Update nav eta info
            val subText = notification.extras.getCharSequence("android.subText")?.toString()
            if (!subText.isNullOrEmpty()) {
                val etaInfo = subText.split("·")
                if (etaInfo.size >= 3) {
                    NavigationDataManager.setEtaInDuration(etaInfo[0].trim())
                    NavigationDataManager.setEtaInDistance(etaInfo[1].trim())
                    NavigationDataManager.setEtaInTime(etaInfo[2].trim().split(" ").take(2).joinToString(" "))
                }
            }
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
        if (matchAccuracy > left.size / 2) return "TURN_LEFT"

        matchAccuracy = 0
        for (i in right) {
            if (pxArray[i-1] != 0) matchAccuracy += 1
        }
        Log.d("Right", matchAccuracy.toString())
        if (matchAccuracy > right.size / 2) return "TURN_RIGHT"

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
        Log.d(NavigationDataManager.TAG, "Notification removed: ${sbn.notification.tickerText}")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(NavigationDataManager.TAG, "NotificationService onBind")
        return super.onBind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(NavigationDataManager.TAG, "NotificationService destroyed")
    }


}
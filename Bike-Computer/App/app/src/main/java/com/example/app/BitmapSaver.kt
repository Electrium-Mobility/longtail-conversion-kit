package com.example.app

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object BitmapSaver {
    private const val TAG = "BitmapSaver"
    private const val IMAGE_FILE_NAME = "direction_icon"
    object cnt {
        var num = 0
    }

    fun saveBitmapToExternalStorage(context: Context, bitmap: Bitmap) {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (directory == null) {
            Log.e(TAG, "External storage directory is null")
            return
        }

        val file = File(directory, IMAGE_FILE_NAME + cnt.num + ".png")
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Log.d(TAG, "Bitmap saved to: ${file.absolutePath}")
            cnt.num += 1
        } catch (e: IOException) {
            Log.e(TAG, "Error saving bitmap", e)
        }
    }
}
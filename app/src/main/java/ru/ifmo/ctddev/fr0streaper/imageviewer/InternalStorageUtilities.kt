package ru.ifmo.ctddev.fr0streaper.imageviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class InternalStorageUtilities {

    companion object {

        fun saveToInternalStorage(imagePath: String, image: Bitmap) {
            try {
                val imageFile = File(imagePath)
                val imageStream = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.JPEG, 100, imageStream)

                val outputStream = FileOutputStream(imageFile)
                outputStream.write(imageStream.toByteArray())
                outputStream.close()
            } catch (e: Exception) {
                Log.d(LOG_TAG, "Unable to save image to internal storage")
            }
        }

    }
}

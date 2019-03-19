package ru.ifmo.ctddev.fr0streaper.imageviewer

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import java.io.File
import java.io.Serializable

@Suppress("UNCHECKED_CAST")
class ImageLoaderService : IntentService("ImageLoaderService") {

    companion object {

        @JvmStatic
        fun downloadImages(
            context: Context,
            dataset: List<Image>,
            receiver: ImageLoaderServiceReceiver,
            isRegular: Boolean
        ) {
            val intent = Intent(context, ImageLoaderService::class.java)

            intent.action = "ACTION_LOAD_IMAGES"
            intent.putExtra("imageList", dataset as Serializable)
            intent.putExtra("internalStoragePath", context.filesDir.path as Serializable)
            intent.putExtra("receiver", receiver)
            intent.putExtra("isRegular", isRegular)

            context.startService(intent)
        }

    }

    private lateinit var receiver: ResultReceiver

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            "ACTION_LOAD_IMAGES" -> {
                val imageList = intent.getSerializableExtra("imageList") as List<Image>
                val internalStoragePath = intent.getSerializableExtra("internalStoragePath") as String
                receiver = intent.getParcelableExtra("receiver")
                val isRegular = intent.getBooleanExtra("isRegular", false)

                handleLoadImages(imageList, internalStoragePath, isRegular)
            }
        }
    }

    private fun handleLoadImages(dataset: List<Image>, path: String, isRegular: Boolean) {
        val folder = if (isRegular) "regular" else "previews"

        for (image in dataset) {
            val file = InternalStorageUtilities.fromInternalStorage("$path/$folder", image.id)!!

            if (!file.exists()) {
                val downloadedImage = ImageUtilities.downloadImageByURL(
                    if (isRegular) image.urls?.regular
                    else image.urls?.small
                )

                if (downloadedImage != null) {
                    InternalStorageUtilities.saveToInternalStorage(file.absolutePath, downloadedImage)

                    if (isRegular) {
                        image.localRegularPath = file.absolutePath
                    } else {
                        image.localPreviewPath = file.absolutePath
                    }

                    val data = Bundle().apply {
                        putSerializable("image", image as Serializable)
                    }
                    receiver.send(200, data)
                } else {
                    Log.i(LOG_TAG, "ImageLoaderService failure: unable to access image preview")
                    receiver.send(400, Bundle())
                }
            } else {
                if (isRegular) {
                    image.localRegularPath = file.absolutePath
                } else {
                    image.localPreviewPath = file.absolutePath
                }

                val data = Bundle().apply {
                    putSerializable("image", image as Serializable)
                }
                receiver.send(200, data)
            }
        }
    }

}

class ImageLoaderServiceReceiver(handler: Handler) : ResultReceiver(handler) {

    interface Receiver {
        fun onReceiveResult(resultCode: Int, data: Bundle)
    }

    private var resultReceiver: Receiver? = null

    fun setReceiver(receiver: Receiver?) {
        resultReceiver = receiver
    }

    override fun onReceiveResult(resultCode: Int, data: Bundle) {
        if (resultReceiver != null) {
            resultReceiver!!.onReceiveResult(resultCode, data)
        }
    }
}
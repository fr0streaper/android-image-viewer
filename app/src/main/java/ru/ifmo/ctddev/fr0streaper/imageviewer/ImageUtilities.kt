package ru.ifmo.ctddev.fr0streaper.imageviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.util.concurrent.Executors


class ImageUtilities {

    companion object {

        private val executor = Executors.newFixedThreadPool(4)

        fun downloadImageByURL(imageURL: String?): Bitmap? {
            val url = URL(imageURL)

            return BitmapFactory.decodeStream(url.openConnection().getInputStream())
        }

        fun downloadRegularImage(context: Context, image: Image, receiver: ImageLoaderServiceReceiver) {
            executor.execute {
                try {
                    ImageLoaderService.downloadImages(context.applicationContext, mutableListOf(image), receiver, true)
                } catch (e: Exception) {
                    Log.d(LOG_TAG, "Unable to download images; Exception: $e")
                }
            }
        }

        fun downloadPreviewList(context: Context, page: Int, receiver: ImageLoaderServiceReceiver) {
            val url =
                URL("${UNSPLASH_API_URL}photos?per_page=$PHOTOS_PER_PAGE&page=$page&client_id=$UNSPLASH_API_ACCESS_KEY")

            executor.execute {
                try {
                    val builder = OkHttpClient.Builder()
                    val client = builder.build()
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()

                    if (response.code() != 200) {
                        Log.d(LOG_TAG, "Server request failure")
                        return@execute
                    }
                    val dataset =
                        jacksonObjectMapper().readValue<List<Image>>(response?.body()?.string()!!)

                    ImageLoaderService.downloadImages(context.applicationContext, dataset, receiver, false)
                } catch (e: Exception) {
                    Log.d(LOG_TAG, "Unable to download images; Exception: $e")
                }
            }
        }

    }
}
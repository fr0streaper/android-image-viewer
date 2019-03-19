package ru.ifmo.ctddev.fr0streaper.imageviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.ifmo.ctddev.fr0streaper.imageviewer.database.Database
import ru.ifmo.ctddev.fr0streaper.imageviewer.database.FavoriteImage
import java.net.URL


class ImageUtilities {

    companion object {

        lateinit var database: Database

        private fun imageFromString(serializedImage: String): Image {
            return jacksonObjectMapper().readerFor(Image::class.java)
                .readValue<Image>(serializedImage)
        }

        private fun stringFromImage(serializedImage: Image): String {
            return jacksonObjectMapper().writeValueAsString(serializedImage)
        }

        fun insertFavorite(image: Image) {
            database.favoritesDAO().insert(FavoriteImage(0, image.id!!, stringFromImage(image)))
        }

        fun deleteFavorite(image: Image) {
            database.favoritesDAO().delete(getByImageId(image)!!)
        }

        private fun getFavoritesPage(page: Int): List<Image> {
            val favoriteImages =
                database.favoritesDAO().getFavoritesRange((page - 1) * PHOTOS_PER_PAGE + 1, page * PHOTOS_PER_PAGE)
            val result = mutableListOf<Image>()
            favoriteImages.mapTo(result) { favoriteImage -> imageFromString(favoriteImage.serializedImage) }
            return result
        }

        private fun getByImageId(image: Image): FavoriteImage? {
            val favorite = database.favoritesDAO().getFavorite(image.id!!)
            return if (favorite.isEmpty()) null else favorite[0]
        }

        fun isFavorite(image: Image): Boolean {
            val favorite = getByImageId(image)
            return favorite != null
        }

        fun loadFavoritesPage(context: Context, page: Int, receiver: ImageLoaderServiceReceiver) {
            GlobalScope.launch(Dispatchers.IO) {
                val dataset = getFavoritesPage(page)
                ImageLoaderService.downloadImages(context.applicationContext, dataset, receiver, false)
            }
        }

        private fun constructQuery(page: Int, query: String): HttpUrl {
            val url = if (query.isEmpty()) "${UNSPLASH_API_URL}photos" else "${UNSPLASH_API_URL}search/photos"

            val requestBuilder = HttpUrl.parse(url)!!.newBuilder()

            if (!query.isEmpty()) {
                requestBuilder.addQueryParameter("query", query)
            }

            requestBuilder
                .addQueryParameter("page", page.toString())
                .addQueryParameter("per_page", PHOTOS_PER_PAGE.toString())
                .addQueryParameter("client_id", UNSPLASH_API_ACCESS_KEY)


            return requestBuilder.build()
        }

        fun downloadImageByURL(imageURL: String?): Bitmap? {
            val url = URL(imageURL)

            return BitmapFactory.decodeStream(url.openConnection().getInputStream())
        }

        fun downloadRegularImage(context: Context, image: Image, receiver: ImageLoaderServiceReceiver) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    ImageLoaderService.downloadImages(context.applicationContext, mutableListOf(image), receiver, true)
                } catch (e: Exception) {
                    Log.d(LOG_TAG, "Unable to download images; Exception: $e")
                }
            }
        }

        fun downloadPreviewList(context: Context, page: Int, receiver: ImageLoaderServiceReceiver, query: String = "") {
            val jsonQuery = constructQuery(page, query)

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val builder = OkHttpClient.Builder()
                    val client = builder.build()
                    val request = Request.Builder().url(jsonQuery).build()
                    val response = client.newCall(request).execute()

                    if (response.code() != 200) {
                        Log.d(LOG_TAG, "Server request failure")
                        GlobalScope.launch(Dispatchers.Main) {
                            Toast.makeText(context, "Server request failed", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    val dataset: List<Image> = if (query.isEmpty()) {
                        jacksonObjectMapper().readValue(response.body()?.string()!!)
                    } else {
                        val node = jacksonObjectMapper().readTree(response.body()?.string()!!).get("results")
                        jacksonObjectMapper().readValue(node.toString())
                    }

                    ImageLoaderService.downloadImages(context.applicationContext, dataset, receiver, false)
                } catch (e: Exception) {
                    Log.d(LOG_TAG, "Unable to download images; Exception: $e")
                }
            }
        }

    }
}
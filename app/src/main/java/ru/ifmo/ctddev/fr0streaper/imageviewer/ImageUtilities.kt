package ru.ifmo.ctddev.fr0streaper.imageviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
                database.favoritesDAO().getFavoritesRange((page - 1) * PHOTOS_PER_PAGE, PHOTOS_PER_PAGE)
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

        fun downloadImageByURL(imageURL: String?): Bitmap? {
            val url = URL(imageURL)

            return BitmapFactory.decodeStream(url.openConnection().getInputStream())
        }

        fun downloadRegularImage(context: Context, image: Image, receiver: ImageLoaderServiceReceiver) {
            ImageLoaderService.downloadImages(context.applicationContext, mutableListOf(image), receiver, true)
        }

        private fun onServerRequestFailed(context: Context) {
            Log.d(LOG_TAG, "Server request failure")
            Toast.makeText(context, "Server request failed", Toast.LENGTH_SHORT).show()
        }

        fun downloadPreviewList(context: Context, page: Int, receiver: ImageLoaderServiceReceiver, query: String = "") {
                val retrofit = Retrofit.Builder()
                .baseUrl(UNSPLASH_API_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(HttpRequestService::class.java)

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    if (query.isEmpty()) {
                        retrofit.getDefaultImagePage(page).enqueue(object : Callback<List<Image>> {
                            override fun onFailure(call: Call<List<Image>>, t: Throwable) = onServerRequestFailed(context)

                            override fun onResponse(call: Call<List<Image>>, response: Response<List<Image>>) {
                                ImageLoaderService.downloadImages(context.applicationContext, response.body()!!, receiver, false)
                            }
                        })
                    }
                    else {
                        retrofit.getSearchImagePage(query, page).enqueue(object : Callback<SearchResult> {
                            override fun onFailure(call: Call<SearchResult>, t: Throwable) = onServerRequestFailed(context)

                            override fun onResponse(call: Call<SearchResult>, response: Response<SearchResult>) {
                                val node = jacksonObjectMapper().readTree(response.body()!!.toString()).toString()
                                val dataset = jacksonObjectMapper().readValue<SearchResult>(node).results
                                ImageLoaderService.downloadImages(context.applicationContext, dataset!!, receiver, false)
                            }
                        })
                    }
                } catch (e: Exception) {
                    Log.d(LOG_TAG, "Unable to download images; Exception: $e")
                }
            }
        }

    }
}
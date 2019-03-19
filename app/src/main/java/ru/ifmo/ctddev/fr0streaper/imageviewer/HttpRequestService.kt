package ru.ifmo.ctddev.fr0streaper.imageviewer

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface HttpRequestService {

    @GET("photos?per_page=$PHOTOS_PER_PAGE&client_id=$UNSPLASH_API_ACCESS_KEY")
    fun getDefaultImagePage(@Query("page") page: Int): Call<List<Image>>

    @GET("search/photos?&per_page=$PHOTOS_PER_PAGE&client_id=$UNSPLASH_API_ACCESS_KEY")
    fun getSearchImagePage(@Query("query") query: String, @Query("page") page: Int):  Call<SearchResult>

}
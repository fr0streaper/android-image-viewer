package ru.ifmo.ctddev.fr0streaper.imageviewer.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface FavoritesDAO {

    @Insert
    fun insert(favoriteImage: FavoriteImage)

    @Delete
    fun delete(favoriteImage: FavoriteImage)

    @Query("SELECT * FROM favorites LIMIT (:limit) OFFSET (:offset)")
    fun getFavoritesRange(offset: Int, limit: Int): List<FavoriteImage>

    @Query("SELECT * FROM favorites WHERE imageId = (:imageId)")
    fun getFavorite(imageId: String): List<FavoriteImage>

}
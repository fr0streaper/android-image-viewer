package ru.ifmo.ctddev.fr0streaper.imageviewer.database

import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Database

@Database(entities = arrayOf(FavoriteImage::class), version = 1)
abstract class Database : RoomDatabase() {

    abstract fun favoritesDAO(): FavoritesDAO

}
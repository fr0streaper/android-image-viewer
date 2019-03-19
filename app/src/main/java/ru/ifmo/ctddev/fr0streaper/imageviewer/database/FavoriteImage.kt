package ru.ifmo.ctddev.fr0streaper.imageviewer.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteImage(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var imageId: String,
    var serializedImage: String
)
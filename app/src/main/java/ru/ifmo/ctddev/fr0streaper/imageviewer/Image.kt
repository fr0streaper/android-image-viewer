package ru.ifmo.ctddev.fr0streaper.imageviewer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

//@JsonIgnoreProperties(ignoreUnknown = true)
data class ImageUrls(
    val regular: String?,
    val small: String?
) : Serializable

//@JsonIgnoreProperties(ignoreUnknown = true)
data class User(val name: String?) : Serializable

//@JsonIgnoreProperties(ignoreUnknown = true)
data class Image(
    val id: String?,
    val user: User?,
    var localPreviewPath: String?,
    var localRegularPath: String?,
    val description: String?,
    val urls: ImageUrls?
) : Serializable

data class SearchResult(
    val results: List<Image>? = null
)
package dev.reeve.torrustapi

import com.google.gson.annotations.SerializedName

data class Category(val name: String, @SerializedName("num_torrents") val numberOfTorrents: Int)
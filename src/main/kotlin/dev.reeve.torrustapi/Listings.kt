package dev.reeve.torrustapi

import com.google.gson.annotations.SerializedName

data class Listings(
	val results: ArrayList<WebListings>,
	val total: Int
) {
	open class WebListings(
		@SerializedName("category_id")
		val categoryId: Int,
		val description: String,
		@SerializedName("file_size")
		val fileSize: String,
		@SerializedName("info_hash")
		val infoHash: String,
		val leechers: Int,
		val seeders: Int,
		val title: String,
		@SerializedName("torrent_id")
		val torrentId: Long,
		@SerializedName("upload_date")
		val uploadDate: Long,
		val uploader: String
	)
}
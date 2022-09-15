package dev.reeve.torrustapi

/**
 * This is a class to store all the weblisting results from Torrust
 * It's pretty dumb tbh, but it's parsing the json and storing it by variable names, and I figured I might as well store all it gives me
 */
data class Listings(
	val results: ArrayList<WebListing>,
	val total: Int
) {
	open class WebListing(
		val categoryId: Int,
		val description: String,
		val fileSize: String,
		val infoHash: String,
		val leechers: Int,
		val seeders: Int,
		val title: String,
		val torrentId: Long,
		val uploadDate: Long,
		val uploader: String
	)
}
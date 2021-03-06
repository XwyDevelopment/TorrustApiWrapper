package dev.reeve.torrustapi

class FullWebListing(
	categoryId: Int,
	description: String,
	fileSize: String,
	val files: ArrayList<File>,
	leechers: Int,
	seeders: Int,
	title: String,
	torrentId: Long,
	uploadDate: Long,
	uploader: String
) : Listings.WebListings(
	categoryId,
	description,
	fileSize,
	fileSize,
	leechers,
	seeders,
	title,
	torrentId,
	uploadDate,
	uploader
) {
	class File(
		val length: Long,
		val md5sum: String,
		val path: ArrayList<String>
	)
}
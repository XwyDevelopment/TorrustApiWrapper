package dev.reeve.torrustapi

class FullWebListing(
	categoryId: Int,
	description: String,
	fileSize: String,
	infoHash: String,
	leechers: Int,
	seeders: Int,
	title: String,
	torrentId: Long,
	uploadDate: Long,
	uploader: String,
	val files: ArrayList<File>
) : Listings.WebListing(
	categoryId,
	description,
	fileSize,
	infoHash,
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
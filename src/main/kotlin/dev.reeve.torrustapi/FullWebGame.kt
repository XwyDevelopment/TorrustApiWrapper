package dev.reeve.torrustapi

class FullWebGame(
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
) : GameListings.WebGame(
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
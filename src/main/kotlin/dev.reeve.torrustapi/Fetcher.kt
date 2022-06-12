package dev.reeve.torrustapi

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.buffer
import okio.sink
import java.io.File

class Fetcher(private var baseURL: String) {
	private val client = OkHttpClient()
	private val JSON = "application/json".toMediaTypeOrNull()
	private val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
	
	init {
		baseURL += "/api/"
	}
	
	/*
	* Grab the list of games
	* Blocks the current thread, should probably be run async
	* Based on this url found on the site
	* https://dl.rpdl.net/api/torrents?page_size=20&page=0&sort=uploaded_DESC&categories=&search=
	*/
	fun getGames(user: User, sorting: Sorting, sortingOrder: SortingOrder, limit: Int, page: Int): GameListings {
		val url = baseURL + "torrents?" +
				"page_size=${limit}" +
				"&page=${page}" +
				"&sort=${sorting.name.lowercase()}_${sortingOrder.name}" +
				"&categories=" +
				"&search="
		
		return getData(user, url)
	}
	
	fun getGame(user: User, game: Long): FullWebGame {
		val url = baseURL + "torrent/${game}"
		return getData(user, url)
	}
	
	fun getTorrentFile(user: User, game: Long): File {
		val url = baseURL + "torrent/download/${game}"
		val request = Request.Builder().url(url).get().addAuth(user).build()
		val response = client.newCall(request).execute()
		
		val game = getGame(user, game)
		val name = game.title + ".torrent"
		val file = File("./torrentFiles", name)
		file.parentFile.mkdirs()
		
		val sink = file.sink().buffer()
		sink.writeAll(response.body.source())
		sink.close()
		
		return file
	}
	
	fun getName(user: User): String {
		val url = baseURL + "settings/name"
		return getData(user, url)
	}
	
	fun banUser(user: User, name: String): String {
		val url = baseURL + "user/ban/${name}"
		return deleteData(user, url)
	}
	
	fun deleteTorrent(user: User, game: Long): TorrentResponse {
		val url = baseURL + "torrent/${game}"
		return deleteData(user, url)
	}
	
	fun uploadTorrent(user: User, title: String, description: String, category: String, file: File): TorrentResponse {
		val url = baseURL + "torrent/upload"
		
		val body = MultipartBody.Builder()
			.setType(MultipartBody.FORM)
			.addFormDataPart("title", title)
			.addFormDataPart("description", description)
			.addFormDataPart("category", category)
			//.addFormDataPart()
		
		val request = Request.Builder().url(url).post(RequestBody.create(MultipartBody.FORM)).addAuth(user).build()
		val response = client.newCall(request).execute()
		
		return getWrappedData(response.body!!.string())
	}
	
	fun login(username: String, password: String): User {
		val url = baseURL + "user/login"
		val login = Login(username, password)
		val request = Request.Builder().url(url).post(gson.toJson(login).toRequestBody(JSON)).build()
		
		val response = client.newCall(request).execute()
		
		return getWrappedData(response.body.string())
	}
	
	private inline fun <reified T> deleteData(user: User, url: String): T {
		val request = Request.Builder().url(url).delete().addAuth(user).build()
		val response = client.newCall(request).execute()
		
		return getWrappedData(response.body.string())
	}
	
	private inline fun <reified T> getData(user: User, url: String): T {
		val request = Request.Builder().url(url).get().addAuth(user).build()
		val response = client.newCall(request).execute()
		
		return getWrappedData(response.body.string())
	}
	
	private inline fun <reified T> getWrappedData(body: String): T {
		return gson.fromJson<DataWrapper<T>>(
			body,
			TypeToken.getParameterized(DataWrapper::class.java, T::class.java).type
		).data
	}
	
	private fun Request.Builder.addAuth(user: User): Request.Builder {
		addHeader("authorization", "Bearer ${user.token}")
		return this
	}
	
	enum class Sorting {
		Name,
		Seeders,
		Leechers,
		Uploaded,
		Size,
		Uploader
	}
	
	enum class SortingOrder {
		ASC,
		DESC
	}
}
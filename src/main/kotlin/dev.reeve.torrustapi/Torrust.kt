package dev.reeve.torrustapi

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.buffer
import okio.sink
import java.io.File

/**
 * This class is used to interact with the Torrust API.
 * @param baseURL the main url of the url (including `/torrust` if it's there)
 * @notice This class requires the use of a login
 */
class Torrust(private var baseURL: String) {
	private val client = OkHttpClient()
	private val JSON = "application/json".toMediaTypeOrNull()
	private val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
	
	init {
		if (baseURL.endsWith("/")) baseURL.removeSuffix("/")
		
		if (!baseURL.endsWith("/api")) {
			baseURL += "/api/"
		}
	}
	
	/**
	 *
	 * Grab the list of all posts
	 * Blocks the current thread, should probably be run async
	 * @param page The page to get, starts at 0
	 * Based on this url found on the site
	 * https://dl.rpdl.net/api/torrents?page_size=20&page=0&sort=uploaded_DESC&categories=&search=
	 */
	fun getListings(
		user: User,
		search: String = "",
		categories: Array<String> = emptyArray(),
		sorting: Sorting = Sorting.Uploaded,
		sortingOrder: SortingOrder = SortingOrder.DESC,
		limit: Int = 50,
		page: Int = 0
	): Listings {
		val url = baseURL + "torrents?" +
				"page_size=${limit}" +
				"&page=${page}" +
				"&sort=${sorting.name.lowercase()}_${sortingOrder.name}" +
				"&categories=${categories.joinToString(separator = ",")}" + // works with multiple as a comma separated list
				"&search=${search.filter { it.isLetterOrDigit() }}" // looks like on the website everything other than letters and numbers are removed
		
		return getData(user, url)
	}
	
	fun getWebListing(user: User, id: Long): FullWebListing {
		val url = baseURL + "torrent/${id}"
		return getData(user, url)
	}
	
	fun downloadTorrentFile(user: User, id: Long): File {
		val url = baseURL + "torrent/download/${id}"
		val request = Request.Builder().url(url).get().addAuth(user).build()
		val response = client.newCall(request).execute()
		
		val webListing = getWebListing(user, id)
		val name = webListing.title + ".torrent"
		val file = File("./torrentFiles", name)
		file.parentFile.mkdirs()
		
		val sink = file.sink().buffer()
		sink.writeAll(response.body!!.source())
		sink.close()
		
		return file
	}
	
	fun getWebsiteName(user: User): String {
		val url = baseURL + "settings/name"
		return getData(user, url)
	}
	
	fun getWebsitePublicSettings(user: User): PublicSettings {
		val url = baseURL + "settings/public"
		return getData(user, url)
	}
	
	fun getCategories(user: User): Array<Category> {
		val url = baseURL + "category"
		return getData(user, url)
	}
	
	fun banUser(user: User, name: String): String {
		val url = baseURL + "user/ban/${name}"
		return deleteData(user, url)
	}
	
	fun deleteTorrent(user: User, id: Long): TorrentResponse {
		val url = baseURL + "torrent/${id}"
		return deleteData(user, url)
	}
	
	@Deprecated("This doesn't really work, so I would advise you to stay away for now")
	internal fun uploadTorrent(user: User, title: String, description: String, category: String, file: File): TorrentResponse {
		val url = baseURL + "torrent/upload"
		
		val body = MultipartBody.Builder()
			.setType(MultipartBody.FORM)
			.addFormDataPart("title", title)
			.addFormDataPart("description", description)
			.addFormDataPart("category", category)
			.build()
		
		val request = Request.Builder().url(url).post(body).addAuth(user).build()
		val response = client.newCall(request).execute()
		
		return getWrappedData(response.body!!.string())
	}
	
	fun login(username: String, password: String): User {
		val url = baseURL + "user/login"
		val login = Login(username, password)
		val request = Request.Builder().url(url).post(gson.toJson(login).toRequestBody(JSON)).build()
		
		val response = client.newCall(request).execute()
		
		return getWrappedData(response.body!!.string())
	}
	
	private inline fun <reified T> deleteData(user: User, url: String): T {
		val request = Request.Builder().url(url).delete().addAuth(user).build()
		val response = client.newCall(request).execute()
		
		return getWrappedData(response.body!!.string())
	}
	
	private inline fun <reified T> getData(user: User, url: String): T {
		val request = Request.Builder().url(url).get().addAuth(user).build()
		val response = client.newCall(request).execute()
		
		return getWrappedData(response.body!!.string())
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
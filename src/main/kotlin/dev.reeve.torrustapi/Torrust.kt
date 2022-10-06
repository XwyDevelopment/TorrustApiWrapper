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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KFunction

/**
 * This class is used to interact with the Torrust API.
 * @param baseURL the main url of the url (including `/torrust` if it's there)
 * @notice This class requires the use of a login
 */
open class Torrust(private var baseURL: String) {
	val calls = HashMap<String, Long>()
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
		search: String = "",
		categories: Array<String> = emptyArray(),
		sorting: Sorting = Sorting.Uploaded,
		sortingOrder: SortingOrder = SortingOrder.DESC,
		limit: Int = 50,
		page: Int = 0
	): Listings? {
		val url =
			baseURL + "torrents?" + "page_size=${limit}" + "&page=${page}" + "&sort=${sorting.name.lowercase()}_${sortingOrder.name}" + "&categories=${
				categories.joinToString(separator = ",")
			}" + // works with multiple as a comma separated list
					"&search=${search.filter { it.isLetterOrDigit() }}" // looks like on the website everything other than letters and numbers are removed
		
		increaseCalls(::getListings)
		return getData(null, url)
	}
	
	fun getWebListing(id: Long): FullWebListing? {
		val url = baseURL + "torrent/${id}"
		increaseCalls(::getWebListing)
		return getData(null, url)
	}
	
	/**
	 *
	 */
	fun getNewWebListings(lastCheck: Date?, category: Category? = null): Listings? {
		if (lastCheck != null) {
			var page = 0
			var lastPage = -1
			val pageSize = 50
			
			if ((getListings(
					limit = 1,
					categories = if (category != null) arrayOf(category.name) else emptyArray(),
					sorting = Sorting.Uploaded,
					sortingOrder = SortingOrder.DESC
				)?.results?.firstOrNull()?.uploadDate ?: error("Could not get listings")) < lastCheck.time
			) {
				println("Tried checking for new listings, but the latest one is older than last check")
				return Listings(ArrayList(), 0)
			}
			
			while (true) {
				val listings = getListings(
					limit = pageSize,
					page = page,
					categories = if (category != null) arrayOf(category.name) else emptyArray(),
					sorting = Sorting.Uploaded,
					sortingOrder = SortingOrder.DESC
				) ?: break
				
				if (listings.results.isEmpty()) {
					return getListings(
						limit = page * pageSize,
						categories = if (category != null) arrayOf(category.name) else emptyArray(),
						sorting = Sorting.Uploaded,
						sortingOrder = SortingOrder.DESC
					)
				}
				
				if (listings.results.last().uploadDate > lastCheck.time) {
					if (lastPage < page) {
						lastPage = page
						page++
					} else {
						return getListings(
							limit = page * pageSize + listings.results.size,
							categories = if (category != null) arrayOf(category.name) else emptyArray(),
							sorting = Sorting.Uploaded,
							sortingOrder = SortingOrder.DESC
						)
					}
				} else {
					for (i in listings.results.indices.reversed()) {
						if (listings.results[i].uploadDate > lastCheck.time) {
							return getListings(
								limit = page * pageSize + i,
								categories = if (category != null) arrayOf(category.name) else emptyArray(),
								sorting = Sorting.Uploaded,
								sortingOrder = SortingOrder.DESC
							)
						}
					}
					page--
				}
			}
			
			return Listings(ArrayList(), 0)
		} else {
			return getListings(
				limit = -1,
				categories = if (category != null) arrayOf(category.name) else emptyArray(),
				sorting = Sorting.Uploaded,
				sortingOrder = SortingOrder.DESC
			)
		}
	}
	
	fun downloadTorrentFile(user: User, id: Long): File? {
		val url = baseURL + "torrent/download/${id}"
		val request = Request.Builder().url(url).get().addAuth(user).build()
		val response = client.newCall(request).execute()
		
		val webListing = getWebListing(id) ?: return null
		val name = webListing.title + ".torrent"
		val file = File("./torrentFiles", name)
		file.parentFile.mkdirs()
		
		val sink = file.sink().buffer()
		sink.writeAll(response.body!!.source())
		sink.close()
		response.close()
		
		return file
	}
	
	fun getWebsiteName(): String? {
		val url = baseURL + "settings/name"
		increaseCalls(::getWebsiteName)
		return getData(null, url)
	}
	
	fun getWebsitePublicSettings(): PublicSettings? {
		val url = baseURL + "settings/public"
		increaseCalls(::getWebsitePublicSettings)
		return getData(null, url)
	}
	
	fun getCategories(): Array<Category>? {
		val url = baseURL + "category"
		increaseCalls(::getCategories)
		return getData(null, url)
	}
	
	fun getTotalTorrents(): Int? {
		return getCategories()?.sumOf { it.numberOfTorrents }
	}
	
	fun banUser(user: User, name: String): String {
		val url = baseURL + "user/ban/${name}"
		return deleteData(user, url)
	}
	
	fun deleteTorrent(user: User, id: Long): TorrentResponse {
		val url = baseURL + "torrent/${id}"
		increaseCalls(::deleteTorrent)
		return deleteData(user, url)
	}
	
	@Deprecated("This doesn't really work, so I would advise you to stay away for now")
	internal fun uploadTorrent(user: User, title: String, description: String, category: String, file: File): TorrentResponse {
		val url = baseURL + "torrent/upload"
		
		val body =
			MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("title", title).addFormDataPart("description", description)
				.addFormDataPart("category", category).build()
		
		val request = Request.Builder().url(url).post(body).addAuth(user).build()
		client.newCall(request).execute().use {
			increaseCalls(::uploadTorrent)
			return getWrappedData(it.body!!.string())
		}
	}
	
	fun login(username: String, password: String): User {
		val url = baseURL + "user/login"
		val login = Login(username, password)
		val request = Request.Builder().url(url).post(gson.toJson(login).toRequestBody(JSON)).build()
		
		client.newCall(request).execute().use {
			increaseCalls(::login)
			return getWrappedData(it.body!!.string())
		}
	}
	
	private inline fun <reified T> deleteData(user: User, url: String): T {
		val request = Request.Builder().url(url).delete().addAuth(user).build()
		client.newCall(request).execute().use {
			return getWrappedData(it.body!!.string())
		}
	}
	
	private inline fun <reified T> getData(user: User?, url: String): T? {
		val request = Request.Builder().url(url).get().addAuth(user).build()
		client.newCall(request).execute().use {
			if (it.code == 400) {
				return null
			}
			
			return getWrappedData(it.body!!.string())
		}
	}
	
	private inline fun <reified T> getWrappedData(body: String): T {
		return gson.fromJson<DataWrapper<T>>(
			body, TypeToken.getParameterized(DataWrapper::class.java, T::class.java).type
		).data
	}
	
	private fun Request.Builder.addAuth(user: User?): Request.Builder {
		if (user != null) addHeader("authorization", "Bearer ${user.token}")
		return this
	}
	
	private fun increaseCalls(function: KFunction<*>) {
		calls.putIfAbsent(function.name, 0)
		calls[function.name] = calls[function.name]!! + 1
	}
	
	enum class Sorting {
		Name, Seeders, Leechers, Uploaded, Size, Uploader
	}
	
	enum class SortingOrder {
		ASC, DESC
	}
}
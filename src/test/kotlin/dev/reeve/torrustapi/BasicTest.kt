package dev.reeve.torrustapi

import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest

class BasicTest {
	private val site = "https://dl.rpdl.net/"
	private val torrust = Torrust(site)
	private lateinit var user: User
	
	@BeforeTest
	fun login() {
		user = torrust.login("reeve567", "rmc1124Erin!")
	}
	
	@Test
	fun getListings() {
		val listings = torrust.getListings(user)
		println("Listings: ${listings?.results?.size}")
		assert(listings?.results?.isNotEmpty() ?: false)
	}
	
	@Test
	fun get100Listings() {
		val listings = torrust.getListings(user, limit = 100)
		println("Listings: ${listings?.results?.size}")
		assert(listings?.results?.size == 100)
	}
	
	@Test
	fun getAllListings() {
		val listings= torrust.getListings(user, limit = -1)
		println("Listings: ${listings?.results?.size}")
		assert(listings != null && listings.results.size > 100)
	}
	
	@Test
	fun getName() {
		val name = torrust.getWebsiteName(user) ?: error("Name is null")
		println("Website name: $name")
		assert(name == (torrust.getWebsitePublicSettings(user)?.websiteName ?: error("Name is null")))
	}
	
	@Test
	fun getCategories() {
		val categories = torrust.getCategories(user) ?: error("No categories")
		println(categories.joinToString("\n") {
			it.name + ": " + it.numberOfTorrents
		})
		assert(categories.isNotEmpty())
	}
	
	@Test
	fun getDescription() {
		val game = torrust.getListings(user, limit = 1)?.results?.firstOrNull()
		
		assert(game != null)
		
		println(game!!.description)
	}
	
	@Test
	fun getTorrentById() {
		val listing = torrust.getWebListing(user, -1000)
		assert(listing == null)
	}
}
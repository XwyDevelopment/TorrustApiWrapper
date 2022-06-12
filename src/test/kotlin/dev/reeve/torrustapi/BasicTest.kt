package dev.reeve.torrustapi

import org.junit.Before
import org.junit.Test

class BasicTest {
	private val site = "https://dl.rpdl.net/"
	private val torrust = Torrust(site)
	private lateinit var user: User
	
	@Before
	fun login() {
		user = torrust.login("?", "?")
	}
	
	@Test
	fun getListings() {
		val listings = torrust.getListings(user)
		println("Listings: ${listings.results.size}")
		assert(listings.results.isNotEmpty())
	}
	
	@Test
	fun get100Listings() {
		val listings = torrust.getListings(user, limit = 100)
		println("Listings: ${listings.results.size}")
		assert(listings.results.size == 100)
	}
	
	@Test
	fun getAllListings() {
		val listings= torrust.getListings(user, limit = -1)
		println("Listings: ${listings.results.size}")
		assert(listings.results.size > 100)
	}
	
	@Test
	fun getName() {
		val name = torrust.getWebsiteName(user)
		println("Website name: $name")
		assert(name == torrust.getWebsitePublicSettings(user).websiteName)
	}
	
	@Test
	fun getCategories() {
		val categories = torrust.getCategories(user)
		println(categories.joinToString("\n") {
			it.name + ": " + it.numberOfTorrents
		})
		assert(categories.isNotEmpty())
	}
}
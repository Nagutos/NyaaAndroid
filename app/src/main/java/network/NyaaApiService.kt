package com.nagutos.nyaaandroid.network

import com.nagutos.nyaaandroid.model.RssFeed
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NyaaApiService {
    @GET("/")
    suspend fun getRssFeed(
        @Query("page") page: String = "rss", // Toujours "rss"
        @Query("q") query: String = "",      // Pour la recherche (vide par défaut)
        @Query("c") category: String = "0_0" // "0_0" = Toutes les catégories
    ): RssFeed
}

object NyaaNetwork {
    private const val BASE_URL = "https://nyaa.si/"

    @Suppress("DEPRECATION")
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .build()

    // variable used in the app
    val api: NyaaApiService = retrofit.create(NyaaApiService::class.java)
}
package com.nagutos.nyaaandroid.network

import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface NyaaApiService {
    @GET("/")
    suspend fun getTorrentsHtml(
        @Query("f") filter: Int = 0,
        @Query("c") category: String = "0_0",
        @Query("q") query: String = "",
        @Query("p") page: Int = 1,
        @Query("u") user: String? = null,
        @Query("s") sort: String? = null,
        @Query("o") order: String? = null
    ): ResponseBody
    @GET
    suspend fun getTorrentDetailHtml(@Url url: String): ResponseBody
}

object NyaaNetwork {
    private const val BASE_URL = "https://nyaa.si/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .build()

    val api: NyaaApiService = retrofit.create(NyaaApiService::class.java)
}
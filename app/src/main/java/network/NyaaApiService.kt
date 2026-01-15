package com.nagutos.nyaaandroid.network.NyaaApiService

import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface NyaaApiService {
    @GET("/")
    suspend fun getTorrentsHtml(
        @Query("q") query: String = "",
        @Query("c") category: String = "0_0",
        @Query("p") page: Int = 1
    ): ResponseBody
}

object NyaaNetwork {
    private const val BASE_URL = "https://nyaa.si/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .build()

    val api: NyaaApiService = retrofit.create(NyaaApiService::class.java)
}
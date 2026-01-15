package com.nagutos.nyaaandroid.network.NyaaApiService

import okhttp3.ResponseBody
import retrofit2.Retrofit
// Retirez SimpleXmlConverterFactory si vous ne l'utilisez plus ailleurs
// import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NyaaApiService {
    // On change le type de retour en ResponseBody (contenu brut)
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
        // Pas besoin de converter XML ici, on veut juste le texte brut
        .build()

    val api: NyaaApiService = retrofit.create(NyaaApiService::class.java)
}
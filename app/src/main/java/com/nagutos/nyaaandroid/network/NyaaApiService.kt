package com.nagutos.nyaaandroid.network

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url

interface NyaaApiService {
    // A single @Url endpoint: the repository builds the full absolute URL (list or detail,
    // nyaa.si or sukebei.nyaa.si) so one Retrofit instance serves both indexes.
    @GET
    suspend fun getHtml(@Url url: String): ResponseBody
}

object NyaaNetwork {
    private const val BASE_URL = "https://nyaa.si/"

    // A browser-like User-Agent: nyaa/sukebei sit behind Cloudflare, which can 403 the
    // default OkHttp UA. Setting it here fixes intermittent load failures.
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Android) NyaaAndroid")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        // baseUrl is required but unused: every call passes an absolute @Url.
        .baseUrl(BASE_URL)
        .client(client)
        .build()

    val api: NyaaApiService = retrofit.create(NyaaApiService::class.java)
}

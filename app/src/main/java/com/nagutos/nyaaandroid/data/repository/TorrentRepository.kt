package com.nagutos.nyaaandroid.data.repository

import com.nagutos.nyaaandroid.model.NyaaSite
import com.nagutos.nyaaandroid.model.TorrentDetail
import com.nagutos.nyaaandroid.model.TorrentUI
import com.nagutos.nyaaandroid.network.NyaaApiService
import com.nagutos.nyaaandroid.network.NyaaHtmlParser
import com.nagutos.nyaaandroid.network.NyaaNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * Single entry point for fetching torrent data from nyaa.si / sukebei.nyaa.si.
 *
 * Owns the network call + HTML parsing so ViewModels only deal with domain models
 * (TorrentUI / TorrentDetail). Fetching and Jsoup parsing run on Dispatchers.IO.
 */
class TorrentRepository(
    private val api: NyaaApiService = NyaaNetwork.api
) {
    suspend fun getTorrents(
        query: String,
        category: String,
        page: Int,
        user: String?,
        sort: String,
        order: String,
        filter: Int = 0,
        site: NyaaSite = NyaaSite.NYAA
    ): List<TorrentUI> = withContext(Dispatchers.IO) {
        val url = "${site.baseUrl}/".toHttpUrl().newBuilder().apply {
            addQueryParameter("f", filter.toString())
            addQueryParameter("c", category)
            if (query.isNotBlank()) addQueryParameter("q", query)
            addQueryParameter("p", page.toString())
            if (!user.isNullOrBlank()) addQueryParameter("u", user)
            addQueryParameter("s", sort)
            addQueryParameter("o", order)
        }.build().toString()

        val body = api.getHtml(url)
        NyaaHtmlParser.parseTorrents(body.string(), site)
    }

    suspend fun getDetail(url: String): TorrentDetail = withContext(Dispatchers.IO) {
        // detailUrl is absolute for new listings; older favorites may still hold a relative
        // "/view/id", which predates Sukebei support and is therefore always a nyaa.si link.
        val absolute = if (url.startsWith("http")) url else "${NyaaSite.NYAA.baseUrl}$url"
        val body = api.getHtml(absolute)
        NyaaHtmlParser.parseDetail(body.string())
    }
}

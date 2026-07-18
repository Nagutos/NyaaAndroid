package com.nagutos.nyaaandroid.data.repository

import com.nagutos.nyaaandroid.model.TorrentDetail
import com.nagutos.nyaaandroid.model.TorrentUI
import com.nagutos.nyaaandroid.network.NyaaApiService
import com.nagutos.nyaaandroid.network.NyaaHtmlParser
import com.nagutos.nyaaandroid.network.NyaaNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Single entry point for fetching torrent data from nyaa.si.
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
        filter: Int = 0
    ): List<TorrentUI> = withContext(Dispatchers.IO) {
        val body = api.getTorrentsHtml(
            filter = filter,
            query = query,
            category = category,
            page = page,
            user = user,
            sort = sort,
            order = order
        )
        NyaaHtmlParser.parseTorrents(body.string())
    }

    suspend fun getDetail(url: String): TorrentDetail = withContext(Dispatchers.IO) {
        val body = api.getTorrentDetailHtml(url)
        NyaaHtmlParser.parseDetail(body.string())
    }
}

package com.nagutos.nyaaandroid.ui.components

import com.nagutos.nyaaandroid.data.local.entity.FavoriteTorrent
import com.nagutos.nyaaandroid.model.TorrentDetail
import com.nagutos.nyaaandroid.model.TorrentUI

fun FavoriteTorrent.toTorrentUI(): TorrentUI {
    return TorrentUI(
        id = this.id,
        title = this.title,
        category = this.category,
        size = this.size,
        date = this.date,
        seeders = this.seeders.toIntOrNull() ?: 0,
        leechers = this.leechers.toIntOrNull() ?: 0,
        downloads = 0,
        linkUrl = "",
        detailUrl = this.detailUrl
    )
}

fun TorrentDetail.toTorrentUI(id: String, url: String): TorrentUI {
    return TorrentUI(
        id = id,
        title = this.title,
        category = this.category,
        size = this.totalSize,
        date = this.date,
        seeders = this.seeders.toIntOrNull() ?: 0,
        leechers = this.leechers.toIntOrNull() ?: 0,
        downloads = 0,
        linkUrl = "",
        detailUrl = url
    )
}
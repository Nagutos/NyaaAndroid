package com.nagutos.nyaaandroid.model

data class TorrentUI(
    val id: String,
    val title: String,
    val category: String,
    val size: String,
    val date: String,
    val seeders: Int,
    val leechers: Int,
    val downloads: Int,
    val linkUrl: String,
    val detailUrl: String,
    val commentsCount: Int = 0
)

data class TorrentDetail(
    val title: String,
    val magnetLink: String,
    val torrentFile: String,
    val descriptionHtml: String,
    val infoHash: String,
    val submitter: String,
    val comments: List<Comment>,
    val fileTree: List<TorrentFile> = emptyList()
)

data class Comment(
    val user: String,
    val date: String,
    val content: String
)

data class TorrentFile(
    val name: String,
    val size: String,
    val isDirectory: Boolean,
    val children: List<TorrentFile> = emptyList()
)

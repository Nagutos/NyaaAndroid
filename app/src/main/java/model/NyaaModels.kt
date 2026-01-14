package com.nagutos.nyaaandroid.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

// --- 1. XML Model (RSS) ---

@Root(name = "rss", strict = false)
@NamespaceList(
    Namespace(reference = "http://purl.org/dc/elements/1.1/", prefix = "dc"),
    Namespace(reference = "http://www.w3.org/2005/Atom", prefix = "atom"),
    Namespace(reference = "https://nyaa.si/xmlns/nyaa", prefix = "nyaa")
)
data class RssFeed(
    @field:Element(name = "channel")
    var channel: RssChannel? = null
)

@Root(name = "channel", strict = false)
data class RssChannel(
    @field:ElementList(inline = true, name = "item")
    var items: MutableList<RssItem> = ArrayList()
)

@Root(name = "item", strict = false)
data class RssItem(
    @field:Element(name = "title")
    var title: String = "",

    @field:Element(name = "link")
    var link: String = "",

    @field:Element(name = "guid")
    var guid: String = "",

    @field:Element(name = "pubDate")
    var pubDate: String = "",

    @field:Element(name = "categoryId")
    @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa")
    var categoryId: String = "",

    @field:Element(name = "size")
    @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa")
    var size: String = "",

    @field:Element(name = "seeders")
    @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa")
    var seeders: Int = 0,

    @field:Element(name = "leechers")
    @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa")
    var leechers: Int = 0,

    @field:Element(name = "downloads")
    @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa")
    var downloads: Int = 0,

    // Le hash n'est pas toujours dans le RSS de base,
    // mais parfois dans infoHash selon la version du feed
    @field:Element(name = "infoHash", required = false)
    @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa")
    var infoHash: String = ""
)

// --- 2. UI Model ---

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

// --- 3. Conversion function ---

fun RssItem.toUiModel(): TorrentUI {
    // Nettoyage de la date (ex: "Wed, 14 Jan 2026 13:58:00 +0000" -> "14 Jan 13:58")
    val cleanDate = try {
        pubDate.substringBeforeLast(" +")
    } catch (e: Exception) { pubDate }

    val categoryLabel = when(categoryId) {
        "1_1" -> "Anime Music Video"
        "1_2" -> "Anime (Eng)"
        "1_3" -> "Anime (Non-Eng)"
        "1_4" -> "Anime (Raw)"
        "2_1" -> "Audio (Lossless)"
        "2_2" -> "Audio (Lossy)"
        "3_1" -> "Manga (Eng)"
        "3_2" -> "Manga (Non-Eng)"
        "3_3" -> "Manga (Raw)"
        else -> "Autre"
    }

    return TorrentUI(
        id = guid.substringAfterLast("/"), // Extrait l'ID de l'URL
        title = title,
        category = categoryLabel,
        size = size,
        date = cleanDate,
        seeders = seeders,
        leechers = leechers,
        downloads = downloads,
        linkUrl = link,
        detailUrl = guid
    )
}
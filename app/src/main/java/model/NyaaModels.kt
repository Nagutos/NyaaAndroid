package com.nagutos.nyaaandroid.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

// --- 1. MODÈLES RSS (Pour la liste rapide, si besoin) ---
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
    @field:Element(name = "title") var title: String = "",
    @field:Element(name = "link") var link: String = "",
    @field:Element(name = "guid") var guid: String = "",
    @field:Element(name = "pubDate") var pubDate: String = "",
    @field:Element(name = "categoryId") @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa") var categoryId: String = "",
    @field:Element(name = "size") @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa") var size: String = "",
    @field:Element(name = "seeders") @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa") var seeders: Int = 0,
    @field:Element(name = "leechers") @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa") var leechers: Int = 0,
    @field:Element(name = "downloads") @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa") var downloads: Int = 0,
    @field:Element(name = "infoHash", required = false) @field:Namespace(reference = "https://nyaa.si/xmlns/nyaa") var infoHash: String = ""
)

// --- 2. MODÈLE UI PRINCIPAL (Liste) ---
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

// --- 3. MODÈLE DÉTAIL (Page spécifique) ---
// C'est ceux-là qui manquaient peut-être !
data class TorrentDetail(
    val title: String,
    val magnetLink: String,
    val torrentFile: String,
    val descriptionHtml: String,
    val infoHash: String,
    val submitter: String,
    val comments: List<Comment>
)

data class Comment(
    val user: String,
    val date: String,
    val content: String
)

// --- 4. CONVERSION (Mapper RSS -> UI) ---
fun RssItem.toUiModel(): TorrentUI {
    val cleanDate = try { pubDate.substringBeforeLast(" +") } catch (e: Exception) { pubDate }

    // C'est ICI que tout se joue : Les textes doivent être EXACTS
    val categoryLabel = when(categoryId) {
        // --- 1. ANIME ---
        "1_1" -> "Anime - AMV"
        "1_2" -> "Anime - English"
        "1_3" -> "Anime - Non-English"
        "1_4" -> "Anime - Raw"

        // --- 2. AUDIO ---
        "2_1" -> "Audio - Lossless"
        "2_2" -> "Audio - Lossy"

        // --- 3. LITTÉRATURE ---
        "3_1" -> "Literature - English"
        "3_2" -> "Literature - Non-English"
        "3_3" -> "Literature - Raw"

        // --- 4. LIVE ACTION ---
        "4_1" -> "Live Action - English"
        "4_2" -> "Live Action - Idol/PV"
        "4_3" -> "Live Action - Non-English"
        "4_4" -> "Live Action - Raw"

        // --- 5. IMAGES ---
        "5_1" -> "Pictures - Graphics"
        "5_2" -> "Pictures - Photos"

        // --- 6. LOGICIELS ---
        "6_1" -> "Software - Apps"
        "6_2" -> "Software - Games"

        else -> "Autre"
    }

    return TorrentUI(
        id = guid.substringAfterLast("/"),
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
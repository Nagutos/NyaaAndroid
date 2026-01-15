package com.nagutos.nyaaandroid.network

import com.nagutos.nyaaandroid.model.TorrentUI
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object NyaaHtmlParser {

    fun parseTorrents(html: String): List<TorrentUI> {
        val doc = Jsoup.parse(html)
        val rows = doc.select("table.torrent-list tbody tr")

        return rows.mapNotNull { row ->
            try {
                parseRow(row)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun parseRow(row: Element): TorrentUI {
        val cells = row.select("td")

        // 1. Récupération de l'ID de catégorie (ex: "1_2") via le lien
        val categoryLink = cells[0].select("a").attr("href")
        // Le lien est souvent "/?c=1_2" ou "/c/1_2", on nettoie pour garder "1_2"
        val categoryId = categoryLink.substringAfter("c=")

        // 2. Titre et ID
        val titleLinks = cells[1].select("a:not(.comments)")
        val titleElement = titleLinks.last()
        val title = titleElement?.text() ?: "Inconnu"
        val detailUrl = titleElement?.attr("href") ?: ""
        val id = detailUrl.substringAfter("/view/")

        // 3. Lien Magnet
        val links = cells[2].select("a")
        val magnet = links.find { it.attr("href").startsWith("magnet") }?.attr("href")
            ?: links.first()?.attr("href") ?: ""

        // 4. Infos diverses
        val size = cells[3].text()
        val date = cells[4].text()
        val seeders = cells[5].text().toIntOrNull() ?: 0
        val leechers = cells[6].text().toIntOrNull() ?: 0
        val downloads = cells[7].text().toIntOrNull() ?: 0

        // 5. ON APPELLE LA NOUVELLE FONCTION DE LABEL ICI
        val fullCategoryLabel = getCategoryLabel(categoryId)

        return TorrentUI(
            id = id,
            title = title,
            category = fullCategoryLabel, // On passe le label précis (ex: "Audio - Lossless")
            size = size,
            date = date,
            seeders = seeders,
            leechers = leechers,
            downloads = downloads,
            linkUrl = magnet,
            detailUrl = detailUrl
        )
    }

    // FONCTION IDENTIQUE À CELLE DU MODÈLE (C'est elle qui manquait !)
    private fun getCategoryLabel(id: String): String {
        return when(id) {
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
    }

    // --- PARSING DÉTAIL (Ne change pas) ---
    fun parseDetail(html: String): com.nagutos.nyaaandroid.model.TorrentDetail {
        val doc = Jsoup.parse(html)
        val title = doc.select("h3.panel-title").first()?.text()?.replace("File details", "")?.trim() ?: "Inconnu"
        val downloadLink = doc.select("a[href^=magnet:]").attr("href")
        val torrentFileLink = doc.select("a[href$=.torrent]").attr("href")
        val descriptionHtml = doc.select("#torrent-description").html()
        val infoHash = doc.select("kbd").first()?.text() ?: ""
        val submitter = doc.select("a[href^=/user/]").first()?.text() ?: "Anonyme"

        val comments = doc.select("div.panel-default:has(div.comment-panel)").map { element ->
            val user = element.select("div.col-md-2 a").text()
            val content = element.select("div.comment-content").text()
            val date = element.select("small[title]").attr("title")
            com.nagutos.nyaaandroid.model.Comment(user, date, content)
        }

        return com.nagutos.nyaaandroid.model.TorrentDetail(
            title = title,
            magnetLink = downloadLink,
            torrentFile = torrentFileLink,
            descriptionHtml = descriptionHtml,
            infoHash = infoHash,
            submitter = submitter,
            comments = comments
        )
    }
}
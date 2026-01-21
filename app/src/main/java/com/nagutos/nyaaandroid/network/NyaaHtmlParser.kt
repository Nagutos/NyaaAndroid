package com.nagutos.nyaaandroid.network

import com.nagutos.nyaaandroid.model.Comment
import com.nagutos.nyaaandroid.model.TorrentFile
import com.nagutos.nyaaandroid.ui.components.TorrentFileListView
import com.nagutos.nyaaandroid.ui.components.FileListItem
import com.nagutos.nyaaandroid.model.TorrentDetail
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

        // 1. Retrieve the category ID (e.g., “1_2”) via the link
        val categoryLink = cells[0].select("a").attr("href")
        val categoryId = categoryLink.substringAfter("c=")

        // 2. Title and ID
        val titleLinks = cells[1].select("a:not(.comments)")
        val titleElement = titleLinks.last()
        val title = titleElement?.text() ?: "Inconnu"
        val detailUrl = titleElement?.attr("href") ?: ""
        val id = detailUrl.substringAfter("/view/")

        // 3. Magnet Link
        val links = cells[2].select("a")
        val magnet = links.find { it.attr("href").startsWith("magnet") }?.attr("href")
            ?: links.first()?.attr("href") ?: ""

        // 4. Miscellaneous information
        val size = cells[3].text()
        val date = cells[4].text()
        val seeders = cells[5].text().toIntOrNull() ?: 0
        val leechers = cells[6].text().toIntOrNull() ?: 0
        val downloads = cells[7].text().toIntOrNull() ?: 0

        val fullCategoryLabel = getCategoryLabel(categoryId)

        return TorrentUI(
            id = id,
            title = title,
            category = fullCategoryLabel,
            size = size,
            date = date,
            seeders = seeders,
            leechers = leechers,
            downloads = downloads,
            linkUrl = magnet,
            detailUrl = detailUrl
        )
    }

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

    // --- PARSING DETAIL ---
    fun parseDetail(html: String): TorrentDetail {
        val doc = Jsoup.parse(html)
        val title = doc.select("h3.panel-title").first()?.text()?.replace("File details", "")?.trim() ?: "Inconnu"
        val downloadLink = doc.select("a[href^=magnet:]").attr("href")
        val torrentFileLink = doc.select("a[href$=.torrent]").attr("href")
        val descriptionHtml = doc.select("#torrent-description").html()
        val infoHash = doc.select("kbd").first()?.text() ?: ""
        val submitter = doc.select("a[href^=/user/]").first()?.text() ?: "Anonyme"

        // LOGIQUE CORRIGÉE : On récupère l'arbre et on ne fait rien d'autre ici
        val rootUl = doc.select(".torrent-file-list > ul").first()
        val fileTree = if (rootUl != null) parseRecursive(rootUl) else emptyList()

        val comments = doc.select("div.panel-default:has(div.comment-panel)").map { element ->
            val user = element.select("div.col-md-2 a").text()
            val content = element.select("div.comment-content").text()
            val date = element.select("small[title]").attr("title")
            Comment(user, date, content)
        }

        return TorrentDetail(
            title = title,
            magnetLink = downloadLink,
            torrentFile = torrentFileLink,
            descriptionHtml = descriptionHtml,
            infoHash = infoHash,
            submitter = submitter,
            comments = comments,
            fileTree = fileTree
        )
    }

    // Changement du type de retour : List<TorrentFile> au lieu de FileNode
    private fun parseRecursive(ulElement: Element): List<TorrentFile> {
        val nodes = mutableListOf<TorrentFile>()

        ulElement.children().filter { it.tagName() == "li" }.forEach { li ->
            val isDir = li.select("i.fa-folder, i.fa-folder-open").isNotEmpty()
            val size = li.select(".file-size").first()?.text() ?: ""

            val name = li.ownText().trim().ifEmpty {
                li.text().replace(size, "").trim()
            }

            val children = if (isDir) {
                val childUl = li.select("ul").first()
                if (childUl != null) parseRecursive(childUl) else emptyList()
            } else {
                emptyList()
            }

            // On crée des objets TorrentFile
            nodes.add(TorrentFile(name, size, isDir, children))
        }
        return nodes
    }
}
package com.nagutos.nyaaandroid.data.repository

import kotlinx.coroutines.flow.Flow
import com.nagutos.nyaaandroid.data.local.entity.FavoriteDao
import com.nagutos.nyaaandroid.data.local.entity.FavoriteTorrent
import com.nagutos.nyaaandroid.data.local.entity.SavedSearch
import com.nagutos.nyaaandroid.data.local.entity.SavedSearchDao
import com.nagutos.nyaaandroid.model.TorrentUI

class FavoriteRepository(private val favoriteDao: FavoriteDao,
                         private val savedSearchDao: SavedSearchDao){

    val allFavorites = favoriteDao.getAllFavorites()

    val allSavedSearches = savedSearchDao.getAllSavedSearches()

    fun isFavorite(torrentId: String): Flow<Boolean> = favoriteDao.isFavorite(torrentId)

    suspend fun addFavorite(torrent: TorrentUI) {
        val favorite = FavoriteTorrent(
            id = torrent.id,
            title = torrent.title,
            category = torrent.category,
            size = torrent.size,
            date = torrent.date,
            seeders = torrent.seeders.toString(),
            leechers = torrent.leechers.toString(),
            detailUrl = torrent.detailUrl
        )
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun deleteById(torrentId: String) {
        favoriteDao.deleteById(torrentId)
    }

    suspend fun insertSavedSearch(search: SavedSearch) {
        savedSearchDao.insertSearch(search)
    }

    suspend fun deleteSavedSearch(search: SavedSearch) {
        savedSearchDao.deleteSearch(search)
    }
}
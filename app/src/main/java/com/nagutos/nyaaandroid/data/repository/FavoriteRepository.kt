package com.nagutos.nyaaandroid.data.repository

import kotlinx.coroutines.flow.Flow
import com.nagutos.nyaaandroid.data.local.entity.FavoriteDao
import com.nagutos.nyaaandroid.data.local.entity.FavoriteTorrent
import com.nagutos.nyaaandroid.model.TorrentUI

class FavoriteRepository(private val favoriteDao: FavoriteDao) {

    val allFavorites: Flow<List<FavoriteTorrent>> = favoriteDao.getAllFavorites()

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

    // Supprimer un favori (on crée un objet temporaire avec l'ID pour que Room le trouve)
    suspend fun removeFavorite(torrentId: String) {
        // Pour supprimer, Room a besoin de l'objet complet ou d'une requête spécifique.
        // On va simplifier avec une requête directe dans le DAO plus tard ou utiliser celle-ci :
        // favoriteDao.deleteById(torrentId) -> à ajouter dans le DAO si besoin
    }
}
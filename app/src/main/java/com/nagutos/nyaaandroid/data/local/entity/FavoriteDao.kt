package com.nagutos.nyaaandroid.data.local.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteTorrent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(torrent: FavoriteTorrent)

    @Query("DELETE FROM favorites WHERE id = :torrentId")
    suspend fun deleteById(torrentId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :torrentId)")
    fun isFavorite(torrentId: String): Flow<Boolean>
}
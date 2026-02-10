package com.nagutos.nyaaandroid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_searches")
data class SavedSearch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,      // Name given by the user (e.g. "Clean Anime")
    val query: String,      // The query (e.g. VOSTFR - "Tsundere-Raws")
    val category: String,   // The associated Nyaa category
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface SavedSearchDao {
    @Query("SELECT * FROM saved_searches ORDER BY createdAt DESC")
    fun getAllSavedSearches(): Flow<List<SavedSearch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SavedSearch)

    @Delete
    suspend fun deleteSearch(search: SavedSearch)
}
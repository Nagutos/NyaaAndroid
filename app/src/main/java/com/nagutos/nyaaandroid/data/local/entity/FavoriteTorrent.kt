package com.nagutos.nyaaandroid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteTorrent(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val size: String,
    val date: String,
    val seeders: String,
    val leechers: String,
    val detailUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)
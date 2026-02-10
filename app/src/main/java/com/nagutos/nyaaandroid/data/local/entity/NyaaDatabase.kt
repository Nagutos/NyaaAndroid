package com.nagutos.nyaaandroid.data.local.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteTorrent::class, SavedSearch::class], version = 2, exportSchema = false)
abstract class NyaaDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    abstract fun savedSearchDao(): SavedSearchDao
    companion object {
        @Volatile
        private var INSTANCE: NyaaDatabase? = null

        fun getDatabase(context: Context): NyaaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NyaaDatabase::class.java,
                    "nyaa_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
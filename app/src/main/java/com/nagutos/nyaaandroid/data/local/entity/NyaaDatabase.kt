package com.nagutos.nyaaandroid.data.local.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteTorrent::class, SavedSearch::class], version = 3, exportSchema = true)
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
                )
                    // Version 3 is our baseline. From here on, every schema change MUST ship a
                    // Migration in Migrations.ALL so user favorites and saved searches survive.
                    .addMigrations(*Migrations.ALL)
                    // Only the pre-baseline dev versions (1, 2) may be reset destructively — their
                    // schemas were never exported so no migration can be written for them. Any
                    // upgrade from version 3 onward without a matching migration will fail loudly
                    // instead of silently wiping data.
                    .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1, 2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.nagutos.nyaaandroid.data.local.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteTorrent::class], version = 1)
abstract class NyaaDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: NyaaDatabase? = null

        fun getDatabase(context: Context): NyaaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NyaaDatabase::class.java,
                    "nyaa_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
package com.nagutos.nyaaandroid.data.local.entity

import androidx.room.migration.Migration

/**
 * Room migrations for [NyaaDatabase].
 *
 * Version 3 is the exported baseline (see app/schemas). When you change any entity:
 *   1. bump the @Database version,
 *   2. add a Migration below (e.g. MIGRATION_3_4) with the ALTER/CREATE statements,
 *   3. register it in [ALL].
 * Room validates each migration against the exported schema at build time, so a mismatch
 * fails the build instead of wiping the user's favorites at runtime.
 *
 * Example for a future column:
 *   val MIGRATION_3_4 = Migration(3, 4) { db ->
 *       db.execSQL("ALTER TABLE favorites ADD COLUMN note TEXT NOT NULL DEFAULT ''")
 *   }
 */
object Migrations {
    // Add real migrations here as the schema evolves, then list them in ALL.
    val ALL: Array<Migration> = arrayOf()
}

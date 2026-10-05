package com.personal.autoscroll.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AppProfileEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AutoScrollDatabase : RoomDatabase() {
    abstract fun appProfileDao(): AppProfileDao
}


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE app_profiles ADD COLUMN overlayOrientation TEXT NOT NULL DEFAULT 'Vertical'",
        )
        db.execSQL(
            """
            DELETE FROM app_profiles
            WHERE id NOT IN (
                SELECT MAX(id)
                FROM app_profiles
                GROUP BY packageName
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_app_profiles_packageName ON app_profiles(packageName)",
        )
    }
}

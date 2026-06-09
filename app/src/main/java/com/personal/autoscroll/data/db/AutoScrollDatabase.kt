package com.personal.autoscroll.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppProfileEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AutoScrollDatabase : RoomDatabase() {
    abstract fun appProfileDao(): AppProfileDao
}

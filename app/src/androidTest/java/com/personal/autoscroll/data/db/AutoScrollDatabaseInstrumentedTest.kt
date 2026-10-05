package com.personal.autoscroll.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.OverlayOrientation
import com.personal.autoscroll.domain.model.PresetType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AutoScrollDatabaseInstrumentedTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @After
    fun cleanupMigrationDatabase() {
        context.deleteDatabase(MIGRATION_DB)
    }

    @Test
    fun migrate1To2PreservesNewestProfileAndAddsUniqueIndex() = runBlocking {
        context.deleteDatabase(MIGRATION_DB)
        val databaseFile = context.getDatabasePath(MIGRATION_DB)
        databaseFile.parentFile?.mkdirs()

        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { database ->
            database.execSQL(V1_CREATE_TABLE)
            database.execSQL(v1Insert(id = 1L, appName = "Older", updatedAt = 100L))
            database.execSQL(v1Insert(id = 2L, appName = "Newest", updatedAt = 200L))
            database.version = 1
        }

        val database = Room.databaseBuilder(
            context,
            AutoScrollDatabase::class.java,
            MIGRATION_DB,
        )
            .addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        try {
            val writableDatabase = database.openHelper.writableDatabase

            writableDatabase.query(
                "SELECT appName, overlayOrientation FROM app_profiles WHERE packageName = '$PACKAGE_NAME'",
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Newest", cursor.getString(0))
                assertEquals("Vertical", cursor.getString(1))
                assertEquals(1, cursor.count)
            }

            var foundUniquePackageIndex = false
            writableDatabase.query("PRAGMA index_list('app_profiles')").use { cursor ->
                val nameColumn = cursor.getColumnIndexOrThrow("name")
                val uniqueColumn = cursor.getColumnIndexOrThrow("unique")
                while (cursor.moveToNext()) {
                    if (
                        cursor.getString(nameColumn) == "index_app_profiles_packageName" &&
                        cursor.getInt(uniqueColumn) == 1
                    ) {
                        foundUniquePackageIndex = true
                    }
                }
            }
            assertTrue(foundUniquePackageIndex)

            val restored = database.appProfileDao().getProfile(PACKAGE_NAME)?.toDomain()
            assertNotNull(restored)
            assertEquals("Newest", restored?.appName)
            assertEquals(OverlayOrientation.Vertical, restored?.overlayConfig?.orientation)
        } finally {
            database.close()
        }
    }

    @Test
    fun savingSamePackageReplacesExistingProfile() = runBlocking {
        withDatabase { database ->
            val dao = database.appProfileDao()
            val original = profile(appName = "Original")
            val updated = profile(appName = "Updated")

            dao.upsertProfile(original.toEntity())
            dao.upsertProfile(updated.toEntity())

            val stored = dao.getProfile(PACKAGE_NAME)
            assertNotNull(stored)
            assertEquals("Updated", stored?.appName)
        }
    }

    @Test
    fun overlayOrientationSurvivesDatabaseRoundTrip() = runBlocking {
        withDatabase { database ->
            val dao = database.appProfileDao()
            val profile = profile(appName = "Horizontal").copy(
                overlayConfig = profile(appName = "Horizontal").overlayConfig.copy(
                    orientation = OverlayOrientation.Horizontal,
                ),
            )

            dao.upsertProfile(profile.toEntity())

            val restored = dao.getProfile(PACKAGE_NAME)?.toDomain()
            assertEquals(OverlayOrientation.Horizontal, restored?.overlayConfig?.orientation)
        }
    }

    private suspend fun withDatabase(block: suspend (AutoScrollDatabase) -> Unit) {
        val database = Room.inMemoryDatabaseBuilder(
            context,
            AutoScrollDatabase::class.java,
        ).allowMainThreadQueries().build()

        try {
            block(database)
        } finally {
            database.close()
        }
    }

    private fun profile(appName: String): AppProfile =
        AppProfile.defaultForPackage(
            packageName = PACKAGE_NAME,
            appName = appName,
            presetType = PresetType.VideoFeed,
            nowMillis = 123L,
        )

    private fun v1Insert(
        id: Long,
        appName: String,
        updatedAt: Long,
    ): String =
        """
        INSERT INTO app_profiles (
            id, packageName, appName, enabled, presetType, profileStatus,
            intentDirection, gestureAxis, distancePercent, startXPercent, startYPercent,
            speedLevel, invertPhysicalDirection, scrollMode, delayMillis, startDelayMillis,
            repeatCount, durationMillis, stopOnAppChange, compactPositionX, compactPositionY,
            expandedPositionX, expandedPositionY, opacity, overlaySize, showNextPrevious,
            autoCollapse, createdAt, updatedAt
        ) VALUES (
            $id, '$PACKAGE_NAME', '$appName', 1, 'VideoFeed', 'Untested',
            'NextItem', 'Vertical', 55, 50, 72,
            600, 0, 'UntilStop', 6500, 0,
            NULL, NULL, 1, 24, 240,
            24, 160, 0.92, 'Medium', 1,
            1, 100, $updatedAt
        )
        """.trimIndent()

    private companion object {
        const val PACKAGE_NAME = "com.example.video"
        const val MIGRATION_DB = "migration-1-2-test"

        const val V1_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS app_profiles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "packageName TEXT NOT NULL, " +
                "appName TEXT NOT NULL, " +
                "enabled INTEGER NOT NULL, " +
                "presetType TEXT NOT NULL, " +
                "profileStatus TEXT NOT NULL, " +
                "intentDirection TEXT NOT NULL, " +
                "gestureAxis TEXT NOT NULL, " +
                "distancePercent INTEGER NOT NULL, " +
                "startXPercent INTEGER NOT NULL, " +
                "startYPercent INTEGER NOT NULL, " +
                "speedLevel INTEGER NOT NULL, " +
                "invertPhysicalDirection INTEGER NOT NULL, " +
                "scrollMode TEXT NOT NULL, " +
                "delayMillis INTEGER NOT NULL, " +
                "startDelayMillis INTEGER NOT NULL, " +
                "repeatCount INTEGER, " +
                "durationMillis INTEGER, " +
                "stopOnAppChange INTEGER NOT NULL, " +
                "compactPositionX INTEGER NOT NULL, " +
                "compactPositionY INTEGER NOT NULL, " +
                "expandedPositionX INTEGER NOT NULL, " +
                "expandedPositionY INTEGER NOT NULL, " +
                "opacity REAL NOT NULL, " +
                "overlaySize TEXT NOT NULL, " +
                "showNextPrevious INTEGER NOT NULL, " +
                "autoCollapse INTEGER NOT NULL, " +
                "createdAt INTEGER NOT NULL, " +
                "updatedAt INTEGER NOT NULL" +
                ")"
    }
}

package com.personal.autoscroll.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.OverlayOrientation
import com.personal.autoscroll.domain.model.PresetType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AutoScrollDatabaseInstrumentedTest {
    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AutoScrollDatabase::class.java,
    )

    @After
    fun cleanupMigrationDatabase() {
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .deleteDatabase(MIGRATION_DB)
    }

    @Test
    fun migrate1To2PreservesNewestProfileAndAddsUniqueIndex() {
        migrationHelper.createDatabase(MIGRATION_DB, 1).apply {
            execSQL(v1Insert(id = 1L, appName = "Older", updatedAt = 100L))
            execSQL(v1Insert(id = 2L, appName = "Newest", updatedAt = 200L))
            close()
        }

        val migrated = migrationHelper.runMigrationsAndValidate(
            MIGRATION_DB,
            2,
            true,
            MIGRATION_1_2,
        )

        migrated.query(
            "SELECT appName, overlayOrientation FROM app_profiles WHERE packageName = '$PACKAGE_NAME'",
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Newest", cursor.getString(0))
            assertEquals("Vertical", cursor.getString(1))
            assertEquals(1, cursor.count)
        }

        var foundUniquePackageIndex = false
        migrated.query("PRAGMA index_list('app_profiles')").use { cursor ->
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
        migrated.close()
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
        val context = ApplicationProvider.getApplicationContext<Context>()
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
    }
}

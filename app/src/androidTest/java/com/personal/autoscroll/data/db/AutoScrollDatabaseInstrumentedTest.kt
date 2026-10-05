package com.personal.autoscroll.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.OverlayOrientation
import com.personal.autoscroll.domain.model.PresetType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AutoScrollDatabaseInstrumentedTest {
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

    private companion object {
        const val PACKAGE_NAME = "com.example.video"
    }
}

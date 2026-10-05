package com.personal.autoscroll.core.permissions

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityConsentStoreInstrumentedTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun clearConsent() {
        preferences().edit().clear().commit()
    }

    @After
    fun cleanupConsent() {
        preferences().edit().clear().commit()
    }

    @Test
    fun consentStartsRejectedAndBecomesAccepted() {
        val store = AccessibilityConsentStore(context)

        assertFalse(store.isAccepted())
        assertFalse(store.accepted.value)

        store.accept()

        assertTrue(store.isAccepted())
        assertTrue(store.accepted.value)
    }

    @Test
    fun staleDisclosureVersionIsNotAccepted() {
        preferences().edit()
            .putInt(AccessibilityConsentStore.DISCLOSURE_VERSION_KEY, 0)
            .commit()

        val store = AccessibilityConsentStore(context)

        assertFalse(store.isAccepted())
    }

    private fun preferences() =
        context.getSharedPreferences(
            AccessibilityConsentStore.PREFERENCES_NAME,
            Context.MODE_PRIVATE,
        )
}

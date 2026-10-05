package com.personal.autoscroll.core.permissions

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AccessibilityConsentStore @Inject constructor(
    @param:ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )
    private val _accepted = MutableStateFlow(readAccepted())

    val accepted: StateFlow<Boolean> = _accepted.asStateFlow()

    fun isAccepted(): Boolean = readAccepted()

    fun accept() {
        preferences.edit()
            .putInt(DISCLOSURE_VERSION_KEY, CURRENT_DISCLOSURE_VERSION)
            .apply()
        _accepted.value = true
    }

    private fun readAccepted(): Boolean =
        preferences.getInt(DISCLOSURE_VERSION_KEY, 0) == CURRENT_DISCLOSURE_VERSION

    internal companion object {
        const val CURRENT_DISCLOSURE_VERSION = 1
        const val PREFERENCES_NAME = "accessibility_consent"
        const val DISCLOSURE_VERSION_KEY = "disclosure_version"
    }
}

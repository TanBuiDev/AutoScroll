package com.personal.autoscroll.core.accessibility

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ForegroundAppObserver @Inject constructor() {
    val foregroundPackage: Flow<String?> = AccessibilityServiceState.foregroundPackage

    fun foregroundPackageExcluding(packageName: String): Flow<String?> = flow {
        var lastExternalPackage: String? = null
        foregroundPackage.collect { foreground ->
            if (foreground != null && foreground != packageName) {
                lastExternalPackage = foreground
            }
            emit(lastExternalPackage)
        }
    }
}

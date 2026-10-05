package com.personal.autoscroll.core.gesture

import com.personal.autoscroll.core.accessibility.AutoScrollAccessibilityService
import com.personal.autoscroll.domain.model.GestureConfig
import javax.inject.Inject

class GestureExecutor @Inject constructor(
    private val gestureMapper: GestureMapper,
) {
    suspend fun execute(config: GestureConfig): Boolean {
        val dispatcher = AutoScrollAccessibilityService.dispatcherOrNull() ?: return false
        return dispatcher.dispatch(gestureMapper.map(config))
    }
}

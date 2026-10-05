package com.personal.autoscroll.domain.usecase

import com.personal.autoscroll.core.gesture.GestureExecutor
import com.personal.autoscroll.domain.model.AppProfile
import javax.inject.Inject

class RunTestGesture @Inject constructor(
    private val gestureExecutor: GestureExecutor,
) {
    suspend operator fun invoke(profile: AppProfile): Boolean =
        gestureExecutor.execute(profile.gestureConfig)
}

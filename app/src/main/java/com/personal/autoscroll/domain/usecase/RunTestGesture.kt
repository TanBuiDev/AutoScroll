package com.personal.autoscroll.domain.usecase

import com.personal.autoscroll.core.accessibility.GestureDispatcher
import com.personal.autoscroll.core.gesture.GestureMapper
import com.personal.autoscroll.domain.model.AppProfile
import javax.inject.Inject

class RunTestGesture @Inject constructor(
    private val gestureMapper: GestureMapper,
) {
    suspend operator fun invoke(
        profile: AppProfile,
        dispatcher: GestureDispatcher?,
    ): Boolean {
        if (dispatcher == null) return false
        return dispatcher.dispatch(gestureMapper.map(profile.gestureConfig))
    }
}

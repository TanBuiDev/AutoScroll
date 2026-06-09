package com.personal.autoscroll.domain.model

enum class ScrollMode {
    Once,
    Repeat,
    UntilStop,
    Timer,
}

data class TimingConfig(
    val mode: ScrollMode,
    val delayMillis: Long,
    val startDelayMillis: Long,
    val repeatCount: Int?,
    val durationMillis: Long?,
    val stopOnAppChange: Boolean,
) {
    companion object {
        fun videoFeedDefault(): TimingConfig = TimingConfig(
            mode = ScrollMode.UntilStop,
            delayMillis = 6_500L,
            startDelayMillis = 0L,
            repeatCount = null,
            durationMillis = null,
            stopOnAppChange = true,
        )
    }
}

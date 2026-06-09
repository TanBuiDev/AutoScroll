package com.personal.autoscroll.core.overlay

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.GestureConfig.Companion.MAX_SWIPE_DURATION_MILLIS
import com.personal.autoscroll.domain.model.GestureConfig.Companion.MIN_SWIPE_DURATION_MILLIS
import com.personal.autoscroll.domain.model.IntentDirection
import com.personal.autoscroll.domain.model.OverlayOrientation
import com.personal.autoscroll.domain.model.ScrollMode

data class ExpandedOverlayActions(
    val onModeChanged: (ScrollMode) -> Unit,
    val onDirectionChanged: (IntentDirection) -> Unit,
    val onDelayChanged: (Long) -> Unit,
    val onSwipeDurationChanged: (Long) -> Unit,
    val onOverlayOrientationChanged: (OverlayOrientation) -> Unit,
    val onOpacityChanged: (Float) -> Unit,
    val onRepeatCountChanged: (Int) -> Unit,
    val onDurationChanged: (Long) -> Unit,
    val onTest: () -> Unit,
    val onSave: () -> Unit,
)

class ExpandedOverlay(
    context: Context,
    private var profile: AppProfile,
    private val actions: ExpandedOverlayActions,
) {
    private val modeRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
    }
    private val directionRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
    }
    private val overlayOrientationRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
    }
    private val conditionalRow = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }

    val view: View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.START
        setPadding(20, 16, 20, 16)
        setBackgroundColor(Color.argb(240, 15, 17, 21))

        addView(label(context, profile.appName, size = 16f, color = Color.WHITE))
        addView(label(context, profile.packageName, size = 12f, color = Color.rgb(168, 172, 179)))

        addView(sectionLabel(context, context.getString(R.string.setting_mode)))
        addView(modeRow)

        addView(sectionLabel(context, context.getString(R.string.setting_direction)))
        addView(directionRow)

        addView(stepperRow(
            context = context,
            label = context.getString(R.string.setting_delay),
            valueText = { "${profile.timingConfig.delayMillis / 1000.0}s" },
            onMinus = {
                updateProfile {
                    copy(timingConfig = timingConfig.copy(delayMillis = (timingConfig.delayMillis - 500L).coerceAtLeast(500L)))
                }
                actions.onDelayChanged(profile.timingConfig.delayMillis)
            },
            onPlus = {
                updateProfile {
                    copy(timingConfig = timingConfig.copy(delayMillis = (timingConfig.delayMillis + 500L).coerceAtMost(60_000L)))
                }
                actions.onDelayChanged(profile.timingConfig.delayMillis)
            },
        ))

        addView(stepperRow(
            context = context,
            label = context.getString(R.string.setting_swipe),
            valueText = { profile.gestureConfig.swipeDurationMillis.asSecondsText() },
            onMinus = {
                updateProfile {
                    copy(
                        gestureConfig = gestureConfig.copy(
                            swipeDurationMillis = (gestureConfig.swipeDurationMillis - 100L)
                                .coerceAtLeast(MIN_SWIPE_DURATION_MILLIS),
                        ),
                    )
                }
                actions.onSwipeDurationChanged(profile.gestureConfig.swipeDurationMillis)
            },
            onPlus = {
                updateProfile {
                    copy(
                        gestureConfig = gestureConfig.copy(
                            swipeDurationMillis = (gestureConfig.swipeDurationMillis + 100L)
                                .coerceAtMost(MAX_SWIPE_DURATION_MILLIS),
                        ),
                    )
                }
                actions.onSwipeDurationChanged(profile.gestureConfig.swipeDurationMillis)
            },
        ))

        addView(sectionLabel(context, context.getString(R.string.setting_overlay)))
        addView(overlayOrientationRow)
        addView(stepperRow(
            context = context,
            label = context.getString(R.string.setting_opacity),
            valueText = { "${(profile.overlayConfig.opacity * 100).toInt()}%" },
            onMinus = {
                val next = (profile.overlayConfig.opacity - 0.1f).coerceAtLeast(0.3f)
                updateProfile { copy(overlayConfig = overlayConfig.copy(opacity = next)) }
                actions.onOpacityChanged(profile.overlayConfig.opacity)
            },
            onPlus = {
                val next = (profile.overlayConfig.opacity + 0.1f).coerceAtMost(1.0f)
                updateProfile { copy(overlayConfig = overlayConfig.copy(opacity = next)) }
                actions.onOpacityChanged(profile.overlayConfig.opacity)
            },
        ))

        addView(conditionalRow)

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            addView(actionButton(context, context.getString(R.string.action_test)) { actions.onTest() })
            addView(actionButton(context, context.getString(R.string.action_save)) { actions.onSave() })
        })
    }

    init {
        rebuildModeRow()
        rebuildDirectionRow()
        rebuildOverlayOrientationRow()
        rebuildConditionalRow()
    }

    fun update(profile: AppProfile) {
        this.profile = profile
        rebuildModeRow()
        rebuildDirectionRow()
        rebuildOverlayOrientationRow()
        rebuildConditionalRow()
    }

    private fun updateProfile(update: AppProfile.() -> AppProfile) {
        profile = profile.update()
        rebuildConditionalRow()
    }

    private fun rebuildModeRow() {
        modeRow.removeAllViews()
        ScrollMode.entries.forEach { mode ->
            modeRow.addView(actionButton(modeRow.context, mode.label()) {
                updateProfile { copy(timingConfig = timingConfig.copy(mode = mode)) }
                actions.onModeChanged(mode)
                rebuildModeRow()
            }.apply {
                setTextColor(if (mode == profile.timingConfig.mode) Color.WHITE else Color.rgb(168, 172, 179))
            })
        }
    }

    private fun rebuildDirectionRow() {
        directionRow.removeAllViews()
        IntentDirection.entries.forEach { direction ->
            directionRow.addView(actionButton(directionRow.context, direction.label()) {
                updateProfile { copy(gestureConfig = gestureConfig.copy(intentDirection = direction)) }
                actions.onDirectionChanged(direction)
                rebuildDirectionRow()
            }.apply {
                setTextColor(if (direction == profile.gestureConfig.intentDirection) Color.WHITE else Color.rgb(168, 172, 179))
            })
        }
    }

    private fun rebuildOverlayOrientationRow() {
        overlayOrientationRow.removeAllViews()
        OverlayOrientation.entries.forEach { orientation ->
            overlayOrientationRow.addView(actionButton(overlayOrientationRow.context, orientation.label()) {
                updateProfile { copy(overlayConfig = overlayConfig.copy(orientation = orientation)) }
                actions.onOverlayOrientationChanged(orientation)
                rebuildOverlayOrientationRow()
            }.apply {
                setTextColor(
                    if (orientation == profile.overlayConfig.orientation) {
                        Color.WHITE
                    } else {
                        Color.rgb(168, 172, 179)
                    },
                )
            })
        }
    }

    private fun rebuildConditionalRow() {
        conditionalRow.removeAllViews()
        when (profile.timingConfig.mode) {
            ScrollMode.Repeat -> conditionalRow.addView(stepperRow(
                context = conditionalRow.context,
                label = conditionalRow.context.getString(R.string.label_repeat_count),
                valueText = { (profile.timingConfig.repeatCount ?: 1).toString() },
                onMinus = {
                    val next = ((profile.timingConfig.repeatCount ?: 1) - 1).coerceAtLeast(1)
                    updateProfile { copy(timingConfig = timingConfig.copy(repeatCount = next)) }
                    actions.onRepeatCountChanged(next)
                },
                onPlus = {
                    val next = ((profile.timingConfig.repeatCount ?: 1) + 1).coerceAtMost(999)
                    updateProfile { copy(timingConfig = timingConfig.copy(repeatCount = next)) }
                    actions.onRepeatCountChanged(next)
                },
            ))
            ScrollMode.Timer -> conditionalRow.addView(stepperRow(
                context = conditionalRow.context,
                label = conditionalRow.context.getString(R.string.label_duration),
                valueText = { "${(profile.timingConfig.durationMillis ?: 60_000L) / 1000}s" },
                onMinus = {
                    val next = ((profile.timingConfig.durationMillis ?: 60_000L) - 10_000L).coerceAtLeast(10_000L)
                    updateProfile { copy(timingConfig = timingConfig.copy(durationMillis = next)) }
                    actions.onDurationChanged(next)
                },
                onPlus = {
                    val next = ((profile.timingConfig.durationMillis ?: 60_000L) + 10_000L).coerceAtMost(3_600_000L)
                    updateProfile { copy(timingConfig = timingConfig.copy(durationMillis = next)) }
                    actions.onDurationChanged(next)
                },
            ))
            ScrollMode.Once,
            ScrollMode.UntilStop -> Unit
        }
    }

    private fun sectionLabel(context: Context, text: String): TextView =
        label(context, text, size = 12f, color = Color.rgb(168, 172, 179)).apply {
            setPadding(0, 14, 0, 4)
        }

    private fun label(context: Context, text: String, size: Float, color: Int): TextView =
        TextView(context).apply {
            this.text = text
            setTextColor(color)
            textSize = size
        }

    private fun stepperRow(
        context: Context,
        label: String,
        valueText: () -> String,
        onMinus: () -> Unit,
        onPlus: () -> Unit,
    ): LinearLayout {
        lateinit var value: TextView
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 0)
            addView(label(context, label, size = 13f, color = Color.WHITE))
            addView(actionButton(context, "-") {
                onMinus()
                value.text = valueText()
            })
            value = label(context, valueText(), size = 13f, color = Color.rgb(220, 224, 230))
            addView(value)
            addView(actionButton(context, "+") {
                onPlus()
                value.text = valueText()
            })
        }
    }

    private fun actionButton(context: Context, label: String, onClick: () -> Unit): Button =
        Button(context).apply {
            text = label
            maxLines = 2
            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0
            setPadding(14, 4, 14, 4)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { onClick() }
        }

    private fun ScrollMode.label(): String = when (this) {
        ScrollMode.Once -> modeRow.context.getString(R.string.mode_once)
        ScrollMode.Repeat -> modeRow.context.getString(R.string.mode_repeat)
        ScrollMode.UntilStop -> modeRow.context.getString(R.string.mode_until_stop)
        ScrollMode.Timer -> modeRow.context.getString(R.string.mode_timer)
    }

    private fun IntentDirection.label(): String = when (this) {
        IntentDirection.NextItem -> directionRow.context.getString(R.string.overlay_next)
        IntentDirection.PreviousItem -> directionRow.context.getString(R.string.overlay_previous)
    }

    private fun OverlayOrientation.label(): String = when (this) {
        OverlayOrientation.Vertical -> overlayOrientationRow.context.getString(R.string.setting_vertical)
        OverlayOrientation.Horizontal -> overlayOrientationRow.context.getString(R.string.setting_horizontal)
    }

    private fun Long.asSecondsText(): String =
        "${this / 1000}.${(this % 1000) / 100}s"
}

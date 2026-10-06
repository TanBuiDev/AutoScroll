package com.personal.autoscroll.core.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
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
    val onPreview: () -> Unit,
    val onSave: () -> Unit,
)

class ExpandedOverlay(
    context: Context,
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private var profile: AppProfile,
    private val actions: ExpandedOverlayActions,
    private val onPositionChanged: (Int, Int) -> Unit = { _, _ -> },
) {
    private val modeRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    private val directionRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    private val overlayOrientationRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    private val conditionalRow = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }

    val view: View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.START
        elevation = dp(18).toFloat()
        setPadding(dp(18), dp(18), dp(18), dp(16))
        background = roundedBackground(
            fill = Color.argb(238, 248, 250, 252),
            stroke = Color.argb(170, 215, 222, 232),
            radius = dp(26).toFloat(),
        )

        addView(label(context, context.getString(R.string.overlay_config), size = 18f, color = Palette.TextPrimary).apply {
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            contentDescription = context.getString(R.string.overlay_drag_handle_description)
            setOnTouchListener(createDragTouchListener())
        })
        val displayName = if (profile.packageName == "manual.overlay") {
            context.getString(R.string.manual_overlay_name)
        } else {
            profile.appName.takeIf { it.isNotBlank() && it != profile.packageName }
                ?: context.getString(R.string.app_name_unavailable)
        }
        addView(label(context, displayName, size = 12f, color = Palette.TextMuted).apply {
            setPadding(0, dp(2), 0, dp(10))
        })

        addSection(
            context = context,
            title = context.getString(R.string.setting_mode),
            accent = Palette.Blue,
            content = modeRow,
        )
        addSection(
            context = context,
            title = context.getString(R.string.setting_direction),
            accent = Palette.Indigo,
            content = directionRow,
        )
        addSection(
            context = context,
            title = context.getString(R.string.label_timing),
            accent = Palette.Amber,
            content = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(stepperRow(
                    context = context,
                    label = context.getString(R.string.setting_delay),
                    valueText = { profile.timingConfig.delayMillis.asSecondsText() },
                    accent = Palette.Amber,
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
                    accent = Palette.Amber,
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
                addView(conditionalRow)
            },
        )
        addSection(
            context = context,
            title = context.getString(R.string.setting_overlay),
            accent = Palette.Teal,
            content = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(overlayOrientationRow)
                addView(stepperRow(
                    context = context,
                    label = context.getString(R.string.setting_opacity),
                    valueText = { "${(profile.overlayConfig.opacity * 100).toInt()}%" },
                    accent = Palette.Teal,
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
            },
        )

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(12), 0, 0)
            addView(commandButton(context, context.getString(R.string.action_preview), Palette.Indigo, filled = false) {
                actions.onPreview()
            })
            addView(commandButton(context, context.getString(R.string.action_save), Palette.Blue, filled = true) {
                actions.onSave()
            })
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

    private fun LinearLayout.addSection(
        context: Context,
        title: String,
        accent: Int,
        content: View,
    ) {
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = roundedBackground(
                fill = Color.argb(88, 255, 255, 255),
                stroke = accent.withAlpha(48),
                radius = dp(18).toFloat(),
            )
            addView(label(context, title, size = 12f, color = accent).apply {
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(0, 0, 0, dp(8))
            })
            addView(content)
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = dp(8)
        })
    }

    private fun rebuildModeRow() {
        modeRow.removeAllViews()
        ScrollMode.entries.forEach { mode ->
            modeRow.addView(choiceButton(
                context = modeRow.context,
                label = mode.label(),
                accent = Palette.Blue,
                selected = mode == profile.timingConfig.mode,
            ) {
                updateProfile { copy(timingConfig = timingConfig.copy(mode = mode)) }
                actions.onModeChanged(mode)
                rebuildModeRow()
            })
        }
    }

    private fun rebuildDirectionRow() {
        directionRow.removeAllViews()
        IntentDirection.entries.forEach { direction ->
            directionRow.addView(choiceButton(
                context = directionRow.context,
                label = direction.label(),
                accent = Palette.Indigo,
                selected = direction == profile.gestureConfig.intentDirection,
            ) {
                updateProfile { copy(gestureConfig = gestureConfig.copy(intentDirection = direction)) }
                actions.onDirectionChanged(direction)
                rebuildDirectionRow()
            })
        }
    }

    private fun rebuildOverlayOrientationRow() {
        overlayOrientationRow.removeAllViews()
        OverlayOrientation.entries.forEach { orientation ->
            overlayOrientationRow.addView(choiceButton(
                context = overlayOrientationRow.context,
                label = orientation.label(),
                accent = Palette.Teal,
                selected = orientation == profile.overlayConfig.orientation,
            ) {
                updateProfile { copy(overlayConfig = overlayConfig.copy(orientation = orientation)) }
                actions.onOverlayOrientationChanged(orientation)
                rebuildOverlayOrientationRow()
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
                accent = Palette.Amber,
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
                valueText = { (profile.timingConfig.durationMillis ?: 60_000L).asSecondsText() },
                accent = Palette.Amber,
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

    private fun createDragTouchListener(): View.OnTouchListener {
        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        return View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - touchX
                    val deltaY = event.rawY - touchY
                    if (kotlin.math.abs(deltaX) > view.dp(4) || kotlin.math.abs(deltaY) > view.dp(4)) {
                        moved = true
                        val metrics = view.resources.displayMetrics
                        val maxX = (metrics.widthPixels - view.width).coerceAtLeast(0)
                        val maxY = (metrics.heightPixels - view.height).coerceAtLeast(0)
                        layoutParams.x = (initialX + deltaX.toInt()).coerceIn(0, maxX)
                        layoutParams.y = (initialY + deltaY.toInt()).coerceIn(0, maxY)
                        windowManager.updateViewLayout(view, layoutParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (moved) {
                        onPositionChanged(layoutParams.x, layoutParams.y)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun label(context: Context, text: String, size: Float, color: Int): TextView =
        TextView(context).apply {
            this.text = text
            setTextColor(color)
            textSize = size
            includeFontPadding = true
        }

    private fun stepperRow(
        context: Context,
        label: String,
        valueText: () -> String,
        accent: Int,
        onMinus: () -> Unit,
        onPlus: () -> Unit,
    ): LinearLayout {
        lateinit var value: TextView
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(5), 0, dp(5))
            addView(label(context, label, size = 13f, color = Palette.TextPrimary), LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f,
            ))
            addView(iconTextButton(context, "-", accent) {
                onMinus()
                value.text = valueText()
            })
            value = label(context, valueText(), size = 14f, color = Palette.TextPrimary).apply {
                gravity = Gravity.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            addView(value, LinearLayout.LayoutParams(dp(72), LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(iconTextButton(context, "+", accent) {
                onPlus()
                value.text = valueText()
            })
        }
    }

    private fun choiceButton(
        context: Context,
        label: String,
        accent: Int,
        selected: Boolean,
        onClick: () -> Unit,
    ): TextView = TextView(context).apply {
        text = label
        maxLines = 1
        gravity = Gravity.CENTER
        textSize = 12f
        setTextColor(if (selected) Color.WHITE else accent)
        setPadding(dp(10), dp(7), dp(10), dp(7))
        background = roundedBackground(
            fill = if (selected) accent else accent.withAlpha(18),
            stroke = accent.withAlpha(if (selected) 0 else 70),
            radius = dp(999).toFloat(),
        )
        isClickable = true
        isFocusable = true
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginEnd = dp(6)
        }
    }

    private fun iconTextButton(context: Context, label: String, accent: Int, onClick: () -> Unit): TextView =
        TextView(context).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = 17f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(accent)
            background = roundedBackground(
                fill = accent.withAlpha(20),
                stroke = accent.withAlpha(54),
                radius = dp(999).toFloat(),
            )
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(dp(32), dp(32)).apply {
                marginStart = dp(4)
                marginEnd = dp(4)
            }
        }

    private fun commandButton(
        context: Context,
        label: String,
        accent: Int,
        filled: Boolean,
        onClick: () -> Unit,
    ): TextView = TextView(context).apply {
        text = label
        gravity = Gravity.CENTER
        textSize = 14f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        setTextColor(if (filled) Color.WHITE else accent)
        setPadding(dp(16), dp(9), dp(16), dp(9))
        background = roundedBackground(
            fill = if (filled) accent else accent.withAlpha(18),
            stroke = accent.withAlpha(if (filled) 0 else 72),
            radius = dp(999).toFloat(),
        )
        isClickable = true
        isFocusable = true
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            marginStart = dp(8)
        }
    }

    private fun roundedBackground(fill: Int, stroke: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fill)
            setStroke(1, stroke)
            cornerRadius = radius
        }

    private fun Int.withAlpha(alpha: Int): Int =
        Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))

    private fun Context.dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun View.dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

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

    private object Palette {
        val TextPrimary: Int = Color.rgb(17, 24, 39)
        val TextMuted: Int = Color.rgb(107, 114, 128)
        val Blue: Int = Color.rgb(37, 99, 235)
        val Indigo: Int = Color.rgb(79, 70, 229)
        val Teal: Int = Color.rgb(15, 118, 110)
        val Amber: Int = Color.rgb(217, 119, 6)
    }
}

package com.personal.autoscroll.core.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.OverlayConfig
import com.personal.autoscroll.domain.model.OverlayOrientation
import com.personal.autoscroll.domain.model.OverlaySize

class CompactOverlay(
    context: Context,
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private var overlayConfig: OverlayConfig,
    private val actions: OverlayActions,
) {
    private val rootContainer = LinearLayout(context)
    private val startStopButton = actionButton(
        context = context,
        iconType = OverlayIconType.Play,
        isPrimary = true,
    )
    private val handleView = DragHandleView(context)
    private val buttonsContainer = LinearLayout(context)
    private var isCollapsed = false
    private var isRunning = false

    val view: View = rootContainer.apply {
        gravity = Gravity.CENTER
        clipToOutline = false
        elevation = context.dp(14).toFloat()

        addOverlayItem(handleView.apply {
            contentDescription = context.getString(R.string.overlay_drag_handle_description)
            setOnTouchListener(createHandleTouchListener())
        })

        buttonsContainer.apply {
            gravity = Gravity.CENTER
            addOverlayItem(startStopButton.apply {
                setOnClickListener { actions.onStartStop() }
            })
            addOverlayItem(actionButton(context, OverlayIconType.Next).apply {
                setOnClickListener { actions.onNext() }
            })
            addOverlayItem(actionButton(context, OverlayIconType.Previous).apply {
                setOnClickListener { actions.onPrevious() }
            })
            addOverlayItem(actionButton(context, OverlayIconType.Settings).apply {
                setOnClickListener { actions.onSettings() }
            })
        }
        addOverlayItem(buttonsContainer)
        applyConfig()
    }

    fun setRunning(isRunning: Boolean) {
        this.isRunning = isRunning
        startStopButton.setIconType(if (isRunning) OverlayIconType.Stop else OverlayIconType.Play)
        startStopButton.background = if (isRunning) {
            actionButtonBackground(startStopButton.context, isPrimary = true, isDanger = true)
        } else {
            actionButtonBackground(startStopButton.context, isPrimary = true)
        }
    }

    fun updateConfig(config: OverlayConfig) {
        overlayConfig = config
        applyConfig()
    }

    private fun applyConfig() {
        val vertical = overlayConfig.orientation == OverlayOrientation.Vertical
        val densitySize = overlayConfig.size.metrics()
        rootContainer.orientation = if (vertical) {
            LinearLayout.VERTICAL
        } else {
            LinearLayout.HORIZONTAL
        }
        buttonsContainer.orientation = rootContainer.orientation
        buttonsContainer.visibility = if (isCollapsed) View.GONE else View.VISIBLE
        rootContainer.setPadding(
            rootContainer.context.dp(densitySize.panelPaddingHorizontal),
            rootContainer.context.dp(densitySize.panelPaddingVertical),
            rootContainer.context.dp(densitySize.panelPaddingHorizontal),
            rootContainer.context.dp(densitySize.panelPaddingVertical),
        )
        rootContainer.background = roundedBackground(
            fill = Color.argb((184 * overlayConfig.opacity).toInt(), 245, 247, 251),
            stroke = Color.argb((128 * overlayConfig.opacity).toInt(), 216, 222, 232),
            radius = rootContainer.context.dp(28).toFloat(),
        )
        rootContainer.alpha = 1f
        updateButtonSizes(densitySize)
        setRunning(isRunning)
    }

    private fun toggleCollapsed() {
        isCollapsed = !isCollapsed
        applyConfig()
    }

    private fun createHandleTouchListener(): View.OnTouchListener {
        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        return View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = this@CompactOverlay.layoutParams.x
                    initialY = this@CompactOverlay.layoutParams.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - touchX
                    val deltaY = event.rawY - touchY
                    if (kotlin.math.abs(deltaX) > view.context.dp(4) || kotlin.math.abs(deltaY) > view.context.dp(4)) {
                        moved = true
                        this@CompactOverlay.layoutParams.x = initialX + deltaX.toInt()
                        this@CompactOverlay.layoutParams.y = initialY + deltaY.toInt()
                        windowManager.updateViewLayout(view, this@CompactOverlay.layoutParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) toggleCollapsed()
                    true
                }
                else -> false
            }
        }
    }

    private fun actionButton(
        context: Context,
        iconType: OverlayIconType,
        isPrimary: Boolean = false,
    ): OverlayActionButton = OverlayActionButton(context, iconType).apply {
        isClickable = true
        isFocusable = true
        background = actionButtonBackground(context, isPrimary)
    }

    private fun actionButtonBackground(
        context: Context,
        isPrimary: Boolean,
        isDanger: Boolean = false,
    ): GradientDrawable =
        roundedBackground(
            fill = when {
                isDanger -> Color.rgb(218, 64, 72)
                isPrimary -> Color.argb(150, 233, 241, 255)
                else -> Color.argb(150, 255, 255, 255)
            },
            stroke = when {
                isDanger -> Color.rgb(247, 139, 145)
                isPrimary -> Color.argb(170, 197, 215, 255)
                else -> Color.argb(150, 227, 232, 240)
            },
            radius = context.dp(999).toFloat(),
        )

    private fun roundedBackground(fill: Int, stroke: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fill)
            setStroke(1, stroke)
            cornerRadius = radius
        }

    private fun Context.dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun updateButtonSizes(metrics: OverlayMetrics) {
        for (index in 0 until buttonsContainer.childCount) {
            buttonsContainer.getChildAt(index).layoutParams = LinearLayout.LayoutParams(
                rootContainer.context.dp(metrics.buttonSize),
                rootContainer.context.dp(metrics.buttonSize),
            ).apply {
                if (rootContainer.orientation == LinearLayout.VERTICAL) {
                    bottomMargin = rootContainer.context.dp(metrics.buttonSpacing)
                } else {
                    marginEnd = rootContainer.context.dp(metrics.buttonSpacing)
                }
            }
        }
    }

    private fun LinearLayout.addOverlayItem(view: View) {
        addView(
            view,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                marginEnd = context.dp(6)
                bottomMargin = context.dp(8)
            },
        )
    }

    private fun OverlaySize.metrics(): OverlayMetrics = when (this) {
        OverlaySize.Small -> OverlayMetrics(buttonSize = 44, buttonSpacing = 10, panelPaddingHorizontal = 8, panelPaddingVertical = 10)
        OverlaySize.Medium -> OverlayMetrics(buttonSize = 52, buttonSpacing = 12, panelPaddingHorizontal = 10, panelPaddingVertical = 12)
        OverlaySize.Large -> OverlayMetrics(buttonSize = 62, buttonSpacing = 14, panelPaddingHorizontal = 12, panelPaddingVertical = 14)
    }

    private data class OverlayMetrics(
        val buttonSize: Int,
        val buttonSpacing: Int,
        val panelPaddingHorizontal: Int,
        val panelPaddingVertical: Int,
    )

    private enum class OverlayIconType {
        Play,
        Stop,
        Next,
        Previous,
        Settings,
    }

    private class OverlayActionButton(
        context: Context,
        iconType: OverlayIconType,
    ) : LinearLayout(context) {
        private val iconView = OverlayIconView(context, iconType)

        init {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(6), dp(6), dp(6), dp(6))
            addView(iconView, LayoutParams(dp(30), dp(30)))
        }

        fun setIconType(iconType: OverlayIconType) {
            iconView.iconType = iconType
        }

        private fun dp(value: Int): Int =
            (value * resources.displayMetrics.density).toInt()
    }

    private class OverlayIconView(context: Context, iconType: OverlayIconType) : View(context) {
        var iconType: OverlayIconType = iconType
            set(value) {
                field = value
                invalidate()
            }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 23, 31)
            strokeWidth = dp(3).toFloat()
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 23, 31)
            style = Paint.Style.FILL
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            when (iconType) {
                OverlayIconType.Play -> drawPlay(canvas)
                OverlayIconType.Stop -> drawStop(canvas)
                OverlayIconType.Next -> drawArrow(canvas, pointsUp = true)
                OverlayIconType.Previous -> drawArrow(canvas, pointsUp = false)
                OverlayIconType.Settings -> drawSettings(canvas)
            }
        }

        private fun drawPlay(canvas: Canvas) {
            val path = Path().apply {
                moveTo(width * 0.32f, height * 0.22f)
                lineTo(width * 0.32f, height * 0.78f)
                lineTo(width * 0.78f, height * 0.50f)
                close()
            }
            canvas.drawPath(path, paint)
        }

        private fun drawStop(canvas: Canvas) {
            canvas.drawRoundRect(
                RectF(width * 0.30f, height * 0.28f, width * 0.70f, height * 0.72f),
                dp(3).toFloat(),
                dp(3).toFloat(),
                fillPaint,
            )
        }

        private fun drawArrow(canvas: Canvas, pointsUp: Boolean) {
            val startY = if (pointsUp) height * 0.76f else height * 0.24f
            val endY = if (pointsUp) height * 0.24f else height * 0.76f
            val headY = endY
            val headBackY = if (pointsUp) height * 0.46f else height * 0.54f
            canvas.drawLine(width * 0.50f, startY, width * 0.50f, endY, paint)
            canvas.drawLine(width * 0.28f, headBackY, width * 0.50f, headY, paint)
            canvas.drawLine(width * 0.72f, headBackY, width * 0.50f, headY, paint)
        }

        private fun drawSettings(canvas: Canvas) {
            canvas.drawCircle(width * 0.40f, height * 0.58f, dp(7).toFloat(), fillPaint)
            canvas.drawCircle(width * 0.40f, height * 0.58f, dp(3).toFloat(), Paint(fillPaint).apply {
                color = Color.WHITE
            })
            canvas.drawCircle(width * 0.68f, height * 0.32f, dp(4).toFloat(), paint)
            canvas.drawCircle(width * 0.72f, height * 0.66f, dp(4).toFloat(), paint)
            canvas.drawLine(width * 0.18f, height * 0.58f, width * 0.62f, height * 0.58f, paint)
            canvas.drawLine(width * 0.40f, height * 0.34f, width * 0.40f, height * 0.82f, paint)
        }

        private fun dp(value: Int): Int =
            (value * resources.displayMetrics.density).toInt()
    }

    private class DragHandleView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(160, 166, 176)
        }

        init {
            minimumWidth = dp(42)
            minimumHeight = dp(22)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(dp(42), dp(22))
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val radius = dp(2.0f)
            val centerX = width / 2f
            val centerY = height / 2f
            val gapX = dp(7f)
            val gapY = dp(6f)
            for (column in -1..1) {
                for (row in -1..1 step 2) {
                    canvas.drawCircle(centerX + column * gapX, centerY + row * gapY / 2f, radius, paint)
                }
            }
        }

        private fun dp(value: Int): Int =
            (value * resources.displayMetrics.density).toInt()

        private fun dp(value: Float): Float =
            value * resources.displayMetrics.density
    }
}

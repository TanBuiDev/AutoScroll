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
import com.personal.autoscroll.domain.model.GestureAxis
import com.personal.autoscroll.domain.model.OverlayConfig
import com.personal.autoscroll.domain.model.OverlayOrientation
import com.personal.autoscroll.domain.model.OverlaySize

class CompactOverlay(
    context: Context,
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private var overlayConfig: OverlayConfig,
    private var gestureAxis: GestureAxis,
    private val actions: OverlayActions,
    private val onPositionChanged: (Int, Int) -> Unit = { _, _ -> },
) {
    private val rootContainer = LinearLayout(context)
    private val startStopButton = actionButton(
        context = context,
        iconType = OverlayIconType.Play,
    )
    private val handleView = DragHandleView(context)
    private val buttonsContainer = LinearLayout(context)
    private val nextButton = actionButton(context, OverlayIconType.Next)
    private val previousButton = actionButton(context, OverlayIconType.Previous)
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
            addOverlayItem(nextButton.apply {
                setOnClickListener { actions.onNext() }
            })
            addOverlayItem(previousButton.apply {
                setOnClickListener { actions.onPrevious() }
            })
            addOverlayItem(actionButton(context, OverlayIconType.Settings).apply {
                setOnClickListener { actions.onSettings() }
            })
            addOverlayItem(actionButton(context, OverlayIconType.Close).apply {
                setOnClickListener { actions.onClose() }
            })
        }
        addOverlayItem(buttonsContainer)
        applyConfig()
    }

    fun setRunning(isRunning: Boolean) {
        this.isRunning = isRunning
        startStopButton.setIconType(if (isRunning) OverlayIconType.Stop else OverlayIconType.Play)
        startStopButton.background = if (isRunning) {
            actionButtonBackground(startStopButton.context, OverlayIconType.Stop)
        } else {
            actionButtonBackground(startStopButton.context, OverlayIconType.Play)
        }
    }

    fun updateConfig(config: OverlayConfig) {
        overlayConfig = config
        applyConfig()
    }

    fun updateGestureAxis(axis: GestureAxis) {
        gestureAxis = axis
        nextButton.setGestureAxis(axis)
        previousButton.setGestureAxis(axis)
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
        nextButton.visibility = if (overlayConfig.showNextPrevious) View.VISIBLE else View.GONE
        previousButton.visibility = if (overlayConfig.showNextPrevious) View.VISIBLE else View.GONE
        handleView.setCollapsed(isCollapsed)
        rootContainer.setPadding(
            rootContainer.context.dp(if (isCollapsed) 4 else densitySize.panelPaddingHorizontal),
            rootContainer.context.dp(if (isCollapsed) 4 else densitySize.panelPaddingVertical),
            rootContainer.context.dp(if (isCollapsed) 4 else densitySize.panelPaddingHorizontal),
            rootContainer.context.dp(if (isCollapsed) 4 else densitySize.panelPaddingVertical),
        )
        rootContainer.background = roundedBackground(
            fill = Color.argb(((if (isCollapsed) 218 else 184) * overlayConfig.opacity).toInt(), 245, 247, 251),
            stroke = Color.argb(((if (isCollapsed) 148 else 128) * overlayConfig.opacity).toInt(), 216, 222, 232),
            radius = rootContainer.context.dp(if (isCollapsed) 20 else 28).toFloat(),
        )
        rootContainer.alpha = 1f
        updateGestureAxis(gestureAxis)
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
                        val metrics = view.resources.displayMetrics
                        val maxX = (metrics.widthPixels - view.width).coerceAtLeast(0)
                        val maxY = (metrics.heightPixels - view.height).coerceAtLeast(0)
                        this@CompactOverlay.layoutParams.x =
                            (initialX + deltaX.toInt()).coerceIn(0, maxX)
                        this@CompactOverlay.layoutParams.y =
                            (initialY + deltaY.toInt()).coerceIn(0, maxY)
                        windowManager.updateViewLayout(view, this@CompactOverlay.layoutParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        toggleCollapsed()
                    } else {
                        onPositionChanged(
                            this@CompactOverlay.layoutParams.x,
                            this@CompactOverlay.layoutParams.y,
                        )
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun actionButton(
        context: Context,
        iconType: OverlayIconType,
    ): OverlayActionButton = OverlayActionButton(context, iconType).apply {
        isClickable = true
        isFocusable = true
        background = actionButtonBackground(context, iconType)
    }

    private fun actionButtonBackground(
        context: Context,
        iconType: OverlayIconType,
    ): GradientDrawable =
        roundedBackground(
            fill = iconType.tint().softFill(),
            stroke = iconType.tint().softStroke(),
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
            val size = if (index == 0) metrics.primaryButtonSize else metrics.buttonSize
            buttonsContainer.getChildAt(index).layoutParams = LinearLayout.LayoutParams(
                rootContainer.context.dp(size),
                rootContainer.context.dp(size),
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
                marginEnd = 0
                bottomMargin = 0
            },
        )
    }

    private fun OverlaySize.metrics(): OverlayMetrics = when (this) {
        OverlaySize.Small -> OverlayMetrics(primaryButtonSize = 44, buttonSize = 36, buttonSpacing = 14, panelPaddingHorizontal = 10, panelPaddingVertical = 10)
        OverlaySize.Medium -> OverlayMetrics(primaryButtonSize = 48, buttonSize = 40, buttonSpacing = 16, panelPaddingHorizontal = 12, panelPaddingVertical = 12)
        OverlaySize.Large -> OverlayMetrics(primaryButtonSize = 56, buttonSize = 48, buttonSpacing = 18, panelPaddingHorizontal = 14, panelPaddingVertical = 14)
    }

    private data class OverlayMetrics(
        val primaryButtonSize: Int,
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
        Close;

        fun tint(): Int = when (this) {
            Play -> Color.rgb(37, 99, 235)
            Stop -> Color.rgb(220, 38, 38)
            Next -> Color.rgb(8, 145, 178)
            Previous -> Color.rgb(79, 70, 229)
            Settings -> Color.rgb(51, 65, 85)
            Close -> Color.rgb(220, 38, 38)
        }
    }

    private fun Int.softFill(): Int =
        Color.argb(24, Color.red(this), Color.green(this), Color.blue(this))

    private fun Int.softStroke(): Int =
        Color.argb(92, Color.red(this), Color.green(this), Color.blue(this))

    private class OverlayActionButton(
        context: Context,
        iconType: OverlayIconType,
    ) : LinearLayout(context) {
        private val iconView = OverlayIconView(context, iconType)

        init {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            addView(iconView, LayoutParams(dp(24), dp(24)))
        }

        fun setIconType(iconType: OverlayIconType) {
            iconView.iconType = iconType
        }

        fun setGestureAxis(axis: GestureAxis) {
            iconView.gestureAxis = axis
        }

        private fun dp(value: Int): Int =
            (value * resources.displayMetrics.density).toInt()
    }

    private class OverlayIconView(context: Context, iconType: OverlayIconType) : View(context) {
        var iconType: OverlayIconType = iconType
            set(value) {
                field = value
                applyTint()
                invalidate()
            }
        var gestureAxis: GestureAxis = GestureAxis.Vertical
            set(value) {
                field = value
                invalidate()
            }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = dp(3).toFloat()
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        init {
            applyTint()
        }

        private fun applyTint() {
            paint.color = iconType.tint()
            fillPaint.color = iconType.tint()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            when (iconType) {
                OverlayIconType.Play -> drawPlay(canvas)
                OverlayIconType.Stop -> drawStop(canvas)
                OverlayIconType.Next -> drawDirectionalArrow(canvas, isNext = true)
                OverlayIconType.Previous -> drawDirectionalArrow(canvas, isNext = false)
                OverlayIconType.Settings -> drawSliders(canvas)
                OverlayIconType.Close -> drawClose(canvas)
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

        private fun drawDirectionalArrow(canvas: Canvas, isNext: Boolean) {
            if (gestureAxis == GestureAxis.Horizontal) {
                drawHorizontalArrow(canvas, pointsLeft = isNext)
            } else {
                drawVerticalArrow(canvas, pointsUp = isNext)
            }
        }

        private fun drawVerticalArrow(canvas: Canvas, pointsUp: Boolean) {
            val startY = if (pointsUp) height * 0.76f else height * 0.24f
            val endY = if (pointsUp) height * 0.24f else height * 0.76f
            val headY = endY
            val headBackY = if (pointsUp) height * 0.46f else height * 0.54f
            canvas.drawLine(width * 0.50f, startY, width * 0.50f, endY, paint)
            canvas.drawLine(width * 0.28f, headBackY, width * 0.50f, headY, paint)
            canvas.drawLine(width * 0.72f, headBackY, width * 0.50f, headY, paint)
        }

        private fun drawHorizontalArrow(canvas: Canvas, pointsLeft: Boolean) {
            val startX = if (pointsLeft) width * 0.76f else width * 0.24f
            val endX = if (pointsLeft) width * 0.24f else width * 0.76f
            val headX = endX
            val headBackX = if (pointsLeft) width * 0.46f else width * 0.54f
            canvas.drawLine(startX, height * 0.50f, endX, height * 0.50f, paint)
            canvas.drawLine(headBackX, height * 0.28f, headX, height * 0.50f, paint)
            canvas.drawLine(headBackX, height * 0.72f, headX, height * 0.50f, paint)
        }

        private fun drawSliders(canvas: Canvas) {
            val left = width * 0.22f
            val right = width * 0.78f
            val y1 = height * 0.30f
            val y2 = height * 0.50f
            val y3 = height * 0.70f
            canvas.drawLine(left, y1, right, y1, paint)
            canvas.drawLine(left, y2, right, y2, paint)
            canvas.drawLine(left, y3, right, y3, paint)
            canvas.drawCircle(width * 0.38f, y1, dp(3).toFloat(), fillPaint)
            canvas.drawCircle(width * 0.64f, y2, dp(3).toFloat(), fillPaint)
            canvas.drawCircle(width * 0.48f, y3, dp(3).toFloat(), fillPaint)
        }

        private fun drawClose(canvas: Canvas) {
            canvas.drawLine(width * 0.30f, height * 0.30f, width * 0.70f, height * 0.70f, paint)
            canvas.drawLine(width * 0.70f, height * 0.30f, width * 0.30f, height * 0.70f, paint)
        }

        private fun dp(value: Int): Int =
            (value * resources.displayMetrics.density).toInt()
    }

    private class DragHandleView(context: Context) : View(context) {
        private var isCollapsed = false
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(160, 166, 176)
        }
        private val chevronPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 23, 31)
            strokeWidth = dp(2.5f)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        init {
            minimumWidth = dp(24)
            minimumHeight = dp(24)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            if (isCollapsed) {
                setMeasuredDimension(dp(32), dp(32))
            } else {
                setMeasuredDimension(dp(24), dp(24))
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            drawChevron(canvas, pointsDown = isCollapsed)
        }

        fun setCollapsed(collapsed: Boolean) {
            if (isCollapsed == collapsed) return
            isCollapsed = collapsed
            animate()
                .rotation(if (collapsed) 180f else 0f)
                .setDuration(140L)
                .withEndAction {
                    rotation = 0f
                    requestLayout()
                    invalidate()
                }
                .start()
        }

        private fun drawChevron(canvas: Canvas, pointsDown: Boolean) {
            val centerX = width / 2f
            val centerY = height / 2f
            val halfWidth = dp(if (isCollapsed) 6f else 4f)
            val halfHeight = dp(if (isCollapsed) 4f else 3f)
            if (pointsDown) {
                canvas.drawLine(centerX - halfWidth, centerY - halfHeight / 2f, centerX, centerY + halfHeight, chevronPaint)
                canvas.drawLine(centerX + halfWidth, centerY - halfHeight / 2f, centerX, centerY + halfHeight, chevronPaint)
            } else {
                canvas.drawLine(centerX - halfWidth, centerY + halfHeight / 2f, centerX, centerY - halfHeight, chevronPaint)
                canvas.drawLine(centerX + halfWidth, centerY + halfHeight / 2f, centerX, centerY - halfHeight, chevronPaint)
            }
        }

        private fun dp(value: Int): Int =
            (value * resources.displayMetrics.density).toInt()

        private fun dp(value: Float): Float =
            value * resources.displayMetrics.density
    }
}

package com.personal.autoscroll.core.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.OverlayConfig
import com.personal.autoscroll.domain.model.OverlayOrientation

class CompactOverlay(
    context: Context,
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private var overlayConfig: OverlayConfig,
    private val actions: OverlayActions,
) {
    private val startStopButton = overlayButton(context, context.getString(R.string.overlay_start), isPrimary = true)
    private val handleView = DragHandleView(context)
    private val buttonsContainer = LinearLayout(context)
    private var isCollapsed = false

    val view: View = LinearLayout(context).apply {
        val initialOrientation = if (overlayConfig.orientation == OverlayOrientation.Vertical) {
            LinearLayout.VERTICAL
        } else {
            LinearLayout.HORIZONTAL
        }
        orientation = initialOrientation
        gravity = Gravity.CENTER
        setPadding(context.dp(8), context.dp(6), context.dp(8), context.dp(6))
        background = roundedBackground(
            fill = Color.argb((238 * overlayConfig.opacity).toInt(), 16, 18, 23),
            stroke = Color.argb(150, 91, 98, 112),
            radius = context.dp(18).toFloat(),
        )
        elevation = context.dp(8).toFloat()

        addOverlayItem(handleView.apply {
            contentDescription = context.getString(R.string.overlay_drag_handle_description)
            setOnTouchListener(createHandleTouchListener())
        })

        buttonsContainer.apply {
            orientation = initialOrientation
            gravity = Gravity.CENTER
            addOverlayItem(startStopButton.apply {
                setOnClickListener { actions.onStartStop() }
            })
            addOverlayItem(overlayButton(context, context.getString(R.string.overlay_next)).apply {
                setOnClickListener { actions.onNext() }
            })
            addOverlayItem(overlayButton(context, context.getString(R.string.overlay_previous)).apply {
                setOnClickListener { actions.onPrevious() }
            })
            addOverlayItem(overlayButton(context, context.getString(R.string.overlay_config)).apply {
                setOnClickListener { actions.onSettings() }
            })
        }
        addOverlayItem(buttonsContainer)
        alpha = overlayConfig.opacity.coerceIn(0.3f, 1f)
    }

    fun setRunning(isRunning: Boolean) {
        startStopButton.text = if (isRunning) {
            startStopButton.context.getString(R.string.overlay_stop)
        } else {
            startStopButton.context.getString(R.string.overlay_start)
        }
        startStopButton.setTextColor(Color.WHITE)
        startStopButton.background = if (isRunning) {
            roundedBackground(
                fill = Color.rgb(180, 43, 43),
                stroke = Color.rgb(255, 118, 118),
                radius = startStopButton.context.dp(14).toFloat(),
            )
        } else {
            overlayButtonBackground(startStopButton.context, isPrimary = true)
        }
    }

    fun updateConfig(config: OverlayConfig) {
        overlayConfig = config
        applyConfig()
    }

    private fun applyConfig() {
        val root = view as LinearLayout
        root.orientation = if (overlayConfig.orientation == OverlayOrientation.Vertical) {
            LinearLayout.VERTICAL
        } else {
            LinearLayout.HORIZONTAL
        }
        buttonsContainer.orientation = root.orientation
        buttonsContainer.visibility = if (isCollapsed) View.GONE else View.VISIBLE
        view.alpha = overlayConfig.opacity.coerceIn(0.3f, 1f)
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

    private fun overlayButton(context: Context, label: String, isPrimary: Boolean = false): TextView = TextView(context).apply {
        text = label
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
        minWidth = context.dp(if (isPrimary) 58 else 54)
        minHeight = context.dp(34)
        includeFontPadding = false
        textSize = 14f
        maxLines = 2
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        setPadding(context.dp(10), 0, context.dp(10), 0)
        setTextColor(Color.WHITE)
        background = overlayButtonBackground(context, isPrimary)
    }

    private fun overlayButtonBackground(context: Context, isPrimary: Boolean): GradientDrawable =
        roundedBackground(
            fill = if (isPrimary) Color.rgb(46, 104, 255) else Color.argb(70, 255, 255, 255),
            stroke = if (isPrimary) Color.rgb(128, 166, 255) else Color.argb(90, 255, 255, 255),
            radius = context.dp(14).toFloat(),
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

    private fun LinearLayout.addOverlayItem(view: View) {
        addView(
            view,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                marginEnd = context.dp(6)
                bottomMargin = context.dp(6)
            },
        )
    }

    private class DragHandleView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(210, 214, 222)
        }

        init {
            minimumWidth = dp(34)
            minimumHeight = dp(34)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(dp(34), dp(34))
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val radius = dp(2).toFloat()
            val centerX = width / 2f
            val centerY = height / 2f
            val gap = dp(7).toFloat()
            for (column in -1..1 step 2) {
                for (row in -1..1) {
                    canvas.drawCircle(centerX + column * gap / 2f, centerY + row * gap, radius, paint)
                }
            }
        }

        private fun dp(value: Int): Int =
            (value * resources.displayMetrics.density).toInt()
    }
}

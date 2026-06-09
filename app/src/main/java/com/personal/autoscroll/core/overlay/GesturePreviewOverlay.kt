package com.personal.autoscroll.core.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.Gravity
import android.view.View
import com.personal.autoscroll.R
import com.personal.autoscroll.core.gesture.GesturePlan
import com.personal.autoscroll.core.gesture.PhysicalSwipeDirection
import com.personal.autoscroll.core.gesture.toGesturePath

class GesturePreviewOverlay(
    context: Context,
    private val gesturePlan: GesturePlan,
    private val onDismiss: () -> Unit,
) {
    val view: View = GesturePreviewView(context, gesturePlan).apply {
        isClickable = true
        setOnClickListener { onDismiss() }
    }
}

private class GesturePreviewView(
    context: Context,
    private val gesturePlan: GesturePlan,
) : View(context) {
    private var progress = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = gesturePlan.durationMillis.coerceIn(200L, 2_000L)
        startDelay = 180L
        addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
    }
    private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(178, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(215, 255, 255, 255)
        strokeWidth = dp(4f)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = dp(3f)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = dp(15f)
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val warningPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(230, 255, 221, 87)
        textAlign = Paint.Align.CENTER
        textSize = dp(12f)
    }

    init {
        animator.start()
        postDelayed({ performClick() }, gesturePlan.durationMillis.coerceIn(200L, 2_000L) + 1_250L)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)

        val gesturePath = gesturePlan.toGesturePath(width.toFloat(), height.toFloat())
        val currentX = gesturePath.startX + (gesturePath.endX - gesturePath.startX) * progress
        val currentY = gesturePath.startY + (gesturePath.endY - gesturePath.startY) * progress

        canvas.drawLine(
            gesturePath.startX,
            gesturePath.startY,
            gesturePath.endX,
            gesturePath.endY,
            pathPaint,
        )
        drawHand(canvas, currentX, currentY)

        val labelY = (currentY + dp(88f)).coerceIn(dp(140f), height - dp(120f))
        canvas.drawText(instructionText(), width / 2f, labelY, textPaint)
        if (gesturePath.clipped) {
            canvas.drawText(context.getString(R.string.preview_clipped), width / 2f, labelY + dp(26f), warningPaint)
        }
    }

    private fun drawHand(canvas: Canvas, x: Float, y: Float) {
        val path = Path().apply {
            moveTo(x, y + dp(18f))
            lineTo(x, y - dp(18f))
            cubicTo(x, y - dp(30f), x + dp(16f), y - dp(30f), x + dp(16f), y - dp(16f))
            lineTo(x + dp(16f), y + dp(4f))
            moveTo(x + dp(16f), y - dp(10f))
            lineTo(x + dp(30f), y - dp(2f))
            moveTo(x + dp(14f), y + dp(4f))
            lineTo(x + dp(28f), y + dp(14f))
            moveTo(x - dp(3f), y + dp(4f))
            lineTo(x + dp(12f), y + dp(22f))
        }
        canvas.drawPath(path, handPaint)
    }

    private fun instructionText(): String = when (gesturePlan.physicalDirection) {
        PhysicalSwipeDirection.Up -> context.getString(R.string.preview_swipe_up)
        PhysicalSwipeDirection.Down -> context.getString(R.string.preview_swipe_down)
        PhysicalSwipeDirection.Left -> context.getString(R.string.preview_swipe_left)
        PhysicalSwipeDirection.Right -> context.getString(R.string.preview_swipe_right)
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density
}

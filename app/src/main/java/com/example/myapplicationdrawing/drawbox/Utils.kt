package com.example.myapplicationdrawing.drawbox

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private fun View.getRect(x: Int, y: Int): android.graphics.Rect {
    val viewWidth = this.width
    val viewHeight = this.height
    return android.graphics.Rect(x, y, viewWidth + x, viewHeight + y)
}


//Model
data class PathWrapper(val path: Path, val strokeWidth: Float = 5f, val strokeColor: Color)

data class DrawBoxPayLoad(val bgColor: Color, val path: List<PathWrapper>)

fun createPath(points: List<Offset>) = Path().apply {
    if (points.size > 1) {
        var oldPoint: Offset? = null
        this.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val point: Offset = points[i]
            oldPoint?.let {
                val midPoint = calculateMidpoint(it, point)
                if (i == 1) {
                    this.lineTo(midPoint.x, midPoint.y)
                } else {
                    this.quadraticBezierTo(it.x, it.y, midPoint.x, midPoint.y)
                }
            }
            oldPoint = point
        }
        oldPoint?.let { this.lineTo(it.x, oldPoint.y) }
    }
}

private fun calculateMidpoint(start: Offset, end: Offset) =
    Offset((start.x + end.x) / 2, (start.y + end.y) / 2)


internal suspend fun View.drawBitmapFromView(context: Context, config: Bitmap.Config): Bitmap =
    suspendCoroutine { continuation ->
        doOnLayout { view ->
            if (Build.VERSION_CODES.O > Build.VERSION.SDK_INT) {
                continuation.resume(view.drawToBitmap(config))
                return@doOnLayout
            }

            val window =
                (context as? Activity)?.window ?: error("Can't get window from the Context")

            Bitmap.createBitmap(width, height, config).apply {
                val (x, y) = IntArray(2).apply { view.getLocationInWindow(this) }
                PixelCopy.request(
                    window,
                    getRect(x, y),
                    this,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) continuation.resume(this) else continuation.resumeWithException(
                            RuntimeException("Bitmap generation failed")
                        )
                    },
                    Handler(Looper.getMainLooper())
                )
            }
        }
    }


internal fun MotionEvent.getRect() =
    Rect(this.x - 0.5f, this.y - 0.5f, this.x + 0.5f, this.y + 0.5f)

internal fun DrawScope.drawSomePath(
    path: Path,
    color: Color,
    width: Float
) = drawPath(
    path,
    color,
    style = Stroke(width, miter = 0f, join = StrokeJoin.Round, cap = StrokeCap.Round),
)

internal fun Canvas.drawSomePath(
    path: Path,
    color: Color,
    width: Float,
) = this.drawPath(path, Paint().apply {
    this.style = PaintingStyle.Stroke
    this.isAntiAlias = true
    this.color = color
    this.strokeJoin = StrokeJoin.Round
    this.strokeCap = StrokeCap.Round
    this.strokeWidth = width
})




//Model
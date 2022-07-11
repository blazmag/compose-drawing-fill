package com.example.myapplicationdrawing

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplicationdrawing.drawbox.*
import com.example.myapplicationdrawing.drawbox.drawSomePath
import com.example.myapplicationdrawing.drawbox.getRect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawBox(
    motionEvent: MutableState<MotionEvent?>,
    validImageIndex: Int?,
    drawController: DrawController,
    modifier: Modifier = Modifier.fillMaxSize(),
    trackHistory: (undoCount: Int, redoCount: Int) -> Unit = { _, _ -> },
    isValidPath: (imageIndex: Int) -> Boolean,
) {

    var size = remember { mutableStateOf(IntSize.Zero) }
    var path = Path()
    val action: MutableState<Any?> = remember { mutableStateOf(null) }
    var imageBitmapCanvas: Canvas? = null
    var refreshState = UUID.randomUUID().toString()

    LaunchedEffect(refreshState) {
        imageBitmapCanvas = drawController.generateCanvas(size.value)
        action.value = UUID.randomUUID().toString()
        drawController.changeRequests.mapNotNull { request ->
            action.value = request
            trackHistory(drawController.undoStack.size, drawController.redoStack.size)
        }.launchIn(this)
    }
    motionEvent.value?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    path.moveTo(it.x, it.y)
                    path.addOval(it.getRect())
                }
                MotionEvent.ACTION_MOVE -> {
                    val isValid = validImageIndex?.let { it1 -> isValidPath.invoke(it1) }
                    if(isValid == true ) {
                        path.lineTo(it.x, it.y)
                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    drawController.redoStack.clear()
                    drawController.undoStack.add(
                        PathWrapper(
                            path,
                            drawController.strokeWidth,
                            drawController.strokeColor
                        )
                    )
                    trackHistory(drawController.undoStack.size, drawController.redoStack.size)
                    path = Path()
                }
                else -> false
            }
            action.value = "${it.x},${it.y}"
        }


    androidx.compose.foundation.Canvas(modifier = modifier
        .onSizeChanged {
            size.value = it
        }) {
        drawController.getDrawBoxBitmap()?.let { bitmap ->
            action.value?.let {
                drawController.undoStack.forEach {
                    imageBitmapCanvas?.drawSomePath(
                        path = it.path,
                        color = it.strokeColor,
                        width = it.strokeWidth
                    )
                }
                imageBitmapCanvas?.drawSomePath(
                    path = path,
                    color = drawController.strokeColor,
                    width = drawController.strokeWidth
                )
            }
            this.drawIntoCanvas {
                it.nativeCanvas.drawBitmap(bitmap, 0f, 0f, null)
            }
        }
    }
}

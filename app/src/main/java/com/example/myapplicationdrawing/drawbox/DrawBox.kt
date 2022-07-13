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
) {




    motionEvent.value?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {

                }
                MotionEvent.ACTION_MOVE -> {

                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {

                }
                else -> false
            }
        }



}

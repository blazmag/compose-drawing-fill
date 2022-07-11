package com.example.myapplicationdrawing

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.toSize
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.scaleMatrix
import androidx.core.view.WindowCompat
import com.example.myapplicationdrawing.drawbox.PathWrapper
import com.example.myapplicationdrawing.drawbox.getRect
import com.example.myapplicationdrawing.drawbox.rememberDrawController
import com.example.myapplicationdrawing.ui.theme.MyApplicationDrawingTheme
import io.ak1.rangvikalp.RangVikalp
import timber.log.Timber
import timber.log.Timber.*
import timber.log.Timber.Forest.plant

val ids = listOf(
    R.drawable.purdy_ear_left_inside,
    R.drawable.purdy_ear_left_outside,
    R.drawable.purdy_nose,
    R.drawable.purdy_foot_left,
    R.drawable.purdy_foot_right,
    R.drawable.purdy_ear_right_inside,
    R.drawable.purdy_ear_right_outside,
    R.drawable.purdy_tail,
    R.drawable.purdy_ribbon_centre,
    R.drawable.purdy_ribbon_left,
    R.drawable.purdy_ribbon_right,
    R.drawable.purdy_body,
    R.drawable.purdy_tongue,
    R.drawable.purdy_outline
)

enum class DrawMode {
    DRAW, TOUCH, ERASE
}

enum class Motion {
    NONE, DOWN, MOVE, UP
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        setContent {
            MyApplicationDrawingTheme {
                // A surface container using the 'background' color from the theme
                Greeting()
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Greeting() {
    val context = LocalContext.current
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var red by remember { mutableStateOf(0) }
    var green by remember { mutableStateOf(0) }
    var blue by remember { mutableStateOf(0) }

    val undoVisibility = remember { mutableStateOf(false) }
    val redoVisibility = remember { mutableStateOf(false) }
    val colorBarVisibility = remember { mutableStateOf(false) }
    val sizeBarVisibility = remember { mutableStateOf(false) }
    val currentColor = remember { mutableStateOf(Color.Black) }
    val bg = MaterialTheme.colors.background
    val currentBgColor = remember { mutableStateOf(bg) }
    val currentSize = remember { mutableStateOf(10) }
    val colorIsBg = remember { mutableStateOf(false) }
    val drawController = rememberDrawController()

    val bitmapsList = ids.map { ImageBitmap.imageResource(id = it, res = context.resources) }.toList()

    Surface(color = Color.White) {
            for (bitmap in bitmapsList) {
                Image(bitmap = bitmap, contentDescription = "", contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
            }

            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }
            var imageSize by remember { mutableStateOf(Size.Unspecified) }
            var lastMatchedIndex by remember { mutableStateOf(0) }
        var newIndex by remember { mutableStateOf(0) }

        var isValid by remember { mutableStateOf(false) }
        var motionEvent:MutableState<MotionEvent?> = remember { mutableStateOf(null) }

            Image(bitmap = bitmapsList[0], contentDescription = "", contentScale = ContentScale.FillBounds, modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    imageSize = it.toSize()
                }
                .pointerInteropFilter {
                    motionEvent.value = it
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            offsetX = it.x
                            offsetY = it.y
                            if (imageSize.isSpecified) {
                                //calculate offset
                                val scaledX = (bitmapsList[0].width / imageSize.width) * offsetX
                                val scaledY = (bitmapsList[0].height / imageSize.height) * offsetY
                                for ((i, bitmap) in bitmapsList.withIndex()) {
                                    try {
                                        val pixel = bitmap
                                            .asAndroidBitmap()
                                            .getPixel(scaledX.toInt(), scaledY.toInt())
                                        //val pixelMap = bitmap.toPixelMap(startX = xx.toInt(), startY = yy.toInt(), width = 1, height = 1)
                                        red = pixel.red
                                        green = pixel.green
                                        blue = pixel.blue

                                        //found white colour, exit loop
                                        if(red > 0) {
                                            lastMatchedIndex = i
                                            break
                                        }
                                    } catch (e: Exception) {
                                        Timber.e(e)
                                    }

                                    Timber
                                        .tag("lol")
                                        .d(
                                            "image: $i color RGB: $red $green $blue, bitmap height: ${bitmap.height} img width:${bitmap.width} container height ${imageSize.height} width ${imageSize.width}" +
                                                    "scaled x $scaledX scaled y $scaledY "
                                        )
                                }
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (imageSize.isSpecified) {
                                offsetX = it.x
                                offsetY = it.y
                                //calculate offset
                                val scaledX = (bitmapsList[0].width / imageSize.width) * offsetX
                                val scaledY = (bitmapsList[0].height / imageSize.height) * offsetY
                                for ((i, bitmap) in bitmapsList.withIndex()) {
                                    try {
                                        val pixel = bitmap
                                            .asAndroidBitmap()
                                            .getPixel(scaledX.toInt(), scaledY.toInt())
                                        //val pixelMap = bitmap.toPixelMap(startX = xx.toInt(), startY = yy.toInt(), width = 1, height = 1)
                                        red = pixel.red
                                        green = pixel.green
                                        blue = pixel.blue

                                        //found white colour, exit loop
                                        if(red > 0) {
                                            newIndex = i
                                            break
                                        }
                                    } catch (e: Exception) {
                                        Timber.e(e)
                                    }
                                    if(newIndex != lastMatchedIndex) {
                                        Timber.tag("lol").d("stop moving!")
                                    } else {
                                        Timber
                                            .tag("lol")
                                            .d("valid")
                                    }
                                }
                            }
                        }
                        MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                        }
                        else -> false
                    }
                    true
                }
//                .pointerInput(Unit) {
//                    detectTapGestures { offset: Offset ->
//                        // Touch coordinates on image view
//                        offsetX = offset.x
//                        offsetY = offset.y
//                        if (imageSize.isSpecified) {
//                            //calculate offset
//                            val scaledX = (bitmapsList[0].width / imageSize.width) * offsetX
//                            val scaledY = (bitmapsList[0].height / imageSize.height) * offsetY
//                            for ((i, bitmap) in bitmapsList.withIndex()) {
//                                try {
//                                    val pixel = bitmap
//                                        .asAndroidBitmap()
//                                        .getPixel(scaledX.toInt(), scaledY.toInt())
//                                    //val pixelMap = bitmap.toPixelMap(startX = xx.toInt(), startY = yy.toInt(), width = 1, height = 1)
//                                    red = pixel.red
//                                    green = pixel.green
//                                    blue = pixel.blue
//
//                                    //found white colour, exit loop
//                                    if(red > 0) {
//                                        lastMatchedIndex = i
//                                        break
//                                    }
//                                } catch (e: Exception) {
//                                    Timber.e(e)
//                                }
//
//                                Timber
//                                    .tag("lol")
//                                    .d(
//                                        "image: $i color RGB: $red $green $blue, bitmap height: ${bitmap.height} img width:${bitmap.width} container height ${imageSize.height} width ${imageSize.width}" +
//                                                "scaled x $scaledX scaled y $scaledY, motion $offset "
//                                    )
//                            }
//                        }
//                    }
//                }
            )

//            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize(), onDraw =
//            {
//                if (currentPosition != Offset.Unspecified) {
//                    drawCircle(color = Color.Red, center = currentPosition, radius = 20f)
//                }
//            })

            Column(modifier = Modifier.background(color = Color.White)) {
                Text(text = "offset x:$offsetX y: $offsetY", color = Color.Black)
            }


            Column(modifier = Modifier.fillMaxSize()) {
                DrawBox(
                    motionEvent = motionEvent,
                    validImageIndex = lastMatchedIndex,
                    drawController = drawController,
                    modifier = Modifier
                        .fillMaxSize(),
                    trackHistory = { undoCount, redoCount ->

                    },
                    isValidPath = {
                        true
                    }

                )
            }

        ControlsBar(
            drawController = drawController,
            {
                //drawController.
            },
            {
                colorBarVisibility.value = when (colorBarVisibility.value) {
                    false -> true
                    colorIsBg.value -> true
                    else -> false
                }
                colorIsBg.value = false
                sizeBarVisibility.value = false
            },
            {
                colorBarVisibility.value = when (colorBarVisibility.value) {
                    false -> true
                    !colorIsBg.value -> true
                    else -> false
                }
                colorIsBg.value = true
                sizeBarVisibility.value = false
            },
            {
                sizeBarVisibility.value = !sizeBarVisibility.value
                colorBarVisibility.value = false
            },
            undoVisibility = undoVisibility,
            redoVisibility = redoVisibility,
            colorValue = currentColor,
            bgColorValue = currentBgColor,
            sizeValue = currentSize
        )
        RangVikalp(isVisible = colorBarVisibility.value, showShades = true) {
            if (colorIsBg.value) {
                currentBgColor.value = it

            } else {
                currentColor.value = it
                drawController.setStrokeColor(it)
            }
            colorBarVisibility.value = false
        }
        CustomSeekbar(
            isVisible = sizeBarVisibility.value,
            progress = currentSize.value,
            progressColor = MaterialTheme.colors.primary.hashCode(),
            thumbColor = currentColor.value.hashCode()
        ) {
            currentSize.value = it
            drawController.setStrokeWidth(it.toFloat())
        }

        }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Surface() {
        Greeting()

    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingCanvas() {

    var drawMode by remember { mutableStateOf(DrawMode.DRAW) }
    var motionEvent by remember { mutableStateOf(Motion.NONE) }
    var currentPath by remember { mutableStateOf(Path()) }

    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }
    var previousPosition by remember { mutableStateOf(Offset.Unspecified) }

    val paths = remember { mutableStateListOf<Path>() }

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        motionEvent = Motion.DOWN
                        currentPosition = Offset(it.x, it.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        motionEvent = Motion.MOVE
                        currentPosition = Offset(it.x, it.y)
                    }
                    MotionEvent.ACTION_UP -> {
                        motionEvent = Motion.UP
                    }
                    else -> {
                        false
                    }
                }
                true
            }
    ) {
        when (motionEvent) {

            Motion.DOWN -> {
                if (drawMode != DrawMode.TOUCH) {
                    currentPath.moveTo(currentPosition.x, currentPosition.y)
                }

                previousPosition = currentPosition
            }
            Motion.MOVE -> {

                if (drawMode != DrawMode.TOUCH) {
                    currentPath.quadraticBezierTo(
                        previousPosition.x,
                        previousPosition.y,
                        (previousPosition.x + currentPosition.x) / 2,
                        (previousPosition.y + currentPosition.y) / 2

                    )
                }

                previousPosition = currentPosition
            }
            Motion.UP -> {
                if (drawMode != DrawMode.TOUCH) {
                    currentPath.lineTo(currentPosition.x, currentPosition.y)

                    // Pointer is up save current path
                    paths.add(currentPath)

                    // Since paths are keys for map, use new one for each key
                    // and have separate path for each down-move-up gesture cycle
                    currentPath = Path()
                }
//                currentPosition = Offset.Unspecified
                previousPosition = currentPosition
                motionEvent = Motion.NONE

                with(drawContext.canvas.nativeCanvas) {

                    val checkPoint = saveLayer(null, null)

                    paths.forEach {

                        val path = it

                        drawPath(
                            color = Color.Black,
                            path = path,
                            style = Stroke(
                                width = 10f
                            )
                        )

                    }
                    restoreToCount(checkPoint)
                }
            }
            Motion.NONE -> {
            }
        }
    }
}
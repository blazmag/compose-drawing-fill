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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.*
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
import com.example.myapplicationdrawing.ui.theme.MyApplicationDrawingTheme
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
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var red by remember { mutableStateOf(0) }
    var green by remember { mutableStateOf(0) }
    var blue by remember { mutableStateOf(0) }

    val bitmapsList = ids.map { ImageBitmap.imageResource(id = it, res = context.resources) }.toList()
    ImageBitmap.imageResource(id = R.drawable.purdy_body)

    var height = 0
    var width = 0


    Surface(color = Color.Black) {

        BoxWithConstraints(modifier = Modifier.pointerInteropFilter {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {

                    // currentPosition = Offset(it.x, it.y)
                    //offsetX = currentPosition.x
                    //offsetY = currentPosition.y
                }
            }
            false
        }) {
            height = LocalDensity.current.run { maxHeight.toPx().toInt() }
            width = LocalDensity.current.run { maxWidth.toPx().toInt() }

            for (bitmap in bitmapsList) {
                Image(bitmap = bitmap, contentDescription = "", contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
            }

            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }
            var imageSize by remember { mutableStateOf(Size.Unspecified) }

            Image(bitmap = bitmapsList[0], contentDescription = "", contentScale = ContentScale.FillBounds, modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    imageSize = it.toSize()
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset: Offset ->
                        currentPosition = offset
                        // Touch coordinates on image view
                        offsetX = offset.x
                        offsetY = offset.y
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
                                } catch (e: Exception) {
                                    Timber.e(e)
                                }

                                Timber
                                    .tag("lol")
                                    .d(
                                        "image: $i color RGB: $red $green $blue, bitmap height: ${bitmap.height} img width:${bitmap.width} container height ${imageSize.height} width ${imageSize.width}" +
                                                "scaled x $scaledX scaled y $scaledY, motion $offset current pos $currentPosition"
                                    )
                            }
                        }
                    }
                }
            )

            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize(), onDraw =
            {
                if (currentPosition != Offset.Unspecified) {
                    drawCircle(color = Color.Red, center = currentPosition, radius = 20f)
                }
            })

            Column(modifier = Modifier.background(color = Color.White)) {
                Text(text = "offset x:$offsetX y: $offsetY", color = Color.Black)
            }
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
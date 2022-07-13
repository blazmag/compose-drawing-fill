package com.example.myapplicationdrawing

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils


val white = Color(0xffffffff)
val whiteTransparent = Color.init(0xffffff, 30)
val datePickerBackground = Color.init(0x9d74c2, 80)
val background = Color(0xff3b338a)
val backgroundAlpha = Color(0x993b338a) //Alpha 60%
val inputTextFill = Color.init(0xffffff, 20)
val coral = Color(0xfff46363)
val coralDisabled = Color.init(0xf46363, 60)
val labelBackground = Color.init(0x3b338a, 60)
val labelBackgroundBorder = Color(0xffff3b33)
val labelText = Color(0xff00bafd)
val textFieldBackground = Color(0x4Dffffff)
val textFieldActiveBackground = Color(0x993b338a)
val textFieldErrorBackground = Color(0x99f46363)
val tabBackground = Color(0xff6c7fc7)
val selectedTab = Color.init(0x3b338a, 60)
val tabSelectedText = Color(0xff00bafd)
val emptyImage = Color(0xff4B4894)

val mainBackgroundStartColor = Color(0xff535AAA)
val mainBackgroundCenterColor = Color(0xff4662BE)
val mainBackgroundEndColor = Color(0xff01ABEE)

val headerAnimationEndColor = Color(0xff535AAA)

val divider = Color(0x4dffffff) //Alpha 60%

val iconButtonBackground = Color.init(0xffffff, 20)

val profilePicBackground = Color.init(0x48489c, 60)



fun Color.Companion.init(color: Int, opacity: Int): Color {
    val alpha = (opacity / 100.0 * 255.0).toInt()
    return Color(ColorUtils.setAlphaComponent(color, alpha))
}
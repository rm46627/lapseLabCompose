package com.michredk.lapselab.ui.common

import androidx.compose.ui.graphics.Color

fun fireColors(photoCount: Int, primaryContainerColor: Color, primaryColor: Color): Array<Pair<Float, Color>> {
    val black = 1f - 0.1f * (photoCount-1)
    val pink = if(photoCount > 20) 0.0f else if(photoCount > 10) 1f - 0.05f * (photoCount - 11) else 1.0f
    val blue = if (photoCount > 30) 0.55f + 0.1f * (photoCount - 31) else 0.55f
    return if (photoCount > 99)
        arrayOf(
            0.0f to Color.Yellow,
            1.0f to Color.Red,
        )
    else
        arrayOf(
            black to Color.Black,
            pink to primaryContainerColor,
            1.0f to primaryColor.copy(blue = blue),
        )

}
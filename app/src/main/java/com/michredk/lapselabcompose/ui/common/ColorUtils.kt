package com.michredk.lapselabcompose.ui.common

import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.michredk.lapselabcompose.TAG

fun fireColors(photoCount: Int, primaryContainerColor: Color, primaryColor: Color): Array<Pair<Float, Color>> {
    val black = 1f - 0.1f * (photoCount-1)
    val pink = if(photoCount > 20) 0.0f else if(photoCount > 10) 1f - 0.05f * (photoCount - 11) else 1.0f
    val blue = if (photoCount > 30) 0.55f + 0.1f * (photoCount - 31) else 0.55f
    Log.d(TAG, "photocount: $photoCount, black $black, pink $pink, blue $blue")
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
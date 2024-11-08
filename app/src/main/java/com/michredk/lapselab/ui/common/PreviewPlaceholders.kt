package com.michredk.lapselab.ui.common

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint

fun createLabeledPlaceholderBitmap(): Bitmap {
    val width = 300
    val height = 600
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        isAntiAlias = true
    }

    // Fill the bitmap with a background color
    canvas.drawColor(Color.LTGRAY)

    // Draw labels
    canvas.drawText("UP", width / 2f - 30, 30f, paint)
    canvas.drawText("LEFT", 10f, height / 2f, paint)
    canvas.drawText("RIGHT", width - 80f, height / 2f, paint)
    canvas.drawText("DOWN", width / 2f - 50, height - 10f, paint)

    return bitmap
}
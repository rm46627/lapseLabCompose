package com.example.lapselabcompose.ui.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun GridPhotoItem(photo: String) {
    Image(
        painter = rememberAsyncImagePainter(photo),
        contentDescription = "Photo",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
    )
}

//suspend fun PointerInputScope.detectPinchGestures(
//    pass: PointerEventPass = PointerEventPass.Main,
//    onGestureStart: (PointerInputChange) -> Unit = {},
//    onGesture: (
//        centroid: Offset,
//        zoom: Float
//    ) -> Unit,
//    onGestureEnd: (PointerInputChange) -> Unit = {}
//) {
//    awaitEachGesture {
//        var zoom = 1f
//        var pastTouchSlop = false
//        val touchSlop = viewConfiguration.touchSlop
//        val down: PointerInputChange = awaitFirstDown(requireUnconsumed = false, pass = pass)
//        onGestureStart(down)
//        var pointer = down
//        var pointerId = down.id
//        do {
//            val event = awaitPointerEvent(pass = pass)
//            val canceled = event.changes.any { it.isConsumed }
//            if (!canceled) {
//                val pointerInputChange = event.changes.firstOrNull { it.id == pointerId } ?: event.changes.first()
//                pointerId = pointerInputChange.id
//                pointer = pointerInputChange
//                val zoomChange = event.calculateZoom()
//                if (!pastTouchSlop) {
//                    zoom *= zoomChange
//                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
//                    val zoomMotion = abs(1 - zoom) * centroidSize
//                    if (zoomMotion > touchSlop) {
//                        pastTouchSlop = true
//                    }
//                }
//                if (pastTouchSlop) {
//                    val centroid = event.calculateCentroid(useCurrent = false)
//                    if (zoomChange != 1f) {
//                        onGesture(
//                            centroid,
//                            zoomChange
//                        )
//                        event.changes.forEach { it.consume() }
//                    }
//                }
//            }
//        } while (!canceled && event.changes.any { it.pressed })
//        onGestureEnd(pointer)
//    }
//}
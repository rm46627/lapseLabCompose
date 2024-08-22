package com.example.lapselabcompose.ui.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.lapselabcompose.R

@Composable
fun GridPhotoItem(photo: String) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp),
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo)
                .crossfade(1000)
                .transformations(
//                    CircleCropTransformation()
//                    RoundedCornersTransformation(),
//                    CustomTransformation(),
//                    BlurTransformation(LocalContext.current)
                )
//                .crossfade(true)
                .build(),
            contentDescription = "Gallery photo",
            contentScale = ContentScale.Crop,
//            modifier = Modifier.clip(CircleShape),
//            placeholder = painterResource(R.drawable.ic_image_placeholder)
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
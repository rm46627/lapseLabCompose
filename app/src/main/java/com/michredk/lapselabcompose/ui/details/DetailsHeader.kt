package com.michredk.lapselabcompose.ui.details

import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.michredk.database.Album

@OptIn(UnstableApi::class)
@Composable
fun DetailsHeader(
    expanded: Boolean,
    album: Album,
    onAddPhotoClicked: () -> Unit,
    onEditVideoClicked: () -> Unit,
    onNotificationIconClicked: () -> Unit,
    backgroundColor: Brush,
    exoPlayer: ExoPlayer,
    showVideo : Boolean,
    alpha : Float
) {
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f, animationSpec = tween(durationMillis = 1000),
        label = ""
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp * scale + 50.dp)
            .background(
                backgroundColor,
                RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (showVideo) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = false
                            player = exoPlayer
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .height(300.dp * scale)
                )
        }
        Text(
            modifier = Modifier
                .alpha(scale)
                .height(38.dp * scale)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = album.directoryName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp), verticalAlignment = Alignment.Bottom
        ) {
            Text(
                modifier = Modifier
                    .weight(3f)
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .alpha(1 - scale),
                text = album.directoryName,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall,
            )
            IconButton(onClick = onNotificationIconClicked) {
                Icon(
                    imageVector = if (album.daysBetweenReminders == 0L) Icons.Outlined.Notifications else Icons.Default.Notifications,
                    contentDescription = "Add photo button"
                )
            }
            IconButton(onClick = onAddPhotoClicked) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Add photo button"
                )
            }
            IconButton(onClick = onEditVideoClicked) {
                Icon(
                    imageVector = Icons.Default.SlowMotionVideo,
                    contentDescription = "Go to editor button"
                )
            }
        }
    }
}

@Composable
fun AlbumCoverPhoto(photo: String, scale: Float) {
    AsyncImage(
        modifier = Modifier
            .width(200.dp)
            .height(300.dp * scale)
            .alpha(scale)
            .padding(8.dp),
        model = ImageRequest.Builder(LocalContext.current)
            .data(photo)
            .crossfade(1000)
            .transformations()
            .build(),
        contentDescription = "Album cover photo",
        contentScale = ContentScale.Crop,
    )
}
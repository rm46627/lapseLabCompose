package com.michredk.lapselabcompose.ui.details

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.michredk.database.Album
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselabcompose.TAG
import kotlin.coroutines.cancellation.CancellationException

// TODO: Streak counter ( progress bar? )
// TODO: Date of the next notification / planned photo

@OptIn(UnstableApi::class)
@Composable
fun DetailsHeader(
    expanded: Boolean,
    album: Album,
    onAddPhotoClicked: () -> Unit,
    onEditVideoClicked: () -> Unit,
    onNotificationIconClicked: () -> Unit,
    backgroundColor: Brush,
    popBackStack: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = REPEAT_MODE_ONE
        }
    }
    val mediaManager = remember {
        MediaManagerFactory(context)
    }
    var showVideo by remember { mutableStateOf(true) }
    BackHandler {
        showVideo = false
        popBackStack()
    }
    var videoUriState by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        videoUriState = mediaManager.getLatestVideoFile(
            album.directoryName
        )?.absolutePath
    }

    val mediaSource = remember(videoUriState) {
        val newUri = videoUriState
        Log.d(TAG, "newUri: $newUri")
        if (newUri != null) MediaItem.fromUri(newUri) else null
    }
    LaunchedEffect(mediaSource) {
        val ms = mediaSource ?: throw CancellationException()
        exoPlayer.setMediaItem(ms)
        exoPlayer.prepare()
    }

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
        if (mediaSource != null && showVideo) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        player = exoPlayer
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp * scale)
            )
        }
//        AlbumCoverPhoto(album.coverPhotoPath, scale)
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
    // TODO: if it is possible view video
    AsyncImage(
        modifier = Modifier
//            .width(200.dp * scale)
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
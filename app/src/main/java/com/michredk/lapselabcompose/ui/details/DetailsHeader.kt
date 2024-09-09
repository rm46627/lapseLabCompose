package com.michredk.lapselabcompose.ui.details

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.michredk.database.Album
import com.michredk.lapselabcompose.ui.common.PermissionTextProvider

@Composable
fun DetailsHeader(
    expanded: Boolean,
    album: Album,
    onAddPhotoClicked: () -> Unit,
    onEditVideoClicked: () -> Unit,
    onNotificationIconClicked: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f, animationSpec = tween(durationMillis = 1000),
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp * scale + 100.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)
            )
            .alpha(scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlbumCoverPhoto(album.coverPhotoPath, scale)
        Text(
            text = album.directoryName,
            style = MaterialTheme.typography.headlineLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), verticalAlignment = Alignment.Bottom
        ) {
            Text(
                modifier = Modifier.weight(3f),
                text = album.directoryName,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.weight(2f))
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
            .width(200.dp * scale)
            .height(200.dp * scale)
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
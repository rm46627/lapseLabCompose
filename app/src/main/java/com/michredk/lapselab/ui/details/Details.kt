package com.michredk.lapselab.ui.details

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.michredk.database.Album
import com.michredk.lapselab.R
import com.michredk.lapselab.TAG
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselab.services.alarm.AlarmScheduler
import com.michredk.lapselab.ui.PermissionViewModel
import com.michredk.lapselab.ui.DetailsGraph
import com.michredk.lapselab.ui.camera.CameraDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File
import java.time.LocalTime
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class DetailsDestination(val albumName: String? = null)

// TODO: Add option to import (copy) a photo from the gallery
// TODO: add share latest video and specific photo

@Composable
fun DetailsRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController, albumName: String?,
    permissionsResultLaunch: () -> Unit,
    permissionViewModel: PermissionViewModel,
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(DetailsGraph)
    }
    val detailsViewModel: DetailsViewModel = hiltViewModel(parentEntry)

    detailsViewModel.setAlbumName(albumName ?: throw IllegalArgumentException())
    val album by detailsViewModel.album.collectAsStateWithLifecycle()
    val granted by permissionViewModel.allPermissionsGranted.collectAsStateWithLifecycle()

    val wasLapseCreatorViewed by detailsViewModel.wasLapseCreatorViewed.collectAsStateWithLifecycle(
        initialValue = true
    )
    Log.d(TAG, "wasLapseCreatorViewed $wasLapseCreatorViewed")
    val context = LocalContext.current
    val mediaManager = remember {
        MediaManagerFactory(context)
    }
    LaunchedEffect(album) {
        val photos = album?.let {
            mediaManager.getPhotoFiles(it.directoryName)
        }
        photos?.let { detailsViewModel.setPhotos(it) }
    }
    val photos by detailsViewModel.photos.collectAsStateWithLifecycle()

    val exoPlayer = remember<ExoPlayer?> {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = REPEAT_MODE_ONE

        }
    }
    val alpha = remember { Animatable(initialValue = 0f) }
    val scope = rememberCoroutineScope()

    BackHandler {
        scope.launch(Dispatchers.Main) {
            alpha.animateTo(
                0f, animationSpec = tween(
                    durationMillis = 200
                )
            )
            navController.popBackStack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
        }
    }
    photos?.let { photos ->
        val albumSafe = album ?: throw IllegalArgumentException()
        if (photos.isNotEmpty()) {
            detailsViewModel.updateCoverAndCounter(photos.first().absolutePath)
        }

        var videoUriState by remember { mutableStateOf<String?>(null) }
        var showDetailsScreen by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            videoUriState = mediaManager.getLatestVideoFile(
                albumSafe.directoryName
            )?.absolutePath
            showDetailsScreen = true
        }

        val mediaSource = remember(videoUriState) {
            val newUri = videoUriState
            if (newUri != null) {
                MediaItem.fromUri(newUri)
            } else {
                scope.launch {
                    alpha.animateTo(1f, animationSpec = tween(durationMillis = 1000))
                }
                null
            }
        }
        LaunchedEffect(mediaSource) {
            val ms = mediaSource ?: throw CancellationException()
            exoPlayer?.setMediaItem(ms)
            exoPlayer?.prepare()
        }
        var showVideo by remember {
            mutableStateOf(true)
        }
        if (showDetailsScreen) {
            DetailsScreen(
                alpha = alpha.value,
                album = albumSafe,
                photos = photos,
                onAddPhotoClicked = {
                    if (granted) {
                        scope.launch {
                            alpha.animateTo(
                                0f, animationSpec = tween(
                                    durationMillis = 300
                                )
                            )
                            showVideo = false
                            navController.navigate(CameraDestination(albumName, true))
                        }
                    } else {
                        permissionsResultLaunch()
                    }
                },
                onEditVideoClicked = {
                    scope.launch {
                        alpha.animateTo(
                            0f, animationSpec = tween(
                                durationMillis = 300
                            )
                        )
                        showVideo = false
                        navController.navigate(LabDestination(albumName))
                    }
                },
                onPhotoClicked = { index ->
                    scope.launch {
                        alpha.animateTo(
                            0f, animationSpec = tween(
                                durationMillis = 300
                            )
                        )
                        showVideo = false
                        navController.navigate(PhotoBrowserDestination(index))
                    }
                },
                onApplyNotificationDialogClicked = { time, freq ->
                    if (!granted) {
                        permissionsResultLaunch()
                    }
                    detailsViewModel.updateAlbum(albumSafe, freq, time, AlarmScheduler(context))
                },
                exoPlayer = exoPlayer!!,
                showVideo = mediaSource != null && showVideo,
                showCreatorBtnTip = !wasLapseCreatorViewed && photos.size > 1
            )
//            if (viewCreatorTip) {
//                val creatorTipAlpha by animateFloatAsState(
//                    targetValue = 1f,
//                    animationSpec = tween(durationMillis = 500), label = ""
//                )
//                Text(
//                    text = stringResource(R.string.try_lapse_creator),
//                    color = Color.Black,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier
//                        .width(100.dp)
//                        .padding(bottom = 8.dp)
//                        .alpha(creatorTipAlpha)
//                        .background(
//                            MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(
//                                corner = CornerSize(8.dp)
//                            )
//                        )
//                )
//            }
        }
    }
}

@Composable
fun DetailsScreen(
    alpha: Float,
    album: Album,
    photos: List<File>,
    onAddPhotoClicked: () -> Unit,
    onEditVideoClicked: () -> Unit,
    onPhotoClicked: (Int) -> Unit,
    onApplyNotificationDialogClicked: (LocalTime, String) -> Unit,
    exoPlayer: ExoPlayer,
    showVideo: Boolean,
    showCreatorBtnTip: Boolean
) {
    var showNotificationDialog by remember {
        mutableStateOf(false)
    }
    val topBackgroundColor = Brush.horizontalGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.inversePrimary
        )
    )
    Column(
        modifier = Modifier
            .alpha(alpha)
            .fillMaxSize()
    ) {
        val gridState = rememberLazyGridState()
        val expandedState =
            if (!showVideo) false
            else if (photos.size < 7) {
                true
            } else if (!gridState.canScrollBackward && !gridState.isScrollInProgress) {
                remember { derivedStateOf { gridState.firstVisibleItemIndex } }.value <= 1
            } else {
                gridState.lastScrolledBackward && remember { derivedStateOf { gridState.firstVisibleItemIndex } }.value == 0
            }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(topBackgroundColor)
                .padding(top = 32.dp)
        )
        DetailsHeader(
            expandedState,
            album,
            onAddPhotoClicked,
            onEditVideoClicked,
            onNotificationIconClicked = {
                showNotificationDialog = true
            },
            topBackgroundColor,
            exoPlayer = exoPlayer,
            showVideo = showVideo,
            alpha,
            showCreatorBtnTip
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2), contentPadding = PaddingValues(
                start = 12.dp, top = 16.dp, end = 12.dp, bottom = 16.dp
            ), state = gridState
        ) {
            itemsIndexed(items = photos, key = { index, _ ->
                index
            }) { index, photo ->
                GridPhotoItem(photo.absolutePath, alpha) {
                    onPhotoClicked(index)
                }
            }
        }
    }

    ConfigureNotificationsDialog(
        showDialog = showNotificationDialog,
        album = album,
        onApplyClicked = onApplyNotificationDialogClicked,
        dismissDialog = { showNotificationDialog = false }
    )
}
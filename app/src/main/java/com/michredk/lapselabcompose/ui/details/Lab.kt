package com.michredk.lapselabcompose.ui.details

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.SHOW_BUFFERING_ALWAYS
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.michredk.files.appMoviesDir
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselabcompose.R
import com.michredk.lapselabcompose.TAG
import com.michredk.lapselabcompose.services.SnackbarController
import com.michredk.lapselabcompose.services.SnackbarEvent
import com.michredk.lapselabcompose.ui.DetailsGraph
import com.michredk.video.LapseCreator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.lang.NullPointerException

// TODO: make screen animate alpha to 0f in BackHandler and on generate video btn click with showScreen
// TODO: add bitmap overlay with lapseLab logo
// TODO: add Proper progress bar with num/photos indicator
// TODO: add RGB, HSL and Contrast adjustments from media/demos/demo-transformer

@Serializable
data class LabDestination(val albumName: String? = null)

@OptIn(UnstableApi::class)
@Composable
fun LabRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    albumName: String?,
    showInterstitialAd: () -> Unit
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(DetailsGraph)
    }
    val detailsViewModel: DetailsViewModel = hiltViewModel(parentEntry)
    val album by detailsViewModel.album.collectAsStateWithLifecycle()
    val photos by detailsViewModel.photos.collectAsStateWithLifecycle()
    val uiState by detailsViewModel.labUiState.collectAsStateWithLifecycle()
    val videoProperties by detailsViewModel.videoProperties.collectAsStateWithLifecycle()

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
    var showScreen by remember { mutableStateOf(true) }
    BackHandler {
        showScreen = false
        detailsViewModel.updateVideoProperties(LabUiState())
        navController.popBackStack()
    }
    var videoUriState by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        videoUriState = mediaManager.getLatestVideoFile(
            albumName ?: throw IllegalArgumentException()
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
        Log.d(TAG, "player prepare!")
        exoPlayer.prepare()
    }

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    if (showScreen) {
        LabScreen(
            exoPlayer,
            mediaSource,
            onGenerateVideoBtnClicked = {
                isLoading = true
                showInterstitialAd()
                scope.launch(Dispatchers.IO) {
                    try {
                        val photosSafe = photos ?: throw NullPointerException()
                        if (photosSafe.size < 2) throw IllegalArgumentException()

                        val lab = LapseCreator(context, album!!)
                        val filename = lab.createVideo(
                            photos = photosSafe,
                            framesPerImage = uiState.framesPerImage,
                            bitrate = uiState.bitrate
                        )
                        MediaManagerFactory(context).saveVideo(
                            filename,
                            "$appMoviesDir/${album!!.directoryName}"
                        )
                        detailsViewModel.updateAlbum(album ?: throw NullPointerException())

                        withContext(Dispatchers.Main) {
                            navController.navigate(LabDestination(albumName)) {
                                popUpTo(LabDestination(albumName)) {
                                    inclusive = true
                                }
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        SnackbarController.sendEvent(
                            event = SnackbarEvent(
                                message = "Need at least two pictures to generate video",
                                duration = SnackbarDuration.Long
                            )
                        )
                    } finally {
                        isLoading = false
                        detailsViewModel.updateVideoProperties(uiState.copy())
                    }
                }
            },
            isLoading = isLoading,
            uiState = uiState,
            videoProperties = videoProperties,
            peaceOnValueChange = { selectedValue ->
                detailsViewModel.updateLabUiState(uiState.copy(framesPerImage = selectedValue))
            },
            bitrateOnValueChange = { selectedValue ->
                detailsViewModel.updateLabUiState(uiState.copy(bitrate = selectedValue))
            }
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun LabScreen(
    exoPlayer: ExoPlayer,
    mediaSource: MediaItem?,
    onGenerateVideoBtnClicked: () -> Unit,
    isLoading: Boolean,
    uiState: LabUiState,
    videoProperties: LabUiState,
    peaceOnValueChange: (Int) -> Unit,
    bitrateOnValueChange: (Int) -> Unit
) {
    Column(
        Modifier
            .safeDrawingPadding()
            .fillMaxSize()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (mediaSource != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        setShowRewindButton(false)
                        setShowFastForwardButton(false)
                        setShowVrButton(false)
                        setShowSubtitleButton(false)
                        setShowBuffering(SHOW_BUFFERING_ALWAYS)
                        player = exoPlayer
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // Set your desired height
            )
        } else {
            // TODO: alpha float animation for image appearing
            Image(
                painter = painterResource(id = R.drawable.camera_shutter),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(8.dp),
                contentDescription = "Video placeholder",
                contentScale = ContentScale.Crop,
            )
        }
        Button(
            modifier = Modifier.padding(top = 8.dp),
            onClick = onGenerateVideoBtnClicked,
            enabled = uiState != videoProperties
        ) {
            Text(text = "generate video")
        }
        PeaceSlider(uiState.framesPerImage, peaceOnValueChange = peaceOnValueChange)
        BitrateSlider(uiState.bitrate, bitrateOnValueChange = bitrateOnValueChange)

    }
    if (isLoading) {
        Box(
            modifier = Modifier
                .background(color = Color.White.copy(alpha = 0.5f))
                .fillMaxSize()
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(64.dp)
            )
        }
    }
}
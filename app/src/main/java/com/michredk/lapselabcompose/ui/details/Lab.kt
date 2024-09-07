package com.michredk.lapselabcompose.ui.details

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.michredk.files.appMoviesDir
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselabcompose.TAG
import com.michredk.lapselabcompose.services.SnackbarController
import com.michredk.lapselabcompose.services.SnackbarEvent
import com.michredk.lapselabcompose.ui.DetailsGraph
import com.michredk.video.LapseCreator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.lang.NullPointerException

// TODO: make screen animate alpha to 0f in BackHandler and on generate video btn click with showScreen

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

    val context = LocalContext.current
    val exoPlayer = ExoPlayer.Builder(context).build()
    val mediaManager = MediaManagerFactory(context)

    var showScreen by remember { mutableStateOf(true) }
    BackHandler {
        showScreen = false
        navController.popBackStack()
    }
    var videoUriState by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        videoUriState = mediaManager.getLatestVideoFile(
            albumName ?: throw IllegalArgumentException()
        )?.absolutePath
    }

    var mediaSource = remember(videoUriState) {
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
                scope.launch {
                    try {
                        val photosSafe = photos ?: throw NullPointerException()
                        if (photosSafe.size < 2) throw IllegalArgumentException()
                        Log.d(TAG, "after if for 2 pictures")
                        showInterstitialAd()
                        val lab = LapseCreator(context, album!!)
                        val filename = lab.createVideo(photosSafe)
                        MediaManagerFactory(context).saveVideo(
                            filename,
                            "$appMoviesDir/${album!!.directoryName}"
                        )
                        exoPlayer.release()
                        mediaSource = null
                        navController.navigate(LabDestination(albumName)) {
                            popUpTo(LabDestination(albumName)) {
                                inclusive = true
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        scope.launch {
                            SnackbarController.sendEvent(
                                event = SnackbarEvent(
                                    message = "Need at least two pictures to generate video"
                                )
                            )
                        }
                    } finally {
                        isLoading = false
                    }
                }

            },
            onTestBtnClicked = {

            },
            testBtnText = "Nothing",
            isLoading
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
fun LabScreen(
    exoPlayer: ExoPlayer,
    mediaSource: MediaItem?,
    onGenerateVideoBtnClicked: () -> Unit,
    onTestBtnClicked: () -> Unit,
    testBtnText: String,
    isLoading: Boolean
) {
    Column(
        Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (mediaSource != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // Set your desired height
            )
            exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
            exoPlayer.play()
        }
        Button(onClick = onGenerateVideoBtnClicked) {
            Text(text = "generate video")
        }
        Button(onClick = onTestBtnClicked) {
            Text(text = testBtnText)
        }

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
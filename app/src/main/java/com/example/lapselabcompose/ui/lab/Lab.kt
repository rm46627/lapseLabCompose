package com.example.lapselabcompose.ui.lab

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.files.appMoviesDir
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.TAG
import com.example.lapselabcompose.ui.DetailsGraph
import com.example.lapselabcompose.ui.details.DetailsViewModel
import com.example.video.LapseCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class LabDestination(val albumName: String? = null)

@Composable
fun LabRoute(backStackEntry: NavBackStackEntry, navController: NavHostController, albumName: String?) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(DetailsGraph)
    }
    val detailsViewModel: DetailsViewModel = hiltViewModel(parentEntry)
    val album by detailsViewModel.album.collectAsStateWithLifecycle()
    val photos by detailsViewModel.photos.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val exoPlayer = ExoPlayer.Builder(context).build()
    val mediaManager = MediaManagerFactory(context)
    val videoUriState = produceState<Uri?>(initialValue = null, albumName) {
        value = mediaManager.getLatestVideoUri(albumName?: throw IllegalArgumentException())
    }
    val latestVideoUri = videoUriState.value
    val mediaSource =remember(latestVideoUri) {
        if (latestVideoUri != null) MediaItem.fromUri(latestVideoUri) else null
    }
    LaunchedEffect(mediaSource) {
        if (mediaSource != null) {
            exoPlayer.setMediaItem(mediaSource)
            exoPlayer.prepare()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    val scope = rememberCoroutineScope()
    LabScreen(
        exoPlayer,
        mediaSource,
        onGenerateVideoBtnClicked = {
            scope.launch {
                withContext(Dispatchers.Main) {
                    val lab = LapseCreator(context, album!!)
                    val filename = lab.createVideo(photos ?: throw IllegalArgumentException())
                    MediaManagerFactory(context).saveVideo(
                        filename,
                        "$appMoviesDir/${album!!.directoryName}"
                    )
                }
            }
        }
    )

}

@Composable
fun LabScreen(exoPlayer: ExoPlayer, mediaSource: MediaItem?, onGenerateVideoBtnClicked: () -> Unit) {
    Scaffold { padding ->
        Column(Modifier.padding(padding).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
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
            }

            Button(onClick = onGenerateVideoBtnClicked) {
                Text(text = "generate video")
            }

        }
    }


}
package com.example.lapselabcompose.ui.lab

import android.util.Log
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import com.example.files.appMoviesDir
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.TAG
import com.example.lapselabcompose.ui.DetailsGraph
import com.example.lapselabcompose.ui.details.DetailsViewModel
import com.example.video.LapseCreator
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class LabDestination(val albumName: String? = null)

@OptIn(UnstableApi::class)
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

    var videoIteration by remember { mutableStateOf(0) }
    val videoUriState = produceState<String?>(initialValue = null, key1 = albumName, key2 = videoIteration) {
        value = mediaManager.getLatestVideoFile(albumName?: throw IllegalArgumentException())?.absolutePath
        Log.d(TAG, "videoUriState: $value")
    }
    val mediaSource = remember(videoUriState.value) {
        val newUri = videoUriState.value
        Log.d(TAG, "newUri: $newUri")
        if (newUri != null) MediaItem.fromUri(newUri) else null
    }
    LaunchedEffect(mediaSource) {
        if (mediaSource != null) {
            exoPlayer.setMediaItem(mediaSource)
            Log.d(TAG, "player prepare!")
            exoPlayer.prepare()
        }
    }

    LabScreen(
        exoPlayer,
        mediaSource,
        onGenerateVideoBtnClicked = {
            val lab = LapseCreator(context, album!!)
            val filename = lab.createVideo(photos ?: throw IllegalArgumentException())
            MediaManagerFactory(context).saveVideo(
                filename,
                "$appMoviesDir/${album!!.directoryName}"
            )
            exoPlayer.release()
            videoIteration++
            Log.d(TAG, "videoIteration: $videoIteration")
        },
        onReloadBtnClicked = {
            Log.d(TAG, "Reload")
            Log.d(TAG, "${exoPlayer.isReleased}")
            exoPlayer.release()
            Log.d(TAG, "${exoPlayer.isReleased}")
        }
    )

}

@Composable
fun LabScreen(exoPlayer: ExoPlayer, mediaSource: MediaItem?, onGenerateVideoBtnClicked: suspend () -> Unit, onReloadBtnClicked: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isLoading by remember{ mutableStateOf(false) }
    Scaffold { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            if (mediaSource != null) {
                Log.d(TAG, "RECOMPOSED")
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

            Button(onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        onGenerateVideoBtnClicked()
                    } finally {
                        isLoading = false
                    }
                }
            }) {
                Text(text = "generate video")
            }
            Button(onClick = onReloadBtnClicked) {
                Text(text = "reload")
            }

        }
        if(isLoading){
            Box(modifier = Modifier
                .background(color = Color.White.copy(alpha = 0.5f))
                .fillMaxSize()){
                CircularProgressIndicator(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(64.dp))
            }
        }
    }


}
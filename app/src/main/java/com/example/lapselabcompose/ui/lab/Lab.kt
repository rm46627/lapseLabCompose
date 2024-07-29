package com.example.lapselabcompose.ui.lab

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.files.appMoviesDir
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.TAG
import com.example.video.LapseCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class LabDestination(val albumName: String? = null)

const val EXAMPLE_VIDEO_URI =
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"


@Composable
fun LabRoute(albumName: String) {
//    LabScreen(exoPlayer)

    val context = LocalContext.current

    val exoPlayer = ExoPlayer.Builder(context).build()
    val mediaManager = MediaManagerFactory(context)
    val videoUriState = produceState<Uri?>(initialValue = null, albumName) {
        value = mediaManager.getLatestVideoUri(albumName)
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
//    else {
//        val scope = rememberCoroutineScope()
//        Button(onClick = {
//            scope.launch {
//                withContext(Dispatchers.Main) {
//                    val lab = LapseCreator(context, album!!)
//                    val filename = lab.createVideo(photos)
//                    MediaManagerFactory(context).saveVideo(filename, "$appMoviesDir/${album!!.directoryName}")
//                }
//            }
//        }) {
//            Text(text = "generate video")
//        }
//    }

}

@Composable
fun LabScreen(exoPlayer: ExoPlayer) {

    Scaffold { padding ->
        Column(Modifier.padding(padding)) {
            Log.d(TAG, "video recomposed")
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = exoPlayer
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Set your desired height
            )
        }
    }


}
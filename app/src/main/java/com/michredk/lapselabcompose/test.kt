package com.michredk.lapselabcompose

import android.content.Context
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
import androidx.compose.material3.Scaffold
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
import com.michredk.database.Album
import com.michredk.files.appMoviesDir
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselabcompose.TAG
import com.michredk.lapselabcompose.ui.DetailsGraph
import com.michredk.lapselabcompose.ui.details.DetailsViewModel
import com.michredk.video.LapseCreator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File
import java.lang.NullPointerException

suspend fun generateVideo(context: Context, album: Album, photos: List<File>) {
    val lab = LapseCreator(context, album!!)
    val filename = lab.createVideo(photos ?: throw IllegalArgumentException())
    MediaManagerFactory(context).saveVideo(
        filename,
        "$appMoviesDir/${album!!.directoryName}"
    )
}

@OptIn(UnstableApi::class)
@Composable
fun VideoScreenRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    albumName: String?
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(DetailsGraph)
    }
    val detailsViewModel: DetailsViewModel = hiltViewModel(parentEntry)
    val album by detailsViewModel.album.collectAsStateWithLifecycle()
    val photos by detailsViewModel.photos.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var exoPlayer = ExoPlayer.Builder(context).build()
    val mediaManager = MediaManagerFactory(context)


    var videoUriState by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val result = mediaManager.getLatestVideoFile(
            albumName ?: throw IllegalArgumentException()
        )?.absolutePath
        videoUriState = result; Log.d(TAG, "result: $result")
    }

//    var mediaSource = remember(videoUriState) {
//        val newUri = videoUriState; Log.d(TAG, "new uri: $newUri")
//        if (newUri != null) MediaItem.fromUri(newUri) else null
//    }
//    LaunchedEffect(mediaSource) {
//        val ms = mediaSource ?: throw CancellationException()
//        exoPlayer.setMediaItem(ms); Log.d(TAG, "mediaItem set")
//        exoPlayer.prepare(); Log.d(TAG, "player prepared")
//    }
    val mediaSource = remember(videoUriState) {
        val newUri = videoUriState ; Log.d(TAG, "new uri: $newUri")
        if (newUri != null) {
            val ms = MediaItem.fromUri(newUri)
            exoPlayer.setMediaItem(ms); Log.d(TAG, "mediaItem set")
            exoPlayer.prepare(); Log.d(TAG, "player prepared")
            exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ONE
            exoPlayer.seekToNextMediaItem()
            ms
        } else {
            null
        }
    }

    val scope = rememberCoroutineScope()
    VideoScreen(
        exoPlayer,
        mediaSource,
        onGenerateBtnClicked = {
            scope.launch {
                val alb = album ?: throw NullPointerException()
                val ph = photos ?: throw NullPointerException()
                generateVideo(context, alb, ph)
                videoUriState = mediaManager.getLatestVideoFile(
                    albumName ?: throw IllegalArgumentException()
                )?.absolutePath
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
fun VideoScreen(
    exoPlayer: ExoPlayer, mediaSource: MediaItem?, onGenerateBtnClicked: () -> Unit
) {
    Scaffold { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Log.d(TAG, "mediaSource: ${mediaSource}")
            if (mediaSource != null) {
                Log.d(TAG, "recomposed")
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
                exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_OFF
                exoPlayer.play()
            }
            Button(onClick = onGenerateBtnClicked) {
                Text(text = "Generate new video")
            }

        }
    }
}
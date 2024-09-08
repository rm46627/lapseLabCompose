package com.michredk.lapselabcompose.ui.details

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
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
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.lang.NullPointerException
import kotlin.math.roundToInt

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
    val uiState by detailsViewModel.labUiState.collectAsStateWithLifecycle()
    val videoProperties by detailsViewModel.videoProperties.collectAsStateWithLifecycle()

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

                        showInterstitialAd()
                        val lab = LapseCreator(context, album!!)
                        Log.d(TAG, "${uiState.toString()}")
                        val filename = lab.createVideo(
                            photos = photosSafe,
                            framesPerImage = uiState.framesPerImage,
                            bitrate = uiState.bitrate
                        )
                        MediaManagerFactory(context).saveVideo(
                            filename,
                            "$appMoviesDir/${album!!.directoryName}"
                        )
                        exoPlayer.release()
                        detailsViewModel.updateAlbum(album ?: throw NullPointerException())
                        mediaSource = null
                        navController.navigate(LabDestination(albumName)) {
                            popUpTo(LabDestination(albumName)) {
                                inclusive = true
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        SnackbarController.sendEvent(
                            event = SnackbarEvent(
                                message = "Need at least two pictures to generate video"
                            )
                        )
                    } finally {
                        isLoading = false
                    }
                }
            },
            onTestBtnClicked = {

            },
            testBtnText = "Nothing",
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

@Composable
fun LabScreen(
    exoPlayer: ExoPlayer,
    mediaSource: MediaItem?,
    onGenerateVideoBtnClicked: () -> Unit,
    onTestBtnClicked: () -> Unit,
    testBtnText: String,
    isLoading: Boolean,
    uiState: LabUiState,
    videoProperties: LabUiState,
    peaceOnValueChange: (Int) -> Unit,
    bitrateOnValueChange: (Int) -> Unit
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
        Button(modifier = Modifier.padding(top = 8.dp), onClick = onGenerateVideoBtnClicked, enabled = uiState != videoProperties) {
            Text(text = "generate video")
        }
        PeaceSlider2(uiState.framesPerImage, peaceOnValueChange = peaceOnValueChange)
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

@Composable
fun PeaceSlider(currentValue: Int, peaceOnValueChange: (Int) -> Unit) {
    val strValues = listOf("Super Slow", "Slow", "Moderate", "Fast", "Super Fast")
    val intValues = listOf(60, 30, 15, 7, 3)
    val valueToPositionMap: Map<Int, Float> = mapOf(
        30 to 0f,
        15 to 1f,
        7 to 2f,
        3 to 3f,
        1 to 4f
    )
    val positionToValueMap: Map<Float, Int> = mapOf(
        0f to 60,
        1f to 30,
        2f to 15,
        3f to 7,
        4f to 3
    )
    val title = "Video Peace"

    // Keep track of the current slider position
    val position = valueToPositionMap[currentValue] ?: 1f
    var sliderPosition by remember { mutableFloatStateOf(position) }

    // Map the slider's position to the corresponding string index
    val currentIndex = sliderPosition.roundToInt().coerceIn(0, strValues.size - 1)
    val selectedValue = strValues[currentIndex]

    Column(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the current selected string value
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = sliderPosition,
            onValueChange = { newPosition ->
                sliderPosition = newPosition
//                val strVal = values[newPosition.roundToInt().coerceIn(0, values.size - 1)]
                val newValue = positionToValueMap[newPosition] ?: 30
                peaceOnValueChange(newValue)

            },

            valueRange = 0f..(strValues.size - 1).toFloat(),
            steps = strValues.size - 2 // Steps between string values
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text(text = selectedValue, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun PeaceSlider2(currentValue: Int, peaceOnValueChange: (Int) -> Unit) {
    val strValues = listOf("Super Slow", "Slow", "Moderate", "Fast", "Super Fast")
    val intValues = listOf(60, 30, 15, 7, 3)

    val position = intValues.indexOf(currentValue).takeIf { it != -1 }?.toFloat() ?: 1f
    var sliderPosition by remember { mutableFloatStateOf(position) }

    val currentIndex = sliderPosition.roundToInt().coerceIn(0, strValues.size - 1)
    val selectedValue = strValues[currentIndex]

    Column(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Video Peace", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = sliderPosition,
            onValueChange = { newPosition ->
                sliderPosition = newPosition
                peaceOnValueChange(intValues[newPosition.roundToInt().coerceIn(0, intValues.size - 1)])
            },
            valueRange = 0f..(strValues.size - 1).toFloat(),
            steps = strValues.size - 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = selectedValue, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun BitrateSlider(currentValue: Int, bitrateOnValueChange: (Int) -> Unit) {
    val values = listOf(1000000, 1250000, 1500000, 1750000, 2000000)

    // Determine the slider's position based on the current value
    var sliderPosition by remember {
        mutableFloatStateOf(values.indexOf(currentValue).takeIf { it != -1 }?.toFloat() ?: 1f)
    }

    // Calculate the current index and selected value based on slider position
    val currentIndex = sliderPosition.roundToInt().coerceIn(0, values.size - 1)
    val selectedValue = values[currentIndex]

    Column(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Bitrate", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = sliderPosition,
            onValueChange = { newPosition ->
                sliderPosition = newPosition
                bitrateOnValueChange(values[newPosition.roundToInt().coerceIn(0, values.size - 1)])
            },
            valueRange = 0f..(values.size - 1).toFloat(),
            steps = values.size - 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = "${selectedValue / 1000} kb/sec", style = MaterialTheme.typography.titleSmall)
    }
}
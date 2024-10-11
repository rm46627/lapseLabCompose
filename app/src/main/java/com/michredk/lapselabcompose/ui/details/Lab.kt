package com.michredk.lapselabcompose.ui.details

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
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
            repeatMode = REPEAT_MODE_ONE
            playWhenReady = true
        }
    }
    val scope = rememberCoroutineScope()
    val alpha = remember {
        Animatable(initialValue = 0f)
    }
    var isVideoInProgress by remember { mutableStateOf(false) }

    val mediaManager = remember {
        MediaManagerFactory(context)
    }
    BackHandler {
        scope.launch(Dispatchers.Main) {
            alpha.animateTo(
                0f, animationSpec = tween(
                    durationMillis = 300
                )
            )
            detailsViewModel.updateVideoProperties(LabUiState())
            navController.popBackStack()
        }
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

    LaunchedEffect(Unit) {
        scope.launch {
            alpha.animateTo(
                1f, animationSpec = tween(
                    durationMillis = 300
                )
            )
        }
    }
    var encodingProgressCurrent by remember {
        mutableIntStateOf(0)
    }
    var encodingProgressEnd by remember {
        mutableIntStateOf(0)
    }
    LabScreen(
        alpha.value,
        exoPlayer,
        mediaSource,
        onGenerateVideoBtnClicked = {
            isVideoInProgress = true
            showInterstitialAd()
            scope.launch(Dispatchers.IO) {
                try {
                    val photosSafe = photos ?: throw NullPointerException()
                    if (photosSafe.size < 2) throw IllegalArgumentException()

                    val lab = LapseCreator(context, album!!)
                    val filename = lab.createVideo(
                        photos = photosSafe,
                        framesPerImage = uiState.framesPerImage,
                        bitrate = uiState.bitrate,
                        rotation = uiState.rotation,
                        startFromLatest = uiState.startFromLatest,
                        rewindEffect = uiState.rewindEffect,
                        encodingProgress = { current, end ->
                            encodingProgressCurrent = current
                            encodingProgressEnd = end
                        }
                    )
                    mediaManager.saveVideo(
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
                            message = context.getString(R.string.need_at_least_two_pictures_to_generate_video),
                            duration = SnackbarDuration.Long
                        )
                    )
                } finally {
                    isVideoInProgress = false
                    encodingProgressEnd = 0
                    detailsViewModel.updateVideoProperties(uiState.copy())
                }
            }
        },
        isLoading = isVideoInProgress,
        encodingProgressEnd = encodingProgressEnd,
        encodingProgressCurrent = encodingProgressCurrent,
        uiState = uiState,
        videoProperties = videoProperties,
        peaceOnValueChange = { selectedValue ->
            detailsViewModel.updateLabUiState(uiState.copy(framesPerImage = selectedValue))
        },
        bitrateOnValueChange = { selectedValue ->
            detailsViewModel.updateLabUiState(uiState.copy(bitrate = selectedValue))
        },
        rotationOnValueChange = { selectedValue ->
            detailsViewModel.updateLabUiState(uiState.copy(rotation = selectedValue))

        },
        startFromLatestOnValueChange = { selectedValue ->
            detailsViewModel.updateLabUiState(uiState.copy(startFromLatest = selectedValue))
        },
        loopVideoOnValueChange = { selectedValue ->
            detailsViewModel.updateLabUiState(uiState.copy(rewindEffect = selectedValue))
        }
    )
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun LabScreen(
    alpha: Float,
    exoPlayer: ExoPlayer,
    mediaSource: MediaItem?,
    onGenerateVideoBtnClicked: () -> Unit,
    isLoading: Boolean,
    encodingProgressEnd: Int,
    encodingProgressCurrent: Int,
    uiState: LabUiState,
    videoProperties: LabUiState,
    peaceOnValueChange: (Int) -> Unit,
    bitrateOnValueChange: (Int) -> Unit,
    rotationOnValueChange: (Float) -> Unit,
    startFromLatestOnValueChange: (Boolean) -> Unit,
    loopVideoOnValueChange: (Boolean) -> Unit
) {
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(
                top = 32.dp,
                bottom = WindowInsets.systemBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (alpha > 0f || mediaSource != null) {
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
                    .alpha(alpha)
                    .height(350.dp) // Set your desired height
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.camera_shutter),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(horizontal = 8.dp),
                contentDescription = "Video placeholder",
                contentScale = ContentScale.Crop,
            )
        }
        OutlinedButton(
            modifier = Modifier.padding(top = 8.dp),
            onClick = onGenerateVideoBtnClicked,
            enabled = uiState != videoProperties
        ) {
            Text(text = stringResource(R.string.generate_video))
        }

        PeaceSlider(uiState.framesPerImage, peaceOnValueChange = peaceOnValueChange)
        BitrateSlider(uiState.bitrate, bitrateOnValueChange = bitrateOnValueChange)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RotationSelector(uiState.rotation, rotationOnValueChange = rotationOnValueChange)
            VideoStartSelector(
                uiState.startFromLatest,
                startFromLatestOnValueChange
            )
        }
        VideoLooperSelector(uiState.rewindEffect, loopVideoOnValueChange)
    }
    if (isLoading) {
        Box(
            modifier = Modifier
                .background(color = Color.White.copy(alpha = 0.5f))
                .fillMaxSize()
        ) {
            if (encodingProgressEnd != 0) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    CircularProgressIndicator(modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(64.dp),
                        progress = { encodingProgressCurrent.toFloat() / encodingProgressEnd.toFloat() })
                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(8.dp)
                            .width(200.dp),
                        text = stringResource(R.string.please_don_t_leave_the_app_until_the_video_finishes_generating),
                        style = TextStyle(
                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            textAlign = TextAlign.Center
                        )
                    )
                }

            } else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(64.dp)
                )
            }
        }
    }
}

@Composable
fun VideoLooperSelector(loopVideo: Boolean, loopVideoOnValueChange: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.rewind_effect), textAlign = TextAlign.Center)
        OutlinedButton(onClick = { loopVideoOnValueChange(!loopVideo) }) {
            Icon(
                imageVector = Icons.Default.Loop,
                contentDescription = ""
            )
            Text(text = if (loopVideo) stringResource(R.string.on) else stringResource(R.string.off))
        }
    }

}

@Composable
fun VideoStartSelector(startFromLatest: Boolean, startFromLatestOnValueChange: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.start_video_from), textAlign = TextAlign.Center)
        OutlinedButton(onClick = { startFromLatestOnValueChange(!startFromLatest) }) {
            Icon(
                imageVector = if (startFromLatest) Icons.Default.KeyboardDoubleArrowLeft else Icons.Default.KeyboardDoubleArrowRight,
                contentDescription = ""
            )
            Text(text = if (startFromLatest) stringResource(R.string.latest_photo) else stringResource(
                R.string.newest_photo
            )
            )
        }
    }

}

@Composable
fun RotationSelector(rotation: Float, rotationOnValueChange: (Float) -> Unit) {
    val rotationValues = remember {
        listOf(0f, 90f, 180f, 270f)
    }
    val rotationAnim = remember {
        Animatable(initialValue = rotation)
    }
    var currentRotation by remember {
        mutableIntStateOf(rotationValues.indexOf(rotation))
    }
    val scope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.rotation),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Row {
            IconButton(onClick = {
                currentRotation = ((currentRotation - 1) % 4 + 4) % 4
                scope.launch {
                    rotationAnim.animateTo(
                        targetValue = -rotationValues[currentRotation], animationSpec = tween(
                            durationMillis = 600
                        )
                    )
                }
                rotationOnValueChange(rotationValues[currentRotation])
            }) {
                Icon(
                    modifier = Modifier.size(35.dp),
                    imageVector = Icons.Default.Rotate90DegreesCw,
                    contentDescription = "Rotate video left"
                )
            }

            Icon(
                modifier = Modifier
                    .size(50.dp)
                    .rotate(rotationAnim.value),
                imageVector = Icons.Default.Image,
                contentDescription = "Rotation preview"
            )
            IconButton(onClick = {
                currentRotation = ((currentRotation + 1) % 4 + 4) % 4
                scope.launch {
                    rotationAnim.animateTo(
                        targetValue = -rotationValues[currentRotation], animationSpec = tween(
                            durationMillis = 600
                        )
                    )
                }
                rotationOnValueChange(rotationValues[currentRotation])
            }) {
                Icon(
                    modifier = Modifier.size(35.dp),
                    imageVector = Icons.Default.Rotate90DegreesCcw,
                    contentDescription = "Rotate video right"
                )
            }
        }
        Text(
            text = "${rotationValues[currentRotation]}\u00B0",
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }
}

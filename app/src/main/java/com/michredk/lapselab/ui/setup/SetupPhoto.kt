package com.michredk.lapselab.ui.setup

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselab.R
import com.michredk.lapselab.ui.SetupGraph
import com.michredk.lapselab.ui.common.BackHandlingDialog
import com.michredk.lapselab.ui.camera.CameraDestination
import com.michredk.lapselab.ui.gallery.GalleryDestination
import kotlinx.coroutines.launch
import androidx.navigation.NavBackStackEntry
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.michredk.lapselab.TAG
import com.michredk.lapselab.services.alarm.AlarmScheduler
import com.michredk.lapselab.ui.details.LabDestination
import kotlinx.serialization.Serializable

//TODO: Display modal explaining storing photos and how to exclude them from the system app gallery
// creating .nomedia file

@Serializable
data class SetupPhotoDestination(val albumName: String? = null)

@Composable
fun SetupPhotoRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    albumName: String?
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(SetupGraph)
    }
    val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mediaManager = MediaManagerFactory(context)
    val alarmScheduler = AlarmScheduler(context)
    val wasFirstAlbumEverCreated by setupViewModel.wasFirstAlbumEverCreated.collectAsStateWithLifecycle(
        initialValue = false
    )

    setupViewModel.albumName?.let {
        SetupPhotoScreen(
            onFirstImagePreviewClicked = { albumName ->
                    navController.navigate(CameraDestination(albumName)) {
                        popUpTo(SetupPhotoDestination()) {
                            inclusive = true
                        }
                    }
            },
            onCreateAlbumClicked = { imagePath ->
                if (wasFirstAlbumEverCreated) {
                    navController.popBackStack()
                } else {
                    navController.navigate(LabDestination(albumName, true)){
                        popUpTo(SetupGraph) {
                            inclusive = true
                        }
                    }
                }
                setupViewModel.createNewAlbum(imagePath, alarmScheduler)
                coroutineScope.launch {
                    Log.d(TAG, "media manager: ${Thread.currentThread().name}")
                    if (wasFirstAlbumEverCreated) {
                        mediaManager.removeLeftoverPhotosFromNewAlbum(it, imagePath)
                    } else {
                        setupViewModel.updateWasFirstAlbumEverCreated()
                    }
                }
            },
            onLeaveAlertClicked = {
                coroutineScope.launch {
                    mediaManager.deleteAlbum(it)
                }
                navController.popBackStack()
            },
            albumName = it,
            wasFirstAlbumEverCreated = wasFirstAlbumEverCreated
            )
    } ?: albumName.let {
        setupViewModel.albumName = it
    }
}

@Composable
fun SetupPhotoScreen(
    onFirstImagePreviewClicked: (String) -> Unit,
    onCreateAlbumClicked: (String) -> Unit,
    onLeaveAlertClicked: () -> Unit,
    albumName: String,
    wasFirstAlbumEverCreated: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var photoPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            photoPath = MediaManagerFactory(context).getLatestPhotoFile(albumName)?.absolutePath
        }
    }
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            modifier = Modifier
                .weight(3f)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            val offset = 400f
            val titleBrush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.primary
                ), tileMode = TileMode.Mirror, start = Offset(0f, 0f), end = Offset(offset, offset)
            )
            Text(
                textAlign = TextAlign.Center,
                style = TextStyle(
                    brush = titleBrush,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                ),
                text = stringResource(R.string.add_you_first_photos)
            )
            AsyncImage(
                modifier = Modifier
                    .padding(8.dp)
                    .width(300.dp)
                    .height(400.dp)
                    .clickable { onFirstImagePreviewClicked(albumName) },
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoPath)
                    .crossfade(1000)
                    .transformations()
                    .build(),
                contentDescription = "First album photo",
                error = painterResource(id = R.drawable.ic_add_photo)
            )
            if(photoPath != null) {
                Button(onClick = {
                    onCreateAlbumClicked(photoPath!!)
                }) {
                    Text(text = stringResource(
                            if (wasFirstAlbumEverCreated)
                                R.string.create_new_album
                        else
                            R.string.continue_to_lab
                        )
                    )
                }
            }
            else {
                Spacer(modifier = Modifier)
            }
        }

    }

    val coroutineScope = rememberCoroutineScope()
    BackHandlingDialog(title = stringResource(R.string.leave_album_creation),
        text = stringResource(R.string.if_you_exit_now_you_will_lose_your_creation_progress_are_you_sure_you_want_to_do_this),
        onLeaveClicked = {
            coroutineScope.launch {
                photoPath?.let {
                    MediaManagerFactory(context).deleteAlbum(albumName)
                }
            }
            onLeaveAlertClicked()
        })
}
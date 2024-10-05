package com.michredk.lapselabcompose.ui.setup

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.michredk.lapselabcompose.ui.PermissionViewModel
import com.michredk.lapselabcompose.R
import com.michredk.lapselabcompose.ui.SetupGraph
import com.michredk.lapselabcompose.ui.common.BackHandlingDialog
import com.michredk.lapselabcompose.ui.camera.CameraDestination
import com.michredk.lapselabcompose.ui.gallery.GalleryDestination
import kotlinx.coroutines.launch
import androidx.navigation.NavBackStackEntry
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.michredk.lapselabcompose.TAG
import com.michredk.lapselabcompose.services.alarm.AlarmScheduler
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
                navController.navigate(GalleryDestination) {
                    popUpTo(GalleryDestination) {
                        inclusive = true
                    }
                }
                coroutineScope.launch {
                    Log.d(TAG, "media manager: ${Thread.currentThread().name}")
                    mediaManager.removeLeftoverPhotosFromNewAlbum(it, imagePath)
                }
                setupViewModel.createNewAlbum(imagePath, alarmScheduler)
            },
            onLeaveAlertClicked = {
                coroutineScope.launch {
                    mediaManager.deleteAlbum(it)
                }
                navController.popBackStack()
            },
            albumName = it,

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
    albumName: String
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
                text = "Add you first photo!"
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
                OutlinedButton(onClick = {
                    onCreateAlbumClicked(photoPath!!)
                }) {
                    Text(text = "Create new album")
                }
            }
            else {
                Spacer(modifier = Modifier)
            }
        }

    }

    val coroutineScope = rememberCoroutineScope()
    BackHandlingDialog(title = "Leave album creation?",
        text = "If you exit now, you will lose your creation progress. Are you sure you want to do this?",
        onLeaveClicked = {
            coroutineScope.launch {
                photoPath?.let {
                    MediaManagerFactory(context).deleteAlbum(albumName)
                }
            }
            onLeaveAlertClicked()
        })
}
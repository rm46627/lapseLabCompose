package com.michredk.lapselabcompose.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.michredk.database.Album
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselabcompose.services.alarm.AlarmScheduler
import com.michredk.lapselabcompose.ui.PermissionViewModel
import com.michredk.lapselabcompose.ui.DetailsGraph
import com.michredk.lapselabcompose.ui.camera.CameraDestination
import kotlinx.serialization.Serializable
import java.io.File
import java.time.LocalTime

@Serializable
data class DetailsDestination(val albumName: String? = null)

// TODO: Add option to import (copy) a photo from the gallery
// TODO: add share latest video and specific photo

@Composable
fun DetailsRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController, albumName: String?,
    permissionsResultLaunch: () -> Unit,
    permissionViewModel: PermissionViewModel,
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(DetailsGraph)
    }
    val detailsViewModel: DetailsViewModel = hiltViewModel(parentEntry)

    detailsViewModel.setAlbumName(albumName ?: throw IllegalArgumentException())
    val album by detailsViewModel.album.collectAsStateWithLifecycle()
    val granted by permissionViewModel.allPermissionsGranted.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(album) {
        val photos = album?.let {
            MediaManagerFactory(context).getPhotoFiles(it.directoryName)
        }
        photos?.let { detailsViewModel.setPhotos(it) }
    }
    val photos by detailsViewModel.photos.collectAsStateWithLifecycle()
    var showVideo by remember { mutableStateOf(false) }
    photos?.let {
        val albumSafe = album ?: throw IllegalArgumentException()
        if (it.isNotEmpty()) {
            detailsViewModel.updateCoverAndCounter(it.first().absolutePath)
        }
        DetailsScreen(
            album = albumSafe,
            photos = it,
            onAddPhotoClicked = {
                if (granted) {
                    showVideo = false
                    navController.navigate(CameraDestination(albumName, true))
                } else {
                    permissionsResultLaunch()
                }
            },
            onEditVideoClicked = {
                showVideo = false
                navController.navigate(LabDestination(albumName))
            },
            onPhotoClicked = { index ->
                showVideo = false
                navController.navigate(PhotoBrowserDestination(index))
            },
            onApplyNotificationDialogClicked = { time, freq ->
                if(!granted){
                    permissionsResultLaunch()
                }
                detailsViewModel.updateAlbum(albumSafe, freq, time, AlarmScheduler(context))
            },
            popBackStack = {
                showVideo = false
                navController.popBackStack()
            },
            showVideo = showVideo,
            toggleShowVideo = { value ->
                showVideo = value
            }
        )
    }
}

@Composable
fun DetailsScreen(
    album: Album,
    photos: List<File>,
    onAddPhotoClicked: () -> Unit,
    onEditVideoClicked: () -> Unit,
    onPhotoClicked: (Int) -> Unit,
    onApplyNotificationDialogClicked: (LocalTime, String) -> Unit,
    popBackStack: () -> Unit,
    showVideo: Boolean,
    toggleShowVideo: (Boolean) -> Unit
) {
    var showNotificationDialog by remember {
        mutableStateOf(false)
    }
    val topBackgroundColor = Brush.horizontalGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.inversePrimary
        )
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val gridState = rememberLazyGridState()
        val expandedState by remember {
            derivedStateOf {
                if (photos.size < 7)
                    true
                else if (!gridState.canScrollBackward && !gridState.isScrollInProgress){
                    gridState.firstVisibleItemIndex <= 1
                }
                else
                    gridState.lastScrolledBackward && gridState.firstVisibleItemIndex == 0
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(topBackgroundColor)
        )
        DetailsHeader(
            expandedState,
            album,
            onAddPhotoClicked,
            onEditVideoClicked,
            onNotificationIconClicked = {
                showNotificationDialog = true
            },
            topBackgroundColor,
            popBackStack = popBackStack,
            showVideo = showVideo,
            setShowVideo = toggleShowVideo
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2), contentPadding = PaddingValues(
                start = 12.dp, top = 16.dp, end = 12.dp, bottom = 16.dp
            ), state = gridState
        ) {
            itemsIndexed(items = photos, key = { index, _ ->
                index
            }) { index, photo ->
                GridPhotoItem(photo.absolutePath) {
                    onPhotoClicked(index)
                }
            }
        }
    }

    ConfigureNotificationsDialog(
        showDialog = showNotificationDialog,
        album = album,
        onApplyClicked = onApplyNotificationDialogClicked,
        dismissDialog = { showNotificationDialog = false }
    )

}
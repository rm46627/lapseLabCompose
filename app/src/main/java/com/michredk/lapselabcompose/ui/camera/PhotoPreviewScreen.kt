package com.michredk.lapselabcompose.ui.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselabcompose.ui.details.DetailsDestination
import com.michredk.lapselabcompose.ui.CameraGraph
import com.michredk.lapselabcompose.ui.DetailsGraph
import com.michredk.lapselabcompose.ui.SetupGraph
import com.michredk.lapselabcompose.ui.setup.SetupPhotoDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class PhotoPreviewDestination(val navigatedFromAlbumDetails: Boolean = false)

@Composable
fun PhotoPreviewRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    navigatedFromAlbumDetails: Boolean = false,

    ) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(CameraGraph)
    }
    val viewModel: CameraViewModel = hiltViewModel(parentEntry)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mediaManager = MediaManagerFactory(context)

    viewModel.bitmap?.let { bitmap ->
        PhotoPreviewScreen(
            bitmap = bitmap,
            onDiscardClicked = {
                scope.launch(Dispatchers.IO) {
                    mediaManager.deleteLatestPhoto(viewModel.albumName)
                    viewModel.bitmap = BitmapFactory.decodeFile(mediaManager.getLatestPhotoFile(viewModel.albumName!!)?.path)
                }
                navController.navigate(CameraDestination(viewModel.albumName, navigatedFromAlbumDetails)) {
                    popUpTo(CameraDestination(viewModel.albumName, navigatedFromAlbumDetails)) {
                        inclusive = true
                    }
                }
            },
            onAcceptClicked = {
                val navFromDest: Any
                val popUpToDest: Any
                if (navigatedFromAlbumDetails) {
                    navFromDest = DetailsDestination(viewModel.albumName)
                    popUpToDest = DetailsGraph
                } else {
                    navFromDest = SetupPhotoDestination(viewModel.albumName)
                    popUpToDest = SetupGraph
                }
                navController.navigate(navFromDest) {
                    popUpTo(popUpToDest) {
                        inclusive = true
                    }
                }
            }
        )
    }
}

@Composable
fun PhotoPreviewScreen(
    bitmap: Bitmap,
    onDiscardClicked: () -> Unit,
    onAcceptClicked: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .safeContentPadding()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Buttons(onDiscardClicked, onAcceptClicked)
        }
    }

}

@Composable
private fun Buttons(onDiscardClicked: () -> Unit, onAcceptClicked: () -> Unit) {
    val backgroundColor = MaterialTheme.colorScheme.primaryContainer
    val pressedBackgroundColor = MaterialTheme.colorScheme.primary
    val iconSize = 30.dp
    val btnSize = 40.dp

    val dscInteractionSource = remember { MutableInteractionSource() }
    val dscIsPressed by dscInteractionSource.collectIsPressedAsState()
    val dscBackgroundColor =
        if (dscIsPressed) pressedBackgroundColor else backgroundColor
    IconButton(
        interactionSource = dscInteractionSource,
        onClick = onDiscardClicked,
        modifier = Modifier
            .size(btnSize)
            .background(dscBackgroundColor, shape = CircleShape),
    ) {
        Icon(
            modifier = Modifier
                .size(iconSize),
            imageVector = Icons.Default.Delete,
            contentDescription = "Discard image button"
        )
    }
    val accInteractionSource = remember { MutableInteractionSource() }
    val acceptIsPressed by accInteractionSource.collectIsPressedAsState()
    val accBackgroundColor =
        if (acceptIsPressed) pressedBackgroundColor else backgroundColor
    IconButton(
        interactionSource = accInteractionSource,
        onClick = onAcceptClicked,
        modifier = Modifier
            .size(btnSize)
            .background(accBackgroundColor, shape = CircleShape),
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            imageVector = Icons.Default.Check,
            contentDescription = "Accept image button"
        )
    }
}
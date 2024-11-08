package com.michredk.lapselab.ui.camera

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.ui.unit.LayoutDirection
import android.util.LayoutDirection.*
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.michredk.lapselab.TAG
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselab.ui.details.DetailsDestination
import com.michredk.lapselab.ui.CameraGraph
import com.michredk.lapselab.ui.DetailsGraph
import com.michredk.lapselab.ui.common.createLabeledPlaceholderBitmap
import com.michredk.lapselab.ui.details.LabDestination
import com.michredk.lapselab.ui.setup.SetupPhotoDestination
import com.michredk.lapselab.ui.theme.LapseLabComposeTheme
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
    val cameraViewModel: CameraViewModel = hiltViewModel(parentEntry)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mediaManager = MediaManagerFactory(context)
    val wasFirstAlbumEverCreated by cameraViewModel.wasFirstAlbumEverCreated.collectAsStateWithLifecycle(
        initialValue = false
    )

    cameraViewModel.bitmap?.let { bitmap ->
        PhotoPreviewScreen(
            bitmap = bitmap,
            onDiscardClicked = {
                scope.launch(Dispatchers.IO) {
                    mediaManager.deleteLatestPhoto(cameraViewModel.albumName)
                    cameraViewModel.bitmap =
                        BitmapFactory.decodeFile(mediaManager.getLatestPhotoFile(cameraViewModel.albumName!!)?.path)
                }
                navController.navigate(
                    CameraDestination(
                        cameraViewModel.albumName,
                        navigatedFromAlbumDetails
                    )
                ) {
                    popUpTo(
                        CameraDestination(
                            cameraViewModel.albumName,
                            navigatedFromAlbumDetails
                        )
                    ) {
                        inclusive = true
                    }
                }
            },
            onAcceptClicked = {
                val navFromDest: Any
                val popUpToDest: Any
                Log.d(TAG, "logs: $wasFirstAlbumEverCreated and ${cameraViewModel.secondPhoto}")
                if (navigatedFromAlbumDetails) {
                    navFromDest = DetailsDestination(cameraViewModel.albumName)
                    popUpToDest = DetailsGraph
                } else if (wasFirstAlbumEverCreated || cameraViewModel.secondPhoto == true) {
                    navFromDest = SetupPhotoDestination(cameraViewModel.albumName)
                    popUpToDest = CameraGraph
                } else {
                    cameraViewModel.secondPhoto = true
                    Log.d(TAG, "logs: $wasFirstAlbumEverCreated and ${cameraViewModel.secondPhoto}")
                    navFromDest = CameraDestination(cameraViewModel.albumName)
                    popUpToDest =
                        CameraDestination(cameraViewModel.albumName, navigatedFromAlbumDetails)
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
        val configuration = LocalConfiguration.current
        val horizontalOrientation =
            when (configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    true
                }
                else -> {
                    false
                }
            }
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentScale = if (horizontalOrientation) ContentScale.FillHeight else ContentScale.FillWidth,
            contentDescription = null,
            modifier = Modifier
                .rotate(if (horizontalOrientation) -90f else 0f)
                .scale(if (horizontalOrientation) 2f else 1f)
                .fillMaxSize()
        )
        if (horizontalOrientation) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .safeContentPadding()
                    .align(Alignment.CenterEnd)
                    .padding(vertical = 16.dp, horizontal = 32.dp),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Buttons(onDiscardClicked, onAcceptClicked)
            }

        } else {
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
}

@Composable
@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp, orientation=landscape"
)
fun Preview() {
    LapseLabComposeTheme {
        Scaffold { it ->
            val dada = it
            PhotoPreviewScreen(
                createLabeledPlaceholderBitmap(),
                {},
                {}
            )
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
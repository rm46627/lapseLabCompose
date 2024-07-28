package com.example.lapselabcompose.ui.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.lapselabcompose.ui.details.DetailsDestination
import com.example.lapselabcompose.ui.CameraGraph
import com.example.lapselabcompose.ui.setup.SetupPhotoDestination
import kotlinx.serialization.Serializable

@Serializable
data class PhotoDestination(val navigatedFromAlbumDetails: Boolean = false)

@Composable
fun PhotoRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    navigatedFromAlbumDetails: Boolean = false
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(CameraGraph)
    }
    val viewModel: CameraViewModel = hiltViewModel(parentEntry)

    viewModel.bitmap?.let {
        PhotoScreen(
            bitmap = it,
            onDiscardClicked = {
                navController.navigateUp()
            },
            onAcceptClicked = {
                val originalDestination: Any = if (navigatedFromAlbumDetails)
                    DetailsDestination(viewModel.albumName)
                else
                    SetupPhotoDestination(viewModel.albumName)

                navController.navigate(originalDestination) {
                    popUpTo(originalDestination) {
                        inclusive = true
                    }
                }
            }
        )
    } ?: throw IllegalArgumentException()
}

@Composable
fun PhotoScreen(
    bitmap: Bitmap,
    onDiscardClicked: () -> Unit,
    onAcceptClicked: (context: Context) -> Unit,
) {
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = onDiscardClicked) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Discard image button"
                    )
                }
                val context = LocalContext.current
                IconButton(onClick = { onAcceptClicked(context) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept image button"
                    )
                }
            }
        }
    }
}
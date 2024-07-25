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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.files.appDir
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.ui.AlbumDetailsDestination
import com.example.lapselabcompose.ui.TakingPhotoGraph
import com.example.lapselabcompose.ui.setup.FirstPhotoDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
data class PhotoDestination(val navigatedFromAlbumDetails: Boolean = false)

@Composable
fun PhotoRoute(backStackEntry: NavBackStackEntry, navController: NavHostController, navigatedFromAlbumDetails: Boolean = false) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(TakingPhotoGraph)
    }
    val viewModel: TakingPhotoViewModel = hiltViewModel(parentEntry)
    val scope = rememberCoroutineScope()

    viewModel.bitmap?.let {
        PhotoScreen(
            bitmap = it,
            onDiscardClicked = {
                navController.navigateUp()
            },
            onAcceptClicked = { context ->
                scope.launch {
                    withContext(Dispatchers.Main) {
                        val originalDestination: Any = if (navigatedFromAlbumDetails)
                            AlbumDetailsDestination(viewModel.albumName)
                        else
                            FirstPhotoDestination(viewModel.albumName)

                        navController.navigate(originalDestination) {
                            popUpTo(originalDestination) {
                                inclusive = true
                            }
                        }

                        val name =
                            SimpleDateFormat(FILENAME, Locale.US).format(System.currentTimeMillis())
                        val (_, _) = MediaManagerFactory(context).saveBitmap(
                            bitmap = it,
                            subfolder = "$appDir/${viewModel.albumName!!}",
                            filename = name
                        )
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
    Box(modifier = Modifier.fillMaxSize()) {
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
                Icon(imageVector = Icons.Default.Check, contentDescription = "Accept image button")
            }
        }
    }
}


const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"

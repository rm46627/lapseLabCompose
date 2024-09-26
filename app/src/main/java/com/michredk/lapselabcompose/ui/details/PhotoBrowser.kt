package com.michredk.lapselabcompose.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselabcompose.R
import com.michredk.lapselabcompose.ui.DetailsGraph
import com.michredk.lapselabcompose.ui.PhotosGraph
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.lang.NullPointerException

@Serializable
data class PhotoBrowserDestination(val index: Int = 0)

@Composable
fun PhotoBrowserRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    index: Int
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(DetailsGraph)
    }
    if (parentEntry != null) {
        val viewModel: DetailsViewModel = hiltViewModel(parentEntry)
        val albumNameState by viewModel.albumName.collectAsStateWithLifecycle()
        val photosState by viewModel.photos.collectAsStateWithLifecycle()
        val albumName = albumNameState ?: throw NullPointerException()

        photosState?.let { photos ->
            PhotoBrowserScreen(
                onNextButtonClicked = {
                    navController.navigate(PhotoBrowserDestination(index + 1)) {
                        popUpTo(PhotosGraph) {
                            inclusive = true
                        }
                    }
                },
                onPreviousButtonClicked = {
                    navController.navigate(PhotoBrowserDestination(index - 1)) {
                        popUpTo(PhotosGraph) {
                            inclusive = true
                        }
                    }
                },
                onDeleteButtonClicked = {
                    scope.launch {
                        MediaManagerFactory(context).deletePhoto(photos[index].absolutePath)
                        val updatedPhotos = MediaManagerFactory(context).getPhotoFiles(albumName)
                        viewModel.setPhotos(updatedPhotos)

                        if (photos.size > 1) {
                            val destIndex = if (index == 0) 0 else index - 1
                            navController.navigate(PhotoBrowserDestination(destIndex)) {
                                popUpTo(PhotosGraph) {
                                    inclusive = true
                                }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                },
                photoPath = photos[index].absolutePath,
                date = "01.01.2024 12:00",
                index,
                index == photos.size - 1
            )
        }
    } else {
        navController.popBackStack()
    }
}

@Composable
fun PhotoBrowserScreen(
    onNextButtonClicked: () -> Unit,
    onPreviousButtonClicked: () -> Unit,
    onDeleteButtonClicked: () -> Unit,
    photoPath: String,
    date: String,
    index: Int,
    isLastPhoto: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoPath)
                .crossfade(1000)
                .build(),
            contentDescription = "Gallery photo",
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter)
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            Text(text = date)
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val prevEnabled = index != 0
                val btnBackgroundColor =
                    MaterialTheme.colorScheme.primaryContainer
                IconButton(
                    modifier = Modifier.background(
                        if (prevEnabled) btnBackgroundColor else btnBackgroundColor.copy(
                            alpha = 0.5f
                        ), shape = CircleShape
                    ),
                    onClick = onPreviousButtonClicked
                ) {
                    Icon(imageVector = Icons.Default.ArrowBackIos, contentDescription = "")
                }
                IconButton(onClick = onDeleteButtonClicked) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "")
                }
                IconButton(
                    modifier = Modifier.background(
                        if (!isLastPhoto) btnBackgroundColor else btnBackgroundColor.copy(
                            alpha = 0.5f
                        ), shape = CircleShape
                    ),
                    onClick = onNextButtonClicked
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = ""
                    )
                }

            }
        }
    }

}


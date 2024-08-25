package com.example.lapselabcompose.ui.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.ui.DetailsGraph
import com.example.lapselabcompose.ui.PhotosGraph
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

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
    val viewModel: DetailsViewModel = hiltViewModel(parentEntry)
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    photos?.let {
        PhotoBrowserScreen(
            onNextButtonClicked = {
                navController.navigate(PhotoBrowserDestination(index + 1)) {
                    popUpTo(PhotosGraph){
                        inclusive = true
                    }
                }
            },
            onPreviousButtonClicked = {
                navController.navigate(PhotoBrowserDestination(index - 1)) {
                    popUpTo(PhotosGraph){
                        inclusive = true
                    }
                }
            },
            onDeleteButtonClicked = {
                scope.launch {
                    MediaManagerFactory(context).deletePhoto(photos!![index].absolutePath)
                    navController.navigate(PhotoBrowserDestination(index - 1)) {
                        popUpTo(PhotosGraph){
                            inclusive = true
                        }
                    }
                }
            },
            photoPath = photos!![index].absolutePath,
            date = "01.01.2024 12:00",
            index,
            index == photos!!.size - 1
        )
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
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                Text(text = date)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (index != 0) {
                        IconButton(onClick = onPreviousButtonClicked) {
                            Icon(imageVector = Icons.Default.ArrowBackIos, contentDescription = "")
                        }
                    }
                    IconButton(onClick = onDeleteButtonClicked) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "")
                    }
                    if (!isLastPhoto) {
                        IconButton(onClick = onNextButtonClicked) {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = ""
                            )
                        }
                    }
                }
            }
        }
    }
}


package com.example.lapselabcompose.ui.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.database.Album
import com.example.files.appMoviesDir
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.ui.DetailsGraph
import com.example.lapselabcompose.ui.camera.CameraDestination
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
import com.example.video.LapseCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class DetailsDestination(val albumName: String? = null)

@Composable
fun DetailsRoute(
    backStackEntry: NavBackStackEntry, navController: NavHostController, albumName: String?
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(DetailsGraph)
    }
    val detailsViewModel: DetailsViewModel = hiltViewModel(parentEntry)
    detailsViewModel.setAlbumName(albumName ?: throw IllegalArgumentException())

    val album by detailsViewModel.album.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var mPhotos by remember { mutableStateOf<List<File>?>(null) }

    LaunchedEffect(album) {
        mPhotos = album?.let { MediaManagerFactory(context).getPhotoFiles(albumName) }
    }

    val scope = rememberCoroutineScope()

    mPhotos?.let { photos ->
        DetailsScreen(
            album ?: throw IllegalArgumentException(),
            photos,
            onAddPhotoClicked = {
                navController.navigate(CameraDestination(albumName, true))
            },
            onEditVideoClicked = {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        val lab = LapseCreator(context, album!!)
                        val filename = lab.createVideo(photos)
                        MediaManagerFactory(context).saveVideo(filename, "$appMoviesDir/${album!!.directoryName}")
                    }
                }
//                navController.navigate(LabDestination(albumName))
            }
        )
    } ?: Loading()
}

@Composable
fun DetailsScreen(
    album: Album,
    photos: List<File>,
    onAddPhotoClicked: () -> Unit,
    onEditVideoClicked: () -> Unit
) {
    Scaffold {
        Column(modifier = Modifier.padding(it)) {
            val gridState = rememberLazyGridState()

            val expandedState by remember {
                derivedStateOf {
                    if (!gridState.canScrollBackward)
                        true
                    else
                        gridState.lastScrolledBackward && gridState.firstVisibleItemIndex == 0
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                DetailsHeader(expandedState, album, onAddPhotoClicked, onEditVideoClicked)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), contentPadding = PaddingValues(
                        start = 12.dp, top = 16.dp, end = 12.dp, bottom = 16.dp
                    ), state = gridState
                ) {
                    itemsIndexed(items = photos, key = { index, _ ->
                        index
                    }) { _, photo ->
                        GridPhotoItem(photo.absolutePath)
                    }
                }
            }
        }
    }
}

@Composable
fun Loading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 28.dp),
            strokeWidth = 5.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
fun PreviewAlbumDetails() {
    LapseLabComposeTheme {
        DetailsScreen(
            album = Album(
                id = 1,
                directoryName = "Album z różami",
                coverPhotoPath = placeholderUrls[0].absolutePath
            ),
            photos = placeholderUrls,
            {}, {}
        )
    }
}

val placeholderUrls =
    List<File>(10) { index -> File("https://via.placeholder.com/150?text=Image+${index + 1})") }
package com.example.lapselabcompose.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.database.Album
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.ui.DetailsGraph
import com.example.lapselabcompose.ui.camera.CameraDestination
import com.example.lapselabcompose.ui.camera.PhotoPreviewDestination
import com.example.lapselabcompose.ui.lab.LabDestination
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
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
    LaunchedEffect(album) {
        val photos = album?.let {
            MediaManagerFactory(context).getPhotoFiles(it.directoryName)
        }
        photos?.let { detailsViewModel.setPhotos(it) }
    }
    val photos by detailsViewModel.photos.collectAsStateWithLifecycle()

    photos?.let {
        DetailsScreen(
            album ?: throw IllegalArgumentException(),
            it,
            onAddPhotoClicked = {
                navController.navigate(CameraDestination(albumName, true))
            },
            onEditVideoClicked = {
                navController.navigate(LabDestination(albumName))
            },
            onPhotoClicked = { index ->
                navController.navigate(PhotoBrowserDestination(index))
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
    onPhotoClicked: (Int) -> Unit
) {
    Scaffold {
        Column(modifier = Modifier
            .padding(it)
            .fillMaxSize()) {
            val gridState = rememberLazyGridState()

            val expandedState by remember {
                derivedStateOf {
                    if(photos.size < 7) true
                    else if (!gridState.canScrollBackward)
                        true
                    else
                        gridState.lastScrolledBackward && gridState.firstVisibleItemIndex == 0
                }
            }

            DetailsHeader(expandedState, album, onAddPhotoClicked, onEditVideoClicked)

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
            {}, {}, {}
        )
    }
}

val placeholderUrls =
    List<File>(10) { index -> File("https://via.placeholder.com/150?text=Image+${index + 1})") }
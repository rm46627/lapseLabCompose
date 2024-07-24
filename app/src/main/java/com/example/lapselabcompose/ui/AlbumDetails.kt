package com.example.lapselabcompose.ui

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.database.Album
import com.example.database.Repository
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.TAG
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File
import javax.inject.Inject

@Serializable
data class AlbumDetailsDestination(val id: Int = 0)

@Composable
fun AlbumDetailsRoute(
    backStackEntry: NavBackStackEntry, navController: NavHostController, id: Int
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(AlbumDetailsGraph)
    }
    val albumDetailsViewModel: AlbumDetailsViewModel = hiltViewModel(parentEntry)
    albumDetailsViewModel.setAlbumId(id)

    val album by albumDetailsViewModel.album.collectAsStateWithLifecycle()
    var loadingState by remember { mutableStateOf(true) }

    val context = LocalContext.current
    var photos by remember { mutableStateOf<List<File>?>(null) }

    LaunchedEffect(album) {
            photos = album?.let { MediaManagerFactory(context).getPhotoFiles(it.directoryName) }
    }
    photos?.let { AlbumDetails(album!!, it) } ?: Loading()

}

@Composable
fun AlbumDetails(album: Album, photos: List<File>) {
    Scaffold {
        Column(modifier = Modifier.padding(it)) {

            val gridState = rememberLazyGridState()
            val expandedState by remember {
                derivedStateOf {
                    if (photos.size < 6) true
                    else
                        when (gridState.firstVisibleItemIndex) {
                            0 -> gridState.firstVisibleItemScrollOffset <= 100
                            else -> false
                        }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                BannerSectionExpand(expandedState, album)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), contentPadding = PaddingValues(
                        start = 12.dp, top = 16.dp, end = 12.dp, bottom = 16.dp
                    ), state = gridState
                ) {
                    itemsIndexed(items = photos, key = { index, _ ->
                        index
                    }) { _, photo ->
                        PhotoItem(photo.absolutePath)
                    }
                }
            }
        }
    }
}

@Composable
fun BannerSectionExpand(expanded: Boolean, album: Album) {
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f, animationSpec = tween(durationMillis = 1000)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp * scale + 100.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp))
            .padding(16.dp)
            .alpha(scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(album.coverPhotoPath),
            contentDescription = "Album cover photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(200.dp * scale)
                .height(200.dp * scale)
                .padding(8.dp)
        )
        Text(
            text = album.directoryName,
            modifier = Modifier
                .wrapContentHeight(),
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

@Composable
fun PhotoItem(photo: String) {
    Image(
        painter = rememberAsyncImagePainter(photo),
        contentDescription = "Photo",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
    )
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

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val _albumId = MutableStateFlow<Int?>(null)
    val albumId: StateFlow<Int?> = _albumId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _album = _albumId.flatMapLatest { albumId ->
        if (albumId != null) {
            repository.getAlbum(albumId)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val album = _album

    fun setAlbumId(id: Int) {
        _albumId.value = id
    }

    fun updateAlbum(album: Album) {
        viewModelScope.launch {
            repository.updateAlbum(album)
        }
    }
}

@Preview
@Composable
fun PreviewAlbumDetails() {
    LapseLabComposeTheme {

    }
}
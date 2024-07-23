package com.example.lapselabcompose.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.scale
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
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    id: Int
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
        if (album != null) {
            photos = MediaManagerFactory(context).getPhotoFiles(album!!.directoryName)
            loadingState = false
        }
    }
    if (loadingState) {
        Loading()
    } else {
        photos?.let { AlbumDetails(it) }
    }

}

@Composable
fun AlbumDetails(photos: List<File>) {
    Scaffold {
        Column(modifier = Modifier.padding(it)) {
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(3),
//                contentPadding = PaddingValues(
//                    start = 12.dp,
//                    top = 16.dp,
//                    end = 12.dp,
//                    bottom = 16.dp
//                )
//            ) {
//                itemsIndexed(items = photos, key = { index, photo ->
//                    photo.
//                }) { index, album ->
//                        PhotoItem(photoFile)
//                    }
//            }
            AnimatedScrollScreen()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedScrollScreen() {
    val listState = rememberLazyListState()

    val expandedState by remember {
        derivedStateOf {
            when (listState.firstVisibleItemIndex) {
                0 -> listState.firstVisibleItemScrollOffset <= 100
                else -> false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState) {
            stickyHeader {
                BannerSectionExpand(expandedState)
            }
            items(photos) { photo ->
                PhotoItem(photo)
            }
        }
    }
}

@Composable
fun BannerSectionExpand(expanded: Boolean) {
    val scale by animateFloatAsState(targetValue = if(expanded) 1f else 0f, animationSpec = tween(durationMillis = 1000))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp * scale + 100.dp)
            .background(Color.Blue, RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp))
            .padding(16.dp)
            .alpha(scale)
    ) {
        Image(
            painter = rememberAsyncImagePainter("https://via.placeholder.com/300"),
            contentDescription = "Header Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(300.dp)
        )
        Text(
            text = "Header Text",
            modifier = Modifier
                .background(Color.Cyan)
                .wrapContentHeight(),
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

@Composable
fun BannerSection(progress: Float) {

    val photoAlpha = progress
    val photoScale = progress
    val textScale = progress * 0.3f + 0.7f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Blue, RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp))
            .padding(16.dp),
    ) {
        Image(
            painter = rememberAsyncImagePainter("https://via.placeholder.com/300"),
            contentDescription = "Header Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(200.dp * photoScale)
                .height(200.dp * photoScale)
//                .scale(photoScale)
                .alpha(photoAlpha)
        )
        Text(
            text = "Header Text",
            modifier = Modifier
                .background(Color.Cyan)
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

val photos = listOf(
    "https://via.placeholder.com/300",
    "https://via.placeholder.com/301",
    "https://via.placeholder.com/302",
    "https://via.placeholder.com/303",
    "https://via.placeholder.com/304",
    "https://via.placeholder.com/305",
    "https://via.placeholder.com/306"
)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosCarousel(photos: List<File>) {
    val carouselState = rememberCarouselState { photos.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = 300.dp,
        itemSpacing = 10.dp
    ) { page ->
        Box(modifier = Modifier.size(300.dp)) {
            val painter = rememberAsyncImagePainter(photos[page].absolutePath)
            Image(
                painter = painter,
                contentDescription = "Carousel photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
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
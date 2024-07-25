package com.example.lapselabcompose.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
import com.example.lapselabcompose.ui.camera.CameraDestination
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
import kotlin.math.abs

@Serializable
data class AlbumDetailsDestination(val albumName: String? = null)

@Composable
fun AlbumDetailsRoute(
    backStackEntry: NavBackStackEntry, navController: NavHostController, albumName: String?
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(AlbumDetailsGraph)
    }
    val albumDetailsViewModel: AlbumDetailsViewModel = hiltViewModel(parentEntry)
    albumDetailsViewModel.setAlbumName(albumName ?: throw IllegalArgumentException())

    val mAlbum by albumDetailsViewModel.album.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var mPhotos by remember { mutableStateOf<List<File>?>(null) }

    LaunchedEffect(mAlbum) {
        mPhotos = mAlbum?.let { MediaManagerFactory(context).getPhotoFiles(albumName) }
    }

    mPhotos?.let { photos ->
        AlbumDetails(
            mAlbum!!,
            photos,
            onAddPhotoClicked = {
                navController.navigate(CameraDestination(albumName, true))
            }
        )
    } ?: Loading()
}

@Composable
fun AlbumDetails(album: Album, photos: List<File>, onAddPhotoClicked: () -> Unit) {
    Scaffold {
        Column(modifier = Modifier.padding(it)) {
            val gridState = rememberLazyGridState()

            val expandedState  by remember {
                derivedStateOf {
                    if(!gridState.canScrollBackward)
                        true
                    else
                        gridState.lastScrolledBackward && gridState.firstVisibleItemIndex == 0
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                HeaderSection(expandedState, album, onAddPhotoClicked)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), contentPadding = PaddingValues(
                        start = 12.dp, top = 16.dp, end = 12.dp, bottom = 16.dp
                    ), state = gridState
                ) {
                    itemsIndexed(items = photos, key = { index, _ ->
                        index
                    }) { _, photo ->
                        ImageGrid(photo.absolutePath)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(expanded: Boolean, album: Album, onAddPhotoClicked: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f, animationSpec = tween(durationMillis = 1000)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp * scale + 100.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)
            )
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
            style = MaterialTheme.typography.headlineLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), verticalAlignment = Alignment.Bottom
        ) {
            Text(
                modifier = Modifier.weight(3f),
                text = album.directoryName,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.weight(2f))
            IconButton(onClick = onAddPhotoClicked) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Add photo button"
                )
            }
        }
    }
}

@Composable
fun ImageGrid(photo: String) {
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

suspend fun PointerInputScope.detectPinchGestures(
    pass: PointerEventPass = PointerEventPass.Main,
    onGestureStart: (PointerInputChange) -> Unit = {},
    onGesture: (
        centroid: Offset,
        zoom: Float
    ) -> Unit,
    onGestureEnd: (PointerInputChange) -> Unit = {}
) {
    awaitEachGesture {
        var zoom = 1f
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        val down: PointerInputChange = awaitFirstDown(requireUnconsumed = false, pass = pass)
        onGestureStart(down)
        var pointer = down
        var pointerId = down.id
        do {
            val event = awaitPointerEvent(pass = pass)
            val canceled = event.changes.any { it.isConsumed }
            if (!canceled) {
                val pointerInputChange = event.changes.firstOrNull { it.id == pointerId } ?: event.changes.first()
                pointerId = pointerInputChange.id
                pointer = pointerInputChange
                val zoomChange = event.calculateZoom()
                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    if (zoomMotion > touchSlop) {
                        pastTouchSlop = true
                    }
                }
                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (zoomChange != 1f) {
                        onGesture(
                            centroid,
                            zoomChange
                        )
                        event.changes.forEach { it.consume() }
                    }
                }
            }
        } while (!canceled && event.changes.any { it.pressed })
        onGestureEnd(pointer)
    }
}

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val _albumName = MutableStateFlow<String?>(null)
    val albumName: StateFlow<String?> = _albumName.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _album = _albumName.flatMapLatest { albumId ->
        if (albumId != null) {
            repository.getAlbum(albumId)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val album = _album

    fun setAlbumName(name: String) {
        _albumName.value = name
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
        AlbumDetails(
            album = Album(
                id = 1,
                directoryName = "Album z różami",
                coverPhotoPath = placeholderUrls[0].absolutePath
            ),
            photos = placeholderUrls,
            {}
        )
    }
}

val placeholderUrls =
    List<File>(10) { index -> File("https://via.placeholder.com/150?text=Image+${index + 1})") }
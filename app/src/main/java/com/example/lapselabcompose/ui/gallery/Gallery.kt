package com.example.lapselabcompose.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.database.Album
import com.example.database.Repository
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.R
import com.example.lapselabcompose.ui.details.DetailsDestination
import com.example.lapselabcompose.ui.setup.SetupAlbumDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

// TODO: tip modal about longpress on gallery item for context menu
// TODO: Display graphic encouraging to create a new album - display as small cell when
//  gallerySize is odd and large cell when gallerySize % 2 == 0

// TODO: animate grid after deleting the album

// TODO: taken photos counter
// TODO: daily photos streak counter
// TODO: different frames for better streak and stars for photos counter

// TODO: new ideas for albums:
//  plants,
//  kids growing up,
//  gym progress,
//  time lapse movie with clay set

@Serializable
object GalleryDestination

class AlbumMenuItem(
    val id: String,
    val text: String,
    val icon: ImageVector,
)

@Composable
fun GalleryRoute(navController: NavHostController) {
    val galleryViewModel: GalleryViewModel = hiltViewModel()
    val albums by galleryViewModel.getAlbums.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    val mediaManager = MediaManagerFactory(context)
    val coroutineScope = rememberCoroutineScope()

    GalleryScreen(albums, onAlbumClick = { name ->
        navController.navigate(DetailsDestination(name))
    }, onCreateClick = {
        navController.navigate(SetupAlbumDestination)
    }, dropDownItems = listOf(
        AlbumMenuItem(id = "delete", text = "Delete album", Icons.Default.DeleteForever)
    ), onMenuItemClicked = { id, albumName ->
        when (id) {
            "delete" -> {
                // TODO: add modal
                galleryViewModel.deleteAlbum(albumName)
                coroutineScope.launch {
                    mediaManager.deleteAlbum(albumName)
                }
            }
        }
    })
}

@Composable
fun GalleryScreen(
    albums: List<Album>,
    onAlbumClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    dropDownItems: List<AlbumMenuItem>,
    onMenuItemClicked: (String, String) -> Unit
) {
    // adds creating new album card
    val albumsWithExtras = albums.plus(Album())
    Scaffold {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), contentPadding = PaddingValues(
                    start = 12.dp, top = 16.dp, end = 12.dp, bottom = 16.dp
                )
            ) {
                itemsIndexed(items = albumsWithExtras, key = { index, album ->
                    album.id
                }) { index, album ->
                    if (index == albums.size) {
                        CreateCard(onCreateClick)
                    } else {
                        GalleryItem(
                            album = album,
                            onAlbumClick,
                            dropDownItems = dropDownItems,
                            onMenuItemClicked = onMenuItemClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryItem(
    album: Album,
    onGalleryItemClick: (String) -> Unit,
    dropDownItems: List<AlbumMenuItem>,
    onMenuItemClicked: (String, String) -> Unit
) {
    var isContextMenuVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var pressOffset by remember {
        mutableStateOf(DpOffset.Zero)
    }
    var itemHeight by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current
    val interactionSource = remember{
        MutableInteractionSource()
    }

    Card(modifier = Modifier
        .wrapContentSize()
        .padding(8.dp)
        .onSizeChanged { itemHeight = with(density) { it.height.toDp() } }
        .indication(interactionSource, LocalIndication.current)
        .pointerInput(true) {
            detectTapGestures(
                onTap = { onGalleryItemClick(album.directoryName) },
                onLongPress = {
                isContextMenuVisible = true
                pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
            },
                onPress = {
                    val press = PressInteraction.Press(it)
                    interactionSource.emit(press)
                    tryAwaitRelease()
                    interactionSource.emit(PressInteraction.Release(press))
                })
        }, elevation = CardDefaults.cardElevation(
        defaultElevation = 8.dp
    ), shape = ShapeDefaults.Medium
    ) {
        Column {
            ItemCoverPhoto(album.coverPhotoPath)
            Text(text = album.directoryName, textAlign = TextAlign.Center)
        }
        DropdownMenu(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer),
            expanded = isContextMenuVisible,
            offset = pressOffset.copy(y = pressOffset.y - itemHeight),
            onDismissRequest = { isContextMenuVisible = false }) {
            dropDownItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = "") },
                    leadingIcon = { Icon(imageVector = item.icon, contentDescription = item.text) },
                    onClick = {
                        onMenuItemClicked(item.id, album.directoryName)
                        isContextMenuVisible = false
                    },

                    )
            }
        }
    }
}

@Composable
fun ItemCoverPhoto(photo: String) {
    AsyncImage(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        model = ImageRequest.Builder(LocalContext.current).data(photo).crossfade(1000)
            .transformations().build(),
        contentDescription = "Album cover photo",
        contentScale = ContentScale.Crop
    )
}

@Composable
fun CreateCard(onCreateNewAlbumClick: () -> Unit) {
    Card(
        modifier = Modifier.wrapContentSize(),
        shape = ShapeDefaults.Medium,
        onClick = onCreateNewAlbumClick
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Create new album icon",
                modifier = Modifier
                    .fillMaxWidth()
                    .size(200.dp)
            )
            Text(
                text = "Create new album!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@HiltViewModel
class GalleryViewModel @Inject constructor(
    val repository: Repository
) : ViewModel() {
    val getAlbums = repository.getAlbums()

    // used to calculate span size in gallery recyclerView
    var gallerySize: Int = 0

    fun deleteAlbum(albumName: String) {
        viewModelScope.launch {
            repository.deleteAlbum(albumName)
        }
    }

}
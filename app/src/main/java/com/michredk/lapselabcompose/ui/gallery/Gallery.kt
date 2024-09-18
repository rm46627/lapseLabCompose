package com.michredk.lapselabcompose.ui.gallery

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.michredk.database.Album
import com.michredk.database.Repository
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselabcompose.R
import com.michredk.lapselabcompose.services.alarm.AlarmScheduler
import com.michredk.lapselabcompose.ui.details.DetailsDestination
import com.michredk.lapselabcompose.ui.setup.SetupAlbumDestination
import com.michredk.lapselabcompose.ui.theme.LapseLabComposeTheme
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
    val albums by galleryViewModel.getAlbums.collectAsStateWithLifecycle(
        initialValue = listOf(
            Album(
                id = Int.MIN_VALUE
            )
        )
    )
    val context = LocalContext.current
    val mediaManager = MediaManagerFactory(context)
    val coroutineScope = rememberCoroutineScope()

    var albumToRemove by remember {
        mutableStateOf<String?>(null)
    }

    if (albums.isEmpty() || albums[0].id != Int.MIN_VALUE) {
        GalleryScreen(albums, onAlbumClick = { name ->
            navController.navigate(DetailsDestination(name))
        }, onCreateClick = {
            navController.navigate(SetupAlbumDestination)
        }, dropDownItems = listOf(
            AlbumMenuItem(id = "delete", text = "Delete album", Icons.Default.DeleteForever)
        ), onMenuItemClicked = { id, albumName ->
            when (id) {
                "delete" -> {
                    albumToRemove = albumName
                }
                "move up" -> {
                    val firstId = albums.get(0).id
                    galleryViewModel
                }
            }
        })
        if (albumToRemove != null) {
            RemoveAlbumDialog {
                galleryViewModel.deleteAlbum(albumToRemove!!)
                coroutineScope.launch {
                    mediaManager.deleteAlbum(albumToRemove!!)
                    AlarmScheduler(context).cancel(albumToRemove!!)
                }
            }
        }
    }
    else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            CircularProgressIndicator(modifier = Modifier.size(400.dp))
        }
    }
}

@Composable
fun GalleryScreen(
    albums: List<Album>,
    onAlbumClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    dropDownItems: List<AlbumMenuItem>,
    onMenuItemClicked: (String, String) -> Unit
) {
    // TODO: Add pager view mode
    // https://www.youtube.com/watch?v=V2Ke-JJDnrU&list=PLWz5rJ2EKKc9tgU26tbUAy01MzC2Yjztb&index=4
    val pagerViewMode by remember {
        mutableStateOf(false)
    }

    // adds creating new album card
    val albumsWithExtras = albums.plus(Album())
    val backgroundColor = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, top = 32.dp)
            .background(backgroundColor)
    ) {
        Text(
            style = MaterialTheme.typography.titleLarge,
            text = "My albums"
        )

        LazyVerticalStaggeredGrid(
            modifier = Modifier.padding(top = 38.dp),
            columns = StaggeredGridCells.Fixed(2), contentPadding = PaddingValues(
                start = 0.dp, top = 8.dp, end = 8.dp, bottom = 8.dp
            ),
            verticalItemSpacing = 8.dp
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
    val interactionSource = remember {
        MutableInteractionSource()
    }

    Column(horizontalAlignment = Alignment.Start) {
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
            ItemCoverPhoto(album.coverPhotoPath)
        }
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = album.directoryName,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            style = TextStyle(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(start = 8.dp),
            text = album.photoCount.toString(),
            textAlign = TextAlign.Start
        )
        DropdownMenu(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer),
            expanded = isContextMenuVisible,
            offset = pressOffset.copy(y = pressOffset.y - itemHeight),
            onDismissRequest = { isContextMenuVisible = false }) {
            dropDownItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = "Remove album") },
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
            .height(200.dp),
        model = ImageRequest.Builder(LocalContext.current).data(photo).crossfade(1000)
            .transformations().build(),
        contentDescription = "Album cover photo",
        contentScale = ContentScale.Crop
    )
}

@Composable
fun CreateCard(onCreateNewAlbumClick: () -> Unit) {
    Column {
        Card(
            modifier = Modifier
                .wrapContentSize()
                .padding(8.dp),
            shape = ShapeDefaults.Medium,
            onClick = onCreateNewAlbumClick
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Create new album icon",
                modifier = Modifier
                    .fillMaxWidth()
                    .size(200.dp)
            )
        }
        Text(
            text = "Create new album!",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@HiltViewModel
class GalleryViewModel @Inject constructor(
    val repository: Repository
) : ViewModel() {

    val getAlbums = repository.getAlbums()

    fun deleteAlbum(albumName: String) {
        viewModelScope.launch {
            repository.deleteAlbum(albumName)
        }
    }

    fun updateAlbum(album: Album) {
        viewModelScope.launch {
            repository.updateAlbum(album)
        }
    }

}

@Composable
fun RemoveAlbumDialog(removeAlbum: () -> Unit) {
    var viewDialog by remember {
        mutableStateOf(true)
    }
    if (viewDialog) {
        AlertDialog(
            title = { Text(text = "Remove album") },
            text = { Text(text = "Do you really want to do this?") },
            onDismissRequest = { viewDialog = false },
            confirmButton = {
                Row {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Yes",
                        modifier = Modifier.clickable {
                            removeAlbum()
                            viewDialog = false
                        })
                }
            },
            dismissButton = {
                Text(
                    text = "No",
                    modifier = Modifier.clickable { viewDialog = false })
            }
        )
    }
}

@Preview
@Composable
fun preciewGallery() {
    LapseLabComposeTheme {

    }
}
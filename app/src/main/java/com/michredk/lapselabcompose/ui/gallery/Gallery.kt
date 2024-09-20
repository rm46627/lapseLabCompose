package com.michredk.lapselabcompose.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.michredk.database.Album
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselabcompose.services.alarm.AlarmScheduler
import com.michredk.lapselabcompose.ui.common.TipDialog
import com.michredk.lapselabcompose.ui.details.DetailsDestination
import com.michredk.lapselabcompose.ui.setup.SetupAlbumDestination
import com.michredk.lapselabcompose.ui.theme.LapseLabComposeTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// TODO: tip modal about long press on gallery item for context menu
// TODO: Display graphic encouraging to create a new album - display as small cell when
//  gallerySize is odd and large cell when gallerySize % 2 == 0

// TODO: animate grid after deleting the album
// TODO: fix bug with list not updating changed albums order

// TODO: daily photos streak counter
// TODO: different frames for better streak and stars for photos counter

// TODO: new ideas for albums:
//  plants,
//  kids growing up,
//  gym progress,
//  time lapse movie with clay set

@Serializable
object GalleryDestination

class GalleryMenuItem(
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

    val isContextMenuTipCompleted by galleryViewModel.isContextMenuTipCompleted.collectAsStateWithLifecycle(
        initialValue = true
    )

    val context = LocalContext.current
    val mediaManager = MediaManagerFactory(context)
    val coroutineScope = rememberCoroutineScope()

    var albumToRemove by remember {
        mutableStateOf<String?>(null)
    }

    if (albums.isEmpty() || albums[0].id != Int.MIN_VALUE) {
        if (albums.isNotEmpty()) {
            TipDialog(
                title = "Context menu",
                text = "Use long press on album card to view context menu.",
                shouldViewTip = !isContextMenuTipCompleted,
                saveTipViewed = {
                    galleryViewModel.updateContextMenuTipValue(isCompleted = true)
                }
            )
        }

        GalleryScreen(
            albums = albums,
            onAlbumClick = { name ->
                navController.navigate(DetailsDestination(name))
            },
            onCreateClick = {
                navController.navigate(SetupAlbumDestination)
            },
            albumDropDownItems = listOf(
                GalleryMenuItem(id = "move up", text = "Move album up", Icons.Default.ArrowUpward),
                GalleryMenuItem(id = "delete", text = "Delete album", Icons.Default.DeleteForever)
            ),
            onAlbumMenuItemClicked = { id, albumName ->
                when (id) {
                    "delete" -> {
                        albumToRemove = albumName
                    }

                    "move up" -> {
                        val orderNum = albums[0].order
                        val album = albums.find { it.directoryName == albumName }
                        if (album != null)
                            galleryViewModel.updateAlbum(
                                album.copy(order = orderNum - 1)
                            )
                    }
                }
            },
            galleryDropDownItems = listOf(
                GalleryMenuItem(id = "reset tips", "Reset Tips", icon = Icons.Default.Cached)
            ),
            onGalleryMenuItemClicked = { id ->
                when (id) {
                    "reset tips" -> {
                        galleryViewModel.resetAllTipsValues()
                        // TODO: view Snackbar
                    }
                }
            })
        if (albumToRemove != null) {
            RemoveAlbumDialog {
                galleryViewModel.deleteAlbum(albumToRemove!!)
                coroutineScope.launch {
                    mediaManager.deleteAlbum(albumToRemove!!)
                    AlarmScheduler(context).cancel(albumToRemove!!)
                    albumToRemove = null
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(400.dp))
        }
    }
}

@Composable
fun GalleryScreen(
    albums: List<Album>,
    onAlbumClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    albumDropDownItems: List<GalleryMenuItem>,
    onAlbumMenuItemClicked: (String, String) -> Unit,
    galleryDropDownItems: List<GalleryMenuItem>,
    onGalleryMenuItemClicked: (String) -> Unit
) {
    // TODO: Add pager view mode
    // https://www.youtube.com/watch?v=V2Ke-JJDnrU&list=PLWz5rJ2EKKc9tgU26tbUAy01MzC2Yjztb&index=4
    val pagerViewMode by remember {
        mutableStateOf(false)
    }
    var isMenuVisible by rememberSaveable {
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                text = "My albums"
            )
            Box {
                IconButton(onClick = { isMenuVisible = true }) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                }
                DropdownMenu(
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer),
                    expanded = isMenuVisible,
//                    offset = pressOffset.copy(y = pressOffset.y - itemHeight),
                    onDismissRequest = { isMenuVisible = false }) {
                    galleryDropDownItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item.text) },
                            leadingIcon = { Icon(imageVector = item.icon, contentDescription = item.text) },
                            onClick = {
                                onGalleryMenuItemClicked(item.id)
                                isMenuVisible = false
                            },

                            )
                    }
                }

            }

        }

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
                        dropDownItems = albumDropDownItems,
                        onMenuItemClicked = onAlbumMenuItemClicked
                    )
                }
            }
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
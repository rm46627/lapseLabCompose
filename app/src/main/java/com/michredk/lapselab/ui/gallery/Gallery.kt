package com.michredk.lapselab.ui.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Pages
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.michredk.database.Album
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselab.R
import com.michredk.lapselab.TAG
import com.michredk.lapselab.services.alarm.AlarmScheduler
import com.michredk.lapselab.ui.common.GifImage
import com.michredk.lapselab.ui.common.TipDialog
import com.michredk.lapselab.ui.details.DetailsDestination
import com.michredk.lapselab.ui.setup.SetupAlbumDestination
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

// TODO: animate grid after deleting the album
// TODO: fix bug with list not updating changed albums order

// TODO: different frames for better streak and stars for photos counter

// TODO: add daily streak counter
// TODO: add click animation for photos in cards in pager

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
    
    val isFilemanagerTipCompleted by galleryViewModel.isFilemanagerTipCompleted.collectAsStateWithLifecycle(
        initialValue = true
    )

    val isPagerViewModeOn by galleryViewModel.isPagerViewModeOn.collectAsStateWithLifecycle(
        initialValue = true
    )

    val context = LocalContext.current
    val mediaManager = MediaManagerFactory(context)
    val coroutineScope = rememberCoroutineScope()

    var albumToRemove by remember {
        mutableStateOf<String?>(null)
    }
    var viewFilemanagerTipDialog by remember {
        mutableStateOf(false)
    }
    val galleryDropDownItems = mutableListOf(
        GalleryMenuItem(
            id = "switch view mode",
            if (isPagerViewModeOn) stringResource(R.string.switch_to_grid) else stringResource(R.string.switch_to_pager),
            icon = if (isPagerViewModeOn) Icons.Default.GridView else Icons.Default.Pages
        ),
        GalleryMenuItem(id = "reset tips",
            stringResource(R.string.reset_tips), icon = Icons.Default.Cached),
    )
    if (albums.isEmpty() || albums[0].id != Int.MIN_VALUE) {
        if (albums.isNotEmpty()) {
            TipDialog(
                title = stringResource(R.string.context_menu),
                content = { Column {
                    Text(text = stringResource(R.string.use_long_press_on_album_card_to_view_context_menu))
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Icon(modifier = Modifier.padding(end = 4.dp), imageVector = Icons.Default.ArrowUpward, contentDescription = "camera switch explanation")
                        Text(text = stringResource(R.string.move_album_up_on_the_list))
                    }
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Icon(modifier = Modifier.padding(end = 4.dp),imageVector = Icons.Default.DeleteForever, contentDescription = "upload button explanation")
                        Text(text = stringResource(R.string.delete_your_album_forever))
                    }
                } },
                viewTipDialog = !isContextMenuTipCompleted,
                saveTipViewed = {
                    galleryViewModel.updateContextMenuTipValue(isCompleted = true)
                }
            )

            galleryDropDownItems.add(GalleryMenuItem(id = "file manager",
                stringResource(R.string.view_files), icon = Icons.Default.Folder))
            TipDialog(
                title = stringResource(R.string.find_your_files),
                content = { Column {
                    Text(text = stringResource(R.string.use_images_and_videos_folders_to_check_your_files))
                    GifImage(data = R.drawable.filemanager, modifier = Modifier
                        .padding(top = 16.dp)
                        .height(250.dp)
                        .fillMaxWidth())
                } },
                viewTipDialog = !isFilemanagerTipCompleted && viewFilemanagerTipDialog,
                saveTipViewed = {
                    galleryViewModel.updateFilemanagerTipValue(isCompleted = true)
                },
                doOnConfirm = {
                    viewFilemanagerTipDialog = false
                    viewFileManager(context)
                },
                confirmButtonText = stringResource(R.string.open_file_manager)
            )
        }

        GalleryScreen(
            albums = albums,
            onAlbumClick = { name ->
                navController.navigate(DetailsDestination(name)){
                    popUpTo(DetailsDestination(name)){
                        inclusive = true
                    }
                }
            },
            onCreateClick = {
                navController.navigate(SetupAlbumDestination)
            },
            albumDropDownItems = listOf(
                GalleryMenuItem(id = "move up", text = stringResource(R.string.move_album_up), Icons.Default.ArrowUpward),
                GalleryMenuItem(id = "delete", text = stringResource(R.string.delete_album), Icons.Default.DeleteForever)
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
                        navController.navigate(GalleryDestination){
                            popUpTo(GalleryDestination){
                                inclusive = true
                            }
                        }
                    }
                }
            },
            galleryDropDownItems = galleryDropDownItems,
            onGalleryMenuItemClicked = { id ->
                when (id) {
                    "switch view mode" -> {
                        galleryViewModel.switchGalleryViewMode(!isPagerViewModeOn)
                    }
                    "reset tips" -> {
                        galleryViewModel.resetAllTipsValues()
                    }
                    "file manager" -> {
                        if (isFilemanagerTipCompleted) {
                            viewFileManager(context)
                        } else {
                            viewFilemanagerTipDialog = true
                        }
                    }
                }
            },
            isPagerViewModeOn = isPagerViewModeOn
        )
        if (albumToRemove != null) {
            RemoveAlbumDialog(
                removeAlbum = {
                    val albumToRemoveSafe = albumToRemove ?: ""
                    galleryViewModel.deleteAlbum(albumToRemoveSafe)
                    coroutineScope.launch {
                        mediaManager.deleteAlbum(albumToRemoveSafe)
                        AlarmScheduler(context).cancel(albumToRemoveSafe)
                        albumToRemove = null
                    }
                },
                hideDialog = { albumToRemove = null },
                albumName = albumToRemove!!
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(400.dp))
        }
    }
}

fun viewFileManager(context: Context) {
    val path = Environment.getExternalStorageDirectory().toString() + "/Movies/"
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(Uri.parse(path), "*/*")
    startActivity(context, intent, null)
}

@Composable
private fun GalleryScreen(
    galleryDropDownItems: List<GalleryMenuItem>,
    onGalleryMenuItemClicked: (String) -> Unit,
    albums: List<Album>,
    onCreateClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
    albumDropDownItems: List<GalleryMenuItem>,
    onAlbumMenuItemClicked: (String, String) -> Unit,
    isPagerViewModeOn: Boolean
) {
    var isMenuVisible by rememberSaveable {
        mutableStateOf(false)
    }
    // adds creating new album card
    val albumsWithExtras = albums.plus(Album())

    LaunchedEffect(albumsWithExtras) {
        Log.d(TAG, "albumsWithExtras:")
        albumsWithExtras.forEach { album ->
            Log.d(TAG, "${album.directoryName} id: ${album.id}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                text = stringResource(R.string.my_albums)
            )
            Box {
                IconButton(onClick = { isMenuVisible = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.three_dots),
                        contentDescription = "Menu"
                    )
                }
                DropdownMenu(
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer),
                    expanded = isMenuVisible,
                    onDismissRequest = { isMenuVisible = false }) {
                    galleryDropDownItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item.text) },
                            leadingIcon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.text
                                )
                            },
                            onClick = {
                                onGalleryMenuItemClicked(item.id)
                                isMenuVisible = false
                            },

                            )
                    }
                }
            }
        }
        if (isPagerViewModeOn) {
            PagerGallery(
                albumsWithExtras,
                onAlbumClick,
                onCreateClick,
                albumDropDownItems,
                onAlbumMenuItemClicked
            )
        } else {
            GalleryGrid(
                albumsWithExtras,
                onCreateClick,
                onAlbumClick,
                albumDropDownItems,
                onAlbumMenuItemClicked
            )
        }
    }
}

@Composable
private fun PagerGallery(
    albumsWithExtras: List<Album>,
    onAlbumClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    albumDropDownItems: List<GalleryMenuItem>,
    onAlbumMenuItemClicked: (String, String) -> Unit
) {
    var isContextMenuVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var pressOffset by remember {
        mutableStateOf(DpOffset.Zero)
    }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val savedPage = rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = savedPage.intValue, pageCount = { albumsWithExtras.size })
    LaunchedEffect(pagerState) {
        pagerState.scrollToPage(savedPage.intValue)
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            savedPage.intValue = page
        }
    }

    val fling = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(2)
    )
    var contextMenuPageOpened by remember { mutableIntStateOf(-1) }

    HorizontalPager(
        state = pagerState,
        pageSpacing = 18.dp,
        beyondViewportPageCount = 1,
        flingBehavior = fling,
        contentPadding = PaddingValues(
            horizontal = 38.dp,
            vertical = 8.dp
        )
    ) { page ->
        val isCreateCard = page == albumsWithExtras.size - 1
        val album = albumsWithExtras[page]
        val pageOffset = pagerState.getOffsetDistanceInPages(page).absoluteValue
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Card(
                modifier = Modifier
                    .height((if (isCreateCard) 430.dp else 530.dp) * (1 - (pageOffset * 0.3f)))
                    .indication(interactionSource, LocalIndication.current)
                    .pointerInput(true) {
                        detectTapGestures(
                            onTap = {
                                if (page == albumsWithExtras.size - 1) onCreateClick() else {
                                    onAlbumClick(
                                        album.directoryName
                                    )
                                }
                            },
                            onLongPress = {
                                if (page != albumsWithExtras.size - 1) {
                                    isContextMenuVisible = true
                                    pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                                    contextMenuPageOpened = page
                                    Log.d(TAG, "page: $page, state: $contextMenuPageOpened")
                                }
                            })
                    },
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                ),
                shape = ShapeDefaults.Medium,
            ) {
                if (isCreateCard) {
                    PagerCreateCard(onCreateClick)
                } else {
                    GalleryPagerItem(album,
                        coverPhotoModifier = Modifier
                            .height(300.dp)
                            .graphicsLayer {
                                val scale = lerp(1f, 1.75f, pageOffset)
                                scaleX *= scale
                                scaleY *= scale
                            })
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    DropdownMenu(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer),
        expanded = isContextMenuVisible,
        offset = pressOffset.copy(y = pressOffset.y + 150.dp),
        onDismissRequest = { isContextMenuVisible = false }) {
        albumDropDownItems.forEach { item ->
            DropdownMenuItem(
                text = { Text(text = item.text) },
                leadingIcon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.text
                    )
                },
                onClick = { // Avoid calling for the last item
                    onAlbumMenuItemClicked(item.id, albumsWithExtras[contextMenuPageOpened].directoryName)
                    isContextMenuVisible = false
                    contextMenuPageOpened = -1
                },
            )
        }
    }

}

@Composable
private fun GalleryGrid(
    albumsWithExtras: List<Album>,
    onCreateClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
    albumDropDownItems: List<GalleryMenuItem>,
    onAlbumMenuItemClicked: (String, String) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2), contentPadding = PaddingValues(
            start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp
        ),
        verticalItemSpacing = 8.dp
    ) {
        itemsIndexed(items = albumsWithExtras, key = { index, album ->
            "${album.id}_$index"
        }) { index, album ->
            if (index == albumsWithExtras.size - 1) {
                GridCreateCard(onCreateClick)
            } else {
                GalleryGridItem(
                    album = album,
                    onAlbumClick,
                    dropDownItems = albumDropDownItems,
                    onMenuItemClicked = onAlbumMenuItemClicked
                )
            }
        }
    }
}

@Composable
fun RemoveAlbumDialog(removeAlbum: () -> Unit, hideDialog: () -> Unit, albumName: String) {
    AlertDialog(
        title = { Text(text = stringResource(R.string.remove_album)) },
        text = { Text(text = stringResource(R.string.do_you_really_want_to_remove_album, albumName)) },
        onDismissRequest = hideDialog,
        confirmButton = {
            Row {
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = stringResource(R.string.yes),
                    modifier = Modifier.clickable {
                        removeAlbum()
                    })
            }
        },
        dismissButton = {
            Text(
                text = stringResource(R.string.no),
                modifier = Modifier.clickable { hideDialog() })
        }
    )
}
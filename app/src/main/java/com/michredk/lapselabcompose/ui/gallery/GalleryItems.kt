package com.michredk.lapselabcompose.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.michredk.database.Album
import com.michredk.lapselabcompose.R


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

@Composable
fun GalleryGridItem(
    album: Album,
    onGalleryItemClick: (String) -> Unit,
    dropDownItems: List<GalleryMenuItem>,
    onMenuItemClicked: (String, String) -> Unit,
    coverPhotoModifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
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
            ItemCoverPhoto(album.coverPhotoPath, coverPhotoModifier)
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
                    text = { Text(text = item.text) },
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

// https://www.youtube.com/watch?v=V2Ke-JJDnrU&list=PLWz5rJ2EKKc9tgU26tbUAy01MzC2Yjztb&index=5
// TODO: make pager looks cool

@Composable
fun GalleryPagerItem(
    album: Album,
    coverPhotoModifier: Modifier = Modifier
) {

    Card(modifier = Modifier
        .wrapContentSize()
        .padding(16.dp),
        shape = ShapeDefaults.Medium
    ) {
        ItemCoverPhoto(album.coverPhotoPath, coverPhotoModifier)
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
}


@Composable
fun ItemCoverPhoto(photo: String, modifier: Modifier) {
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current).data(photo).crossfade(1000)
            .transformations().build(),
        contentDescription = "Album cover photo",
        contentScale = ContentScale.Crop
    )
}

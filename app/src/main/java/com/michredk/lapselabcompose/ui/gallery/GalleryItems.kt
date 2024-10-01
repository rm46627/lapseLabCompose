package com.michredk.lapselabcompose.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.michredk.database.Album
import com.michredk.lapselabcompose.R
import com.michredk.lapselabcompose.ui.common.FreqUtils
import com.michredk.lapselabcompose.ui.common.fireColors
import kotlin.random.Random

@Composable
fun GridCreateCard(onCreateNewAlbumClick: () -> Unit, textSize: TextUnit = MaterialTheme.typography.bodyLarge.fontSize ) {
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
            modifier = Modifier.fillMaxWidth(),
            fontSize = textSize
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

@Composable
fun GalleryPagerItem(
    album: Album,
    coverPhotoModifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp),
        shape = ShapeDefaults.Medium
    ) {
        ItemCoverPhoto(album.coverPhotoPath, coverPhotoModifier)
    }
    Text(
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier
            .padding(start = 8.dp)
            .fillMaxWidth(),
        text = album.directoryName,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        style = TextStyle(color = MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        text = "Number of photos",
        textAlign = TextAlign.Center
    )
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val primaryColor = MaterialTheme.colorScheme.primary
    val fireColors = remember {
        fireColors(
            album.photoCount, primaryContainerColor, primaryColor
        )
    }
    val fireBrush = Brush.verticalGradient(colorStops = fireColors)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(fireBrush, blendMode = BlendMode.SrcAtop)
                    }
                },
            painter = painterResource(id = if(album.photoCount > 49) R.drawable.big_fire else R.drawable.small_fire),
            contentDescription = "photo counter icon"
        )
        Text(
            fontSize = 50.sp,
            modifier = Modifier
                .padding(top = 4.dp),
            text = album.photoCount.toString(),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    val daysPassed = remember {
        val lastPhotoDate = FreqUtils.filePathToLocalDateTime(album.coverPhotoPath)
        FreqUtils.localDateToDaysPassed(lastPhotoDate).toInt()
    }
    Text(
        style = TextStyle(color = MaterialTheme.colorScheme.primary, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        text = "Latest photo was taken " + pluralStringResource(id = R.plurals.numberOfDaysAgo, count = daysPassed, daysPassed),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

// TODO: add alpha animation for idea text
@Composable
fun PagerCreateCard(onCreateNewAlbumClick: () -> Unit) {
    GridCreateCard(onCreateNewAlbumClick, MaterialTheme.typography.headlineSmall.fontSize)
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        Text(
            style = TextStyle(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = "Ready to capture something new? How about...?",
        )
        val ideasArray: Array<String> =
            stringArrayResource(id = R.array.album_ideas)
        var randIndex by remember {
            mutableIntStateOf(Random.nextInt(0, ideasArray.size))
        }
        Text(
            modifier = Modifier.padding(top = 8.dp),
            style = TextStyle(fontSize = MaterialTheme.typography.titleMedium.fontSize),
            text = ideasArray[randIndex],
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2
        )
        OutlinedButton(
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            onClick = {
                randIndex++
                if (randIndex >= ideasArray.size) randIndex = 0
            }) {
            Text(
                text = "Next idea"
            )
        }
    }
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
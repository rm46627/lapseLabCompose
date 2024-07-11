package com.example.lapselabcompose.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
import com.example.database.Album
import com.example.database.Repository
import com.example.lapselabcompose.R
import com.example.lapselabcompose.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
fun Gallery(navController: NavController, albums: List<com.example.database.Album>) {

    // adding creating new album card
    val albumsWithExtras = albums.plus(com.example.database.Album())

    Scaffold {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(


            ) {
                itemsIndexed(items = albumsWithExtras, key = { index, album ->
                    album.id
                }) { index, album ->
                    if (index == albums.size) {
                        CreateCard {
                            navController.navigate(AlbumSetupScreen)
                        }
                    } else {
                        GalleryItem(album = album) {
                            navController.navigate(route = AlbumDetailsScreen(album.id))
                        }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GalleryItem(album: Album, onGalleryItemClick: () -> Unit) {
    Card(
        modifier = Modifier.wrapContentSize(),
        shape = ShapeDefaults.Medium,
        onClick = onGalleryItemClick
    ) {
        Column {
            GlideImage(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                model = album.coverPhotoPath,
                contentDescription = "Album cover photo"
            ) {
                it.error(R.drawable.ic_image_placeholder)
                    .placeholder(R.drawable.ic_image_placeholder)
            }
            Text(text = album.directoryName, textAlign = TextAlign.Center)
        }
    }
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
    repository: Repository
) : ViewModel() {
    val getAlbums = repository.readAlbums()

    // used to calculate span size in gallery recyclerView
    var gallerySize: Int = 0
}

@Preview
@Composable
fun PreviewGallery() {
    LapseLabComposeTheme {
        Gallery(
            navController = rememberNavController(),
            albums = listOf(
                com.example.database.Album(
                    1,
                    "Nowy albumik",
                    "",
                    122,
                    5152
                )
            )
        )
    }
}
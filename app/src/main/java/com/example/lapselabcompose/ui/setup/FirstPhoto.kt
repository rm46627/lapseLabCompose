package com.example.lapselabcompose.ui.setup

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.PermissionViewModel
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
import com.example.lapselabcompose.R
import com.example.lapselabcompose.TAG
import com.example.lapselabcompose.ui.CreatingAlbumGraph
import com.example.lapselabcompose.ui.camera.CameraDestination
import com.example.lapselabcompose.ui.gallery.GalleryDestination
import kotlinx.serialization.Serializable

// TODO: view GrantPermissionDialog first before giving user access to this screen

@Serializable
data class FirstPhotoDestination(val albumName: String? = null)

@Composable
fun FirstPhotoRoute(
    navController: NavHostController,
    permissionsResultLaunch: () -> Unit,
    permissionViewModel: PermissionViewModel,
    albumName: String?
) {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(CreatingAlbumGraph)
    }
    val albumCreationViewModel: AlbumCreationViewModel = hiltViewModel(parentEntry)
    albumCreationViewModel.albumName?.let {
        AddFirstPhoto(
            permissionsResultLaunch,
            permissionViewModel,
            onFirstImagePreviewClicked = { albumName ->
                navController.navigate(CameraDestination(albumName))
            },
            onCreateAlbumClicked = { imagePath ->
                albumCreationViewModel.createNewAlbum(imagePath)
                navController.navigate(GalleryDestination)
            },
            albumName = it
        )
    } ?: albumName.let {
        albumCreationViewModel.albumName = it
    }
}

@Composable
fun AddFirstPhoto(
    permissionsResultLaunch: () -> Unit,
    permissionViewModel: PermissionViewModel,
    onFirstImagePreviewClicked: (String) -> Unit,
    onCreateAlbumClicked: (String) -> Unit,
    albumName: String
) {

    val granted by permissionViewModel.allPermissionsGranted.collectAsStateWithLifecycle()
    var imagePath by remember { mutableStateOf<String?>(null) }

    Scaffold {
        Column(Modifier.padding(it)) {
            Text(text = "Add your first photo!")
            IconButton(
                onClick = {
                    if (granted) {
                        onFirstImagePreviewClicked(albumName)
                    } else {
                        permissionsResultLaunch()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                LaunchedEffect(lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        imagePath = MediaManagerFactory(context).getLatestPhotoFile(albumName)?.absolutePath
                    }
                    Log.d(TAG, "path: $imagePath")
                }
                LaunchedEffect(imagePath) {
                    if (imagePath != null) {
                        // ładowanie obrazu
                    }}
                if(imagePath == null){
                    Image(
                        painter = painterResource(id = R.drawable.ic_add_photo),
                        modifier = Modifier.size(300.dp),
                        contentDescription = "First image preview"
                    )
                }
                else {
                    val painter = rememberAsyncImagePainter(imagePath)
                    Image(
                        painter = painter,
                        contentDescription = "Image from path",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            imagePath?.let {
                OutlinedButton(onClick = {
                    onCreateAlbumClicked(it)
                }) {
                    Text(text = "Create new album")
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewFirstPhoto() {
    LapseLabComposeTheme {
        AddFirstPhoto({}, PermissionViewModel(), {}, {}, "" )
    }
}
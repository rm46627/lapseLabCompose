package com.example.lapselabcompose.ui.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.PermissionViewModel
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
import com.example.lapselabcompose.R
import com.example.lapselabcompose.ui.SetupGraph
import com.example.lapselabcompose.ui.common.BackHandlingDialog
import com.example.lapselabcompose.ui.camera.CameraDestination
import com.example.lapselabcompose.ui.gallery.GalleryDestination
import kotlinx.coroutines.launch
import androidx.navigation.NavBackStackEntry
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.serialization.Serializable

// TODO: view GrantPermissionDialog first before giving user access to this screen

@Serializable
data class SetupPhotoDestination(val albumName: String? = null)

@Composable
fun SetupPhotoRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    permissionsResultLaunch: () -> Unit,
    permissionViewModel: PermissionViewModel,
    albumName: String?
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(SetupGraph)
    }
    val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    setupViewModel.albumName?.let {
        SetupPhotoScreen(
            permissionsResultLaunch,
            permissionViewModel,
            onFirstImagePreviewClicked = { albumName ->
                navController.navigate(CameraDestination(albumName))
            },
            onCreateAlbumClicked = { imagePath ->
                navController.navigate(GalleryDestination) {
                    popUpTo(GalleryDestination) {
                        inclusive = true
                    }
                }
                coroutineScope.launch {
                    MediaManagerFactory(context).removeLeftoverPhotosFromNewAlbum(it, imagePath)
                }
                setupViewModel.createNewAlbum(imagePath)
            },
            onLeaveAlertClicked = {
                navController.popBackStack()
            },
            albumName = it,

            )
    } ?: albumName.let {
        setupViewModel.albumName = it
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SetupPhotoScreen(
    permissionsResultLaunch: () -> Unit,
    permissionViewModel: PermissionViewModel,
    onFirstImagePreviewClicked: (String) -> Unit,
    onCreateAlbumClicked: (String) -> Unit,
    onLeaveAlertClicked: () -> Unit,
    albumName: String
) {

    val granted by permissionViewModel.allPermissionsGranted.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var photoPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            photoPath = MediaManagerFactory(context).getLatestPhotoFile(albumName)?.absolutePath
        }
    }

    Scaffold {
        Column(Modifier.padding(it), verticalArrangement = Arrangement.Center) {
            Text(text = "Add your first photo!")
            GlideImage(
                model = if (photoPath == null) R.drawable.ic_add_photo else photoPath,
                contentDescription = "Image from path",
                modifier = Modifier
                    .size(300.dp)
                    .clickable {
                        if (granted) {
                            onFirstImagePreviewClicked(albumName)
                        } else {
                            permissionsResultLaunch()
                        }
                    }
            )
            photoPath?.let {
                OutlinedButton(onClick = {
                    onCreateAlbumClicked(it)
                }) {
                    Text(text = "Create new album")
                }
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    BackHandlingDialog(title = "Leave album creation?",
        text = "If you exit now, you will lose your creation progress. Are you sure you want to do this?",
        onLeaveClicked = {
            coroutineScope.launch {
                photoPath?.let {
                    MediaManagerFactory(context).deleteAlbum(albumName)
                }
            }
            onLeaveAlertClicked()
        })
}

@Preview
@Composable
fun PreviewFirstPhoto() {
    LapseLabComposeTheme {
        SetupPhotoScreen({}, PermissionViewModel(), {}, {}, {}, "")
    }
}
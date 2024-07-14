package com.example.lapselabcompose.ui.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lapselabcompose.PermissionViewModel
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
import com.example.lapselabcompose.R
import kotlinx.serialization.Serializable

// TODO: view GrantPermissionDialog first before giving user access to this screen

@Serializable
object FirstPhotoDestination

@Composable
fun FirstPhotoRoute(
    permissionsResultLaunch: () -> Unit,
    permissionViewModel: PermissionViewModel,
    onFirstImagePreviewClick: () -> Unit,
    navigateToGallery: () -> Unit
) {
    AddFirstPhoto(
        permissionsResultLaunch,
        permissionViewModel,
        onFirstImagePreviewClick,
        navigateToGallery
    )
}

@Composable
fun AddFirstPhoto(
    permissionsResultLaunch: () -> Unit,
    permissionViewModel: PermissionViewModel,
    onFirstImagePreviewClick: () -> Unit,
    navigateToGallery: () -> Unit
) {

    val granted by permissionViewModel.allPermissionsGranted.collectAsStateWithLifecycle()

    var createButtonIsVisible by remember {
        mutableStateOf(false)
    }

    Scaffold {
        Column(Modifier.padding(it)) {
            Text(text = "Add your first photo!")
            IconButton(
                onClick = {
                    if (granted) {
                        onFirstImagePreviewClick()
                    } else {
                        permissionsResultLaunch()

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_add_photo),
                    modifier = Modifier.size(300.dp),
                    contentDescription = "First image preview"
                )
            }
            if (createButtonIsVisible) {
                OutlinedButton(onClick = {
                    // TODO: create album
                    navigateToGallery()
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
        AddFirstPhoto({}, PermissionViewModel(), {}, {} )
    }
}
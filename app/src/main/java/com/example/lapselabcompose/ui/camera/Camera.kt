package com.example.lapselabcompose.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LifecycleEventEffect

import kotlinx.coroutines.launch

@Composable
fun Permissions(
    navigateToCameraScreen: () -> Unit,
) {

    Text(text = "PERMISSION SCREEN")

}


@Composable
fun Camera(
    navigateToPermissionScreen: () -> Unit
) {
//    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
//        if (!hasRequiredPermissions()) {
//            navigateToPermissionScreen()
//        }
//    }

    Text(text = "CAMERA SCREEN")
}

@Composable
fun PhotoView() {

}


package com.example.lapselabcompose

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.setContent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.rememberNavController
import com.example.lapselabcompose.ui.LapselabNavController
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
import com.example.lapselabcompose.ui.common.CameraPermissionTextProvider
import com.example.lapselabcompose.ui.common.PermissionDialog
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()

        this.actionBar?.hide()

        // TODO: take off splashscreen after loading all data

        val permissionViewModel by viewModels<PermissionViewModel>()
        val dialogQueue = permissionViewModel.visiblePermissionDialogQueue
        val permissionsToRequest = permissionViewModel.permissionsToRequest

        permissionViewModel.setAllPermissionsGranted(permissionsToRequest.fold(true) { acc, permission ->
            acc && isPermissionGranted(this, permission)
        })

        setContent {
            LapseLabComposeTheme {
                val multiplePermissionResultLauncher =
                    managedActivityResultLauncher(permissionViewModel)

                LapselabNavController(rememberNavController()).SetupNavGraph(
                    permissionsResultLaunch = {
                        multiplePermissionResultLauncher.launch(permissionsToRequest)
                    }, permissionViewModel = permissionViewModel
                )

                DisplayPermissionDialogs(
                    dialogQueue, permissionViewModel, multiplePermissionResultLauncher
                )
            }
        }
    }

    @Composable
    private fun managedActivityResultLauncher(permissionViewModel: PermissionViewModel): ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> {
        val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { perms ->
                permissionViewModel.permissionsToRequest.forEach { permission ->
                    permissionViewModel.onPermissionResult(
                        permission = permission, isGranted = perms[permission] == true
                    )
                }
            })
        return multiplePermissionResultLauncher
    }

    @Composable
    private fun DisplayPermissionDialogs(
        dialogQueue: SnapshotStateList<String>,
        permissionViewModel: PermissionViewModel,
        multiplePermissionResultLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>
    ) {
        dialogQueue.reversed().forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.CAMERA -> {
                        CameraPermissionTextProvider()
                    }

                    Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        // TODO: create proper classes
                        CameraPermissionTextProvider()
                    }

                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        CameraPermissionTextProvider()
                    }

                    else -> return@forEach
                },
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(permission),
                onDismiss = permissionViewModel::dismissDialog,
                onOkClick = {
                    permissionViewModel.dismissDialog()
                    multiplePermissionResultLauncher.launch(
                        arrayOf(permission)
                    )
                },
                onGoToAppSettingsClick = ::openAppSettings
            )
        }
    }
}

private fun isPermissionGranted(context: Context, permission: String) =
    (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)

private fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

@HiltViewModel
class PermissionViewModel @Inject constructor() : ViewModel() {
    var permissionsToRequest = arrayOf(
        Manifest.permission.CAMERA
    )
    private val permissionsMap = mutableMapOf(Pair(Manifest.permission.CAMERA, false))
    private val _allPermissionsGranted = MutableStateFlow(false)
    val allPermissionsGranted = _allPermissionsGranted.asStateFlow()
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    init {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissionsToRequest = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            permissionsMap[Manifest.permission.READ_EXTERNAL_STORAGE] = false
            permissionsMap[Manifest.permission.WRITE_EXTERNAL_STORAGE] = false
        }
    }

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(
        permission: String, isGranted: Boolean
    ) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        } else {
            permissionsMap[permission] = isGranted
            _allPermissionsGranted.value = permissionsMap.values.all { it }
        }
    }

    fun setAllPermissionsGranted(value: Boolean) = run { _allPermissionsGranted.value = value }
}
package com.example.lapselabcompose.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.provider.ContactsContract.Contacts.Photo
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.lapselabcompose.ui.TakingPhotoGraph
import kotlinx.serialization.Serializable

@Serializable
data class CameraDestination(
    val albumName: String = ""
)

@Composable
fun CameraRoute(backStackEntry: NavBackStackEntry, navController: NavHostController, albumName: String) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(TakingPhotoGraph)
    }
    val takingPhotoViewModel: TakingPhotoViewModel = hiltViewModel(parentEntry)
    takingPhotoViewModel.albumName = albumName

    CameraScreen(
        onPhotoTaken = { bitmap ->
            takingPhotoViewModel.bitmap = bitmap
            navController.navigate(PhotoDestination)
        }
    )
}

@Composable
fun CameraScreen(onPhotoTaken: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    Scaffold{ padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())
            IconButton(
                onClick = {
                    controller.cameraSelector =
                        if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else
                            CameraSelector.DEFAULT_BACK_CAMERA
                },
                modifier = Modifier.offset(16.dp, 16.dp)
            ) {
                Icon(imageVector = Icons.Default.Cameraswitch, contentDescription = "Switch camera")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = {

                }) {
                    Icon(imageVector = Icons.Default.PeopleAlt, contentDescription = "Open gallery")
                }
                CaptureButton(controller, context, onPhotoTaken)
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

@Composable
private fun CaptureButton(
    controller: LifecycleCameraController,
    context: Context,
    onPhotoTaken: (Bitmap) -> Unit
) {
    var captureButtonEnabled by remember {
        mutableStateOf(true)
    }
    IconButton(enabled = captureButtonEnabled,
        onClick = {
            captureButtonEnabled = false
            takePhoto(controller, context, onPhotoTaken)
            captureButtonEnabled = true
        }) {
        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Take photo")
    }
}

fun takePhoto(
    cameraController: LifecycleCameraController,
    context: Context,
    onPhotoTaken: (Bitmap) -> Unit
) {
    cameraController.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )

                onPhotoTaken(rotatedBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}
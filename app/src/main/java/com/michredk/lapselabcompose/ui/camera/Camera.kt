package com.michredk.lapselabcompose.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.michredk.files.appPicturesDir
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselabcompose.ui.CameraGraph
import com.michredk.lapselabcompose.ui.theme.LapseLabComposeTheme
import com.michredk.video.TAG
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// TODO: Add ghost button
//TODO: Display a modal explaining the ghost button usage
// TODO: add slider to control transparency of the ghost image
//TODO: Check if user trying to do next photo in different orientation and warn him about that
// e.g. view black screen with text asking for rotating device
// send proper orientation with args
//TODO: series mode - taking photo without moving to photo fragment, updating ghost image immediately

@Serializable
data class CameraDestination(
    val albumName: String? = null, val navigatedFromAlbumDetails: Boolean = false
)

@Composable
fun CameraRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    albumName: String?,
    navigatedFromAlbumDetails: Boolean = false
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(CameraGraph)
    }
    val cameraViewModel: CameraViewModel = hiltViewModel(parentEntry)
    cameraViewModel.albumName = albumName

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mediaManager = MediaManagerFactory(context)

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    CameraScreen(
        cameraController = cameraController,
        onTakePictureClicked = {
            cameraController.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        val finalBitmap = scaleCropRotateBitmap(image)
                        scope.launch {
                            cameraViewModel.bitmap = finalBitmap
                            val uri = mediaManager.saveBitmap(
                                bitmap = finalBitmap, subfolder = "$appPicturesDir/${albumName}"
                            )
                        }
                        navController.navigate(PhotoPreviewDestination(navigatedFromAlbumDetails))

                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        Log.e("Camera", "Couldn't take photo: ", exception)
                    }
                }
            )
        },
        onChangeCameraClicked = {
            cameraController.cameraSelector =
                if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else CameraSelector.DEFAULT_BACK_CAMERA
        }
    )
}

@Composable
fun CameraScreen(cameraController: LifecycleCameraController, onChangeCameraClicked: () -> Unit, onTakePictureClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CameraPreview(controller = cameraController, modifier = Modifier.fillMaxSize())
        CameraButtons(onChangeCameraClicked, onTakePictureClicked)
    }
}

@Composable
private fun BoxScope.CameraButtons(
    onChangeCameraClicked: () -> Unit,
    onTakePictureClicked: () -> Unit,
) {
    IconButton(
        onClick = onChangeCameraClicked, modifier = Modifier.offset(16.dp, 16.dp)
    ) {
        Icon(imageVector = Icons.Default.Cameraswitch, contentDescription = "Switch camera")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(32.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        IconButton(onClick = {

        }) {
            val primaryColor = MaterialTheme.colorScheme.primary
            Icon(
                imageVector = Icons.Default.PeopleAlt,
                contentDescription = "Open gallery",
                modifier = Modifier.drawBehind {
                    drawCircle(color = primaryColor)
                })
        }
        CaptureButton(onTakePictureClicked)
        Spacer(modifier = Modifier.width(10.dp))
    }
}

@Composable
private fun CaptureButton(
    onTakePictureClicked: () -> Unit
) {
    var captureButtonEnabled by remember {
        mutableStateOf(true)
    }
    IconButton(enabled = captureButtonEnabled, onClick = {
        captureButtonEnabled = false
        onTakePictureClicked()
        captureButtonEnabled = true
    }) {
        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Take photo")
    }
}

fun scaleCropRotateBitmap(
    image: ImageProxy
): Bitmap {
    val bitmap = image.toBitmap()
    Log.d(TAG, "Original height: ${bitmap.height} width: ${bitmap.width}")

    val matrix = Matrix().apply {
        postRotate(image.imageInfo.rotationDegrees.toFloat())
    }

    val targetRatio = 4000f / 2024f

    // Calculate the target dimensions, ensuring the aspect ratio is maintained and no scaling/stretching occurs
    val (targetWidth, targetHeight) = if (bitmap.width.toFloat() / bitmap.height.toFloat() > targetRatio) {
        // Width is too large, so adjust the width to match the target aspect ratio
        val adjustedWidth = (bitmap.height * targetRatio).toInt()
        adjustedWidth to bitmap.height
    } else {
        // Height is too large, so adjust the height to match the target aspect ratio
        val adjustedHeight = (bitmap.width / targetRatio).toInt()
        bitmap.width to adjustedHeight
    }

    // Ensure target dimensions are even numbers
    val finalWidth = targetWidth - targetWidth % 2
    val finalHeight = targetHeight - targetHeight % 2

    // Calculate the x and y coordinates to center the crop
    val x = (bitmap.width - finalWidth) / 2
    val y = (bitmap.height - finalHeight) / 2

    // Create the cropped bitmap centered on the original image
    val croppedBitmap = Bitmap.createBitmap(
        bitmap,
        x,             // X coordinate to start the crop
        y,             // Y coordinate to start the crop
        finalWidth,    // Width of the cropped image
        finalHeight,   // Height of the cropped image
        matrix,
        true
    )

    Log.d(TAG, "Final dimensions height = $finalHeight width = $finalWidth ")
    return croppedBitmap
}
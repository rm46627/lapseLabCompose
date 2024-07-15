package com.example.lapselabcompose.ui.camera

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.lapselabcompose.appPicturesPath
import com.example.lapselabcompose.filenameFormat
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
object CameraDestination

@Composable
fun CameraRoute() {
    val albumName = "testAlbum"
    CameraScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {

        }) { padding ->
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
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.Photo, contentDescription = "Open gallery")
                }
                CaptureButton(controller, context)
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.Photo, contentDescription = "Open gallery")
                }
            }
        }
    }
}

@Composable
private fun CaptureButton(
    controller: LifecycleCameraController,
    context: Context
) {
    var captureButtonEnabled by remember {
        mutableStateOf(true)
    }
    IconButton(enabled = captureButtonEnabled,
        onClick = {
            captureButtonEnabled = false
            takePhoto(controller, context){

            }
            captureButtonEnabled = true
        }) {
        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Take photo")
    }
}

fun takePhoto(cameraController: LifecycleCameraController, context: Context, onPhotoTaken: (Bitmap) -> Unit) {
    // Create time stamped name and MediaStore entry.
    val name = SimpleDateFormat(filenameFormat, Locale.US).format(System.currentTimeMillis())

//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, PHOTO_TYPE)
//            // MediaStore.Images.Media.RELATIVE_PATH requires API level 29+
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.Images.Media.RELATIVE_PATH, "$appPicturesPath/$albumName")
//            } else {
//                val albumFolder = File(
//                    Environment.getExternalStorageDirectory(), "$appPicturesPath/$albumName"
//                )
//                if (!albumFolder.exists()) {
//                    albumFolder.mkdirs()
//                }
//                put(
//                    MediaStore.Images.Media.DATA,
//                    "${Environment.getExternalStorageDirectory()}/$appPicturesPath/$albumName/${name}.jpg"
//                )
//            }
//        }

    val contentValues = ContentValues().apply {
        val albumFolder = File(
            Environment.getExternalStorageDirectory(), "$appPicturesPath/$albumName"
        )
        if (!albumFolder.exists()) {
            albumFolder.mkdirs()
        }
        put(
            MediaStore.Images.Media.DATA,
            "${Environment.getExternalStorageDirectory()}/$appPicturesPath/$albumName/${name}.jpg"
        )
    }


    // Create output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        requireContext().contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    cameraController.takePicture(outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {

            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                // Implicit broadcasts will be ignored for devices running API level >= 24
                // so if you only target API level 24+ you can remove this statement
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    // Suppress deprecated Camera usage needed for API level 23 and below
                    @Suppress("DEPRECATION") requireActivity().sendBroadcast(
                        Intent(android.hardware.Camera.ACTION_NEW_PICTURE, output.savedUri)
                    )
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    val path = File(MediaManagerFactory(requireContext()).getLatestPhotoFile(albumName)!!.toURI()).path
                    val action =
                        CameraFragmentDirections.toPhotoFragment(path)
                    findNavController().safeNavigate(action)
                }
            }
        }
    )
}

@Preview
@Composable
fun PreviewCamera() {
    LapseLabComposeTheme {
        CameraScreen()
    }
}
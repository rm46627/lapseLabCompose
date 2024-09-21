package com.michredk.lapselabcompose.ui.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.michredk.files.appPicturesDir
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselabcompose.R
import com.michredk.lapselabcompose.ui.CameraGraph
import com.michredk.lapselabcompose.ui.common.TipDialog
import com.michredk.lapselabcompose.ui.theme.LapseLabComposeTheme
import com.michredk.video.TAG
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// TODO: add slider to control transparency of the ghost image
//TODO: Check if user trying to do next photo in different orientation and warn him about that
// e.g. view black screen with text asking for rotating device
// send proper orientation with args

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

    val context = LocalContext.current
    val mediaManager = MediaManagerFactory(context)
    val scope = rememberCoroutineScope()
    cameraViewModel.albumName = albumName
    val isGhostBtnTipCompleted by cameraViewModel.isGhostBtnTipCompleted.collectAsStateWithLifecycle(
        initialValue = true
    )
    var viewTip by remember {
        mutableStateOf(false)
    }

    Log.d(TAG, "tip completed: $isGhostBtnTipCompleted")

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    var photosTaken by remember {
        mutableIntStateOf(0)
    }
    LaunchedEffect(key1 = isGhostBtnTipCompleted) {
        viewTip = true
    }
    if (viewTip) {
        TipDialog(
            title = "View ghost of previous photo",
            text = "Lorem ipsum tip",
            viewTipDialog = !isGhostBtnTipCompleted,
            saveTipViewed = {
                cameraViewModel.updateGhostBtnTipValue(true)
                viewTip = false
            }
        )
    }
    CameraScreen(
        cameraController = cameraController,
        albumName = albumName ?: throw NullPointerException(),
        onTakePictureClicked = { shootSeries ->
            cameraController.takePicture(ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        val finalBitmap = scaleCropRotateBitmap(image)
                        cameraViewModel.bitmap = finalBitmap
                        scope.launch {
                            mediaManager.saveBitmap(
                                bitmap = finalBitmap, subfolder = "$appPicturesDir/${albumName}"
                            )
                        }
                        if (shootSeries) {
                            photosTaken++
                        } else {
                            navController.navigate(PhotoPreviewDestination(navigatedFromAlbumDetails))
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        Log.e(TAG, "HERE !!!!!!!! Couldn't take photo: ", exception)
                    }
                })
        },
        onChangeCameraClicked = {
            cameraController.cameraSelector =
                if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else CameraSelector.DEFAULT_BACK_CAMERA
        },
        photosTaken = photosTaken,
        ghostBitmap = cameraViewModel.bitmap
    )
}

@Composable
fun CameraScreen(
    cameraController: LifecycleCameraController,
    albumName: String,
    onChangeCameraClicked: () -> Unit,
    onTakePictureClicked: (Boolean) -> Unit,
    photosTaken: Int,
    ghostBitmap: Bitmap?
) {
    val context = LocalContext.current
    var ghostPath by remember { mutableStateOf<String?>(null) }
    var showFlash by remember {
        mutableStateOf(false)
    }
    var showGhost by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(photosTaken) {
        if (photosTaken > 0) {
            showFlash = true
        }
        if (ghostBitmap == null) ghostPath =
            MediaManagerFactory(context).getLatestPhotoFile(albumName)?.absolutePath
        delay(100)
        showFlash = false
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraPreview(controller = cameraController, modifier = Modifier.fillMaxSize())
        if (showGhost) {
            val data = ghostBitmap ?: ghostPath!!
            Log.d(TAG, "data: ${data}")
            GhostImage(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5f), data = data
            )
        }
        if (showFlash) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }
        CameraButtons(30.dp,
            40.dp,
            btnBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            pressedCaptureBackgroundColor = MaterialTheme.colorScheme.primary,
            onChangeCameraClicked,
            onTakePictureClicked,
            modeButtonsEnabled = ghostPath != null || ghostBitmap != null,
            onGhostImageClicked = {
                showGhost = !showGhost
            })
    }
}

@Composable
fun GhostImage(modifier: Modifier = Modifier, data: Any) {
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current).data(data).build(),
        contentScale = ContentScale.Crop,
        contentDescription = "Ghost image"
    )

}

@Composable
private fun BoxScope.CameraButtons(
    iconSize: Dp,
    btnSize: Dp,
    btnBackgroundColor: Color,
    pressedCaptureBackgroundColor: Color,
    onChangeCameraClicked: () -> Unit,
    onTakePictureClicked: (Boolean) -> Unit,
    modeButtonsEnabled: Boolean,
    onGhostImageClicked: () -> Unit
) {
    var shootSeries by remember {
        mutableStateOf(false)
    }
    var modeIcon by remember {
        mutableStateOf(R.drawable.image_mode)
    }
    var animateModeText by remember {
        mutableStateOf(false)
    }

    val modeAlpha by animateFloatAsState(
        finishedListener = { _ ->
            animateModeText = false
        },
        targetValue = if (animateModeText) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )


    Row(
        modifier = Modifier
            .padding(32.dp, 64.dp)
            .fillMaxWidth()
            .align(Alignment.TopCenter),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        IconButton(
            onClick = onChangeCameraClicked,
            modifier = Modifier
                .size(btnSize)
                .background(btnBackgroundColor, shape = CircleShape),
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Switch camera"
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = 68.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                modifier = Modifier
                    .size(btnSize)
                    .background(
                        if (modeButtonsEnabled) btnBackgroundColor else btnBackgroundColor.copy(
                            alpha = 0.5f
                        ), shape = CircleShape
                    ), enabled = modeButtonsEnabled, onClick = onGhostImageClicked
            ) {
                Icon(
                    imageVector = Icons.Default.PeopleAlt,
                    contentDescription = "Ghost photo",
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        val interactionSource = remember { MutableInteractionSource() }
        val captureIsPressed by interactionSource.collectIsPressedAsState()
        val finalBackgroundColor =
            if (captureIsPressed) pressedCaptureBackgroundColor else btnBackgroundColor
        Column(
            modifier = Modifier.width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CaptureButton(
                interactionSource = interactionSource,
                btnModifier = Modifier
                    .size(btnSize + 15.dp)
                    .background(finalBackgroundColor, shape = CircleShape),
                iconModifier = Modifier.size(iconSize + 10.dp),
                onTakePictureClicked = onTakePictureClicked,
                shootSeries = shootSeries
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (shootSeries) "Series Mode" else "Photo Mode",
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(100.dp)
                    .alpha(modeAlpha)
                    .padding(bottom = 8.dp)
                    .background(
                        btnBackgroundColor, shape = RoundedCornerShape(
                            corner = CornerSize(8.dp)
                        )
                    )
            )
            IconButton(modifier = Modifier
                .size(btnSize)
                .background(
                    if (modeButtonsEnabled) btnBackgroundColor else btnBackgroundColor.copy(
                        alpha = 0.5f
                    ), shape = CircleShape
                ),
                onClick = {
                    shootSeries = !shootSeries
                    modeIcon = if (shootSeries) R.drawable.bursts_mode else R.drawable.image_mode
                    animateModeText = true
                }, enabled = modeButtonsEnabled
            ) {
                Icon(
                    painter = painterResource(id = modeIcon),
                    contentDescription = "Ghost photo",
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Composable
private fun CaptureButton(
    btnModifier: Modifier,
    iconModifier: Modifier,
    onTakePictureClicked: (Boolean) -> Unit,
    interactionSource: MutableInteractionSource,
    shootSeries: Boolean
) {
    IconButton(interactionSource = interactionSource, modifier = btnModifier, onClick = {
        onTakePictureClicked(shootSeries)
    }) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = "Take photo",
            modifier = iconModifier
        )
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
        bitmap, x,             // X coordinate to start the crop
        y,             // Y coordinate to start the crop
        finalWidth,    // Width of the cropped image
        finalHeight,   // Height of the cropped image
        matrix, true
    )

    Log.d(TAG, "Final dimensions height = $finalHeight width = $finalWidth ")
    return croppedBitmap
}

@Preview
@Composable
fun previewButtons() {
    LapseLabComposeTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraButtons(30.dp,
                40.dp,
                btnBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                pressedCaptureBackgroundColor = MaterialTheme.colorScheme.primary,
                { },
                { },
                modeButtonsEnabled = true,
                onGhostImageClicked = {

                })
        }


    }
}
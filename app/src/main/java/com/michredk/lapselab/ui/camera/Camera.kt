package com.michredk.lapselab.ui.camera

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import com.michredk.lapselab.R
import com.michredk.lapselab.services.SnackbarController
import com.michredk.lapselab.services.SnackbarEvent
import com.michredk.lapselab.ui.CameraGraph
import com.michredk.lapselab.ui.common.TipDialog
import com.michredk.video.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// TODO: add slider to control transparency of the ghost image
//TODO: Check if user trying to do next photo in different orientation and warn him about that
// e.g. view black screen with text asking for rotating device
// send proper orientation with args

// TODO: block camera rotation

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

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)

        }
    }
    var photosTaken by remember {
        mutableIntStateOf(0)
    }
    var captureBtnEnabled by remember {
        mutableStateOf(true)
    }
    var showPhotoPicker by remember {
        mutableStateOf(false)
    }
    val alphaAnim = remember {
        Animatable(initialValue = 0f)
    }
    LaunchedEffect(Unit) {
        scope.launch {
            alphaAnim.animateTo(1f, animationSpec = tween(durationMillis = 300))
        }
    }
    BackHandler {
        scope.launch(Dispatchers.Main) {
            alphaAnim.animateTo(0f, animationSpec = tween(durationMillis = 300))
            navController.popBackStack()
        }
    }

    CameraScreen(
        cameraController = cameraController,
        albumName = albumName ?: throw NullPointerException(),
        onTakePictureClicked = { shootBurst ->
            captureBtnEnabled = false
            cameraController.takePicture(ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        val finalBitmap = scaleCropRotateBitmap(
                            image,
                            cameraController.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
                        )
                        image.close()
                        cameraViewModel.bitmap = finalBitmap
                        scope.launch {
                            mediaManager.saveBitmap(
                                bitmap = finalBitmap, subfolder = "$appPicturesDir/${albumName}"
                            )
                        }
                        captureBtnEnabled = true
                        if (shootBurst) {
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
        ghostBitmap = cameraViewModel.bitmap,
        isGhostBtnTipCompleted = isGhostBtnTipCompleted,
        updateGhostBtnTipValue = {
            cameraViewModel.updateGhostBtnTipValue(true)
        },
        navigatedFromAlbumDetails = navigatedFromAlbumDetails,
        captureBtnEnabled = captureBtnEnabled,
        onUploadClicked = { showPhotoPicker = true },
        alpha = alphaAnim.value
    )

    PhotoPicker(showPhotoPicker = showPhotoPicker, onResult = { uris ->
        showPhotoPicker = false
        scope.launch {
            var successFlag: Boolean = false
            var failedFlag: Boolean = false
            var noSelectionFlag: Boolean = false
            if (uris.isNotEmpty()) {
                uris.forEach { uri ->
                    val bitmap = uriToBitmap(context, uri)
                    if (bitmap == null) {
                        failedFlag = true
                    } else {
                        mediaManager.saveBitmap(
                            bitmap = bitmap, subfolder = "$appPicturesDir/${albumName}"
                        )
                        successFlag = true
                    }
                }
            } else {
                noSelectionFlag = true
            }
            SnackbarController.sendEvent(
                event = SnackbarEvent(
                    message = if (successFlag && failedFlag) context.getString(R.string.some_uploads_were_successful_and_some_failed)
                    else if (successFlag) context.getString(R.string.uploaded_photos_successfully)
                    else if (failedFlag) context.getString(R.string.uploading_photos_failed)
                    else if (noSelectionFlag) context.getString(R.string.no_photos_selected)
                    else context.getString(R.string.a_really_obscure_error),
                    duration = SnackbarDuration.Short
                )
            )
        }
    })
}

@Composable
private fun PhotoPicker(showPhotoPicker: Boolean, onResult: (List<Uri>) -> Unit) {
    val pickMultipleMedia =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(),
            onResult = { uris -> onResult(uris) })
    if (showPhotoPicker) {
        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

@Composable
fun CameraScreen(
    cameraController: LifecycleCameraController,
    albumName: String,
    onChangeCameraClicked: () -> Unit,
    onTakePictureClicked: (Boolean) -> Unit,
    photosTaken: Int,
    ghostBitmap: Bitmap?,
    isGhostBtnTipCompleted: Boolean,
    updateGhostBtnTipValue: () -> Unit,
    navigatedFromAlbumDetails: Boolean,
    captureBtnEnabled: Boolean,
    onUploadClicked: () -> Unit,
    alpha: Float
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
    val ghostModeEnabled = ghostPath != null || ghostBitmap != null
    if (navigatedFromAlbumDetails) {
        TipDialog(
            title = stringResource(R.string.what_these_buttons_do),
            content = {
                Column {
                    Row {
                        Icon(
                            modifier = Modifier.padding(end = 4.dp),
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "camera switch explanation"
                        )
                        Text(text = stringResource(R.string.change_camera))
                    }
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Icon(
                            modifier = Modifier.padding(end = 4.dp),
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "upload button explanation"
                        )
                        Text(text = stringResource(R.string.upload_image_from_your_phone))
                    }
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Icon(
                            modifier = Modifier.padding(end = 4.dp),
                            painter = painterResource(id = R.drawable.ic_ghost),
                            contentDescription = "ghost button explanation"
                        )
                        Text(text = stringResource(R.string.switch_on_off_preview_of_the_last_photo))
                    }
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Icon(
                            modifier = Modifier.padding(end = 4.dp),
                            imageVector = Icons.Default.Photo,
                            contentDescription = "Switch camera mode explanation"
                        )
                        Text(text = stringResource(R.string.switch_between_normal_and_burst_modes))
                    }
                }
            },
            viewTipDialog = !isGhostBtnTipCompleted,
            saveTipViewed = updateGhostBtnTipValue
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    ) {
        CameraPreview(controller = cameraController, modifier = Modifier.fillMaxSize())
        if (showGhost) {
            val data = ghostBitmap ?: ghostPath!!
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
        CameraButtons(
            30.dp,
            40.dp,
            btnBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            pressedCaptureBackgroundColor = MaterialTheme.colorScheme.primary,
            onChangeCameraClicked,
            onTakePictureClicked,
            ghostModeEnabled = ghostModeEnabled,
            otherFunctionBtnsEnabled = navigatedFromAlbumDetails,
            onGhostImageClicked = {
                showGhost = !showGhost
            },
            onUploadClicked = onUploadClicked,
            captureBtnEnabled = captureBtnEnabled
        )
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
    ghostModeEnabled: Boolean,
    otherFunctionBtnsEnabled: Boolean,
    onGhostImageClicked: () -> Unit,
    onUploadClicked: () -> Unit,
    captureBtnEnabled: Boolean
) {
    var shootBurst by remember {
        mutableStateOf(false)
    }
    var modeIcon by remember {
        mutableIntStateOf(R.drawable.image_mode)
    }
    var animateModeText by remember {
        mutableStateOf(false)
    }

    val modeAlpha by animateFloatAsState(
        finishedListener = { _ ->
            animateModeText = false
        },
        targetValue = if (animateModeText) 1f else 0f,
        animationSpec = tween(durationMillis = 500), label = ""
    )


    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 38.dp)
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
        IconButton(
            onClick = onUploadClicked,
            modifier = Modifier
                .size(btnSize)
                .background(
                    if (otherFunctionBtnsEnabled) btnBackgroundColor else btnBackgroundColor.copy(
                        alpha = 0.5f
                    ), shape = CircleShape
                ), enabled = otherFunctionBtnsEnabled
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = Icons.Default.UploadFile,
                contentDescription = "Upload photo"
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = 38.dp)
            .windowInsetsPadding(WindowInsets.systemBars),
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
                        if (ghostModeEnabled) btnBackgroundColor else btnBackgroundColor.copy(
                            alpha = 0.5f
                        ), shape = CircleShape
                    ), enabled = ghostModeEnabled, onClick = onGhostImageClicked
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
                    .size(btnSize + 30.dp)
                    .background(finalBackgroundColor, shape = CircleShape),
                iconModifier = Modifier.size(iconSize + 10.dp),
                onTakePictureClicked = onTakePictureClicked,
                shootBurst = shootBurst,
                captureBtnEnabled = captureBtnEnabled
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (shootBurst) stringResource(R.string.burst_mode) else stringResource(R.string.photo_mode),
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
            IconButton(
                modifier = Modifier
                    .size(btnSize)
                    .background(
                        if (otherFunctionBtnsEnabled) btnBackgroundColor else btnBackgroundColor.copy(
                            alpha = 0.5f
                        ), shape = CircleShape
                    ),
                onClick = {
                    shootBurst = !shootBurst
                    modeIcon = if (shootBurst) R.drawable.bursts_mode else R.drawable.image_mode
                    animateModeText = true
                }, enabled = otherFunctionBtnsEnabled
            ) {
                Icon(
                    painter = painterResource(id = modeIcon),
                    contentDescription = "Photo mode switch",
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
    shootBurst: Boolean,
    captureBtnEnabled: Boolean
) {
    IconButton(
        enabled = captureBtnEnabled,
        interactionSource = interactionSource,
        modifier = btnModifier,
        onClick = {
            onTakePictureClicked(shootBurst)
        }) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = "Take photo",
            modifier = iconModifier
        )
    }
}

fun scaleCropRotateBitmap(
    image: ImageProxy,
    mirrorImage: Boolean
): Bitmap {
    val bitmap = image.toBitmap()
    val isPhotoVertical =
        image.imageInfo.rotationDegrees.toFloat() == 90f || image.imageInfo.rotationDegrees.toFloat() == 270f

    val matrix = Matrix().apply {
        if (mirrorImage) preScale(1f, -1f);
        if (isPhotoVertical) postRotate(image.imageInfo.rotationDegrees.toFloat()) else postRotate(
            image.imageInfo.rotationDegrees.toFloat() + 90f
        )
    }
    val targetRatio = 4000f / 2024f
//    val targetRatio = 1280f / 646f

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
    var finalWidth = targetWidth - targetWidth % 2
    var finalHeight = targetHeight - targetHeight % 2

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

    Log.d(
        TAG,
        "From: height = ${bitmap.height} width = ${bitmap.width} to Final dimensions height = $finalHeight width = $finalWidth "
    )
    return croppedBitmap
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    // Obtain the content resolver from the context
    val contentResolver: ContentResolver = context.contentResolver

    // Check the API level to use the appropriate method for decoding the Bitmap
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // For Android P (API level 28) and higher, use ImageDecoder to decode the Bitmap
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        // For versions prior to Android P, use BitmapFactory to decode the Bitmap
        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            Bitmap.createBitmap(BitmapFactory.decodeStream(stream))
        }
        bitmap
    }
}
package com.example.lapselabcompose.ui.camera

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.files.MediaStoreManager
import com.example.lapselabcompose.R
import com.example.lapselabcompose.ui.TakingPhotoGraph
import com.example.lapselabcompose.ui.gallery.GalleryDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
object PhotoDestination

@Composable
fun PhotoRoute(navController: NavHostController) {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(TakingPhotoGraph)
    }
    val viewModel: TakingPhotoViewModel = hiltViewModel(parentEntry)
    val scope = rememberCoroutineScope()

    viewModel.bitmap?.let {
        PhotoScreen(
            bitmap = it,
            onDiscardClicked = {
                navController.navigateUp()
            },
            onAcceptClicked = { context ->
                scope.launch {
                    withContext(Dispatchers.Main) {
                        val name = SimpleDateFormat(FILENAME, Locale.US).format(System.currentTimeMillis())
                        // Use MediaStoreManager to save and share bitmap
                        val (uri, filename) = MediaStoreManager(context).saveShareBitmap(it, viewModel.albumName!!, name)
                        navController.navigate(GalleryDestination)
                        if (uri is Uri) {
                            val chooser = Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    clipData =
                                        ClipData.newRawUri(filename, uri)  // to fix Security Exception
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    type = PHOTO_TYPE
                                }, context.getString(R.string.share_where)
                            )
                            context.startActivity(chooser)
                        }
                    }
                }
//                    withContext(Dispatchers.Main){
//                        val name = SimpleDateFormat(FILENAME, Locale.US).format(System.currentTimeMillis())
//                        // in this scope I make share bitmap by URI
//                        val (uri, filename) = MediaStoreManager(context).saveShareBitmap(it, viewModel.albumName!!, name)
//
//                        if (uri is Uri && filename is String) {
//                            val chooser = Intent.createChooser(
//                                Intent(Intent.ACTION_SEND).apply {
//                                    clipData =
//                                        ClipData.newRawUri(filename, uri)  // to fix Security Exception
//                                    putExtra(Intent.EXTRA_STREAM, uri)
//                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                                    type = "image/png"
//                                }, context.getString(R.string.share_where)
//                            )
//                            context.startActivity(chooser)
//                        }
//                    }

            }
        )
    } ?: throw IllegalArgumentException()
}

@Composable
fun PhotoScreen(
    bitmap: Bitmap,
    onDiscardClicked: () -> Unit,
    onAcceptClicked: (context: Context) -> Unit,
    ) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(onClick = onDiscardClicked) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Discard image button")
            }
            val context = LocalContext.current
            IconButton(onClick = { onAcceptClicked(context) }) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Accept image button")
            }
        }
    }
}


    const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    const val PHOTO_TYPE = "image/png"

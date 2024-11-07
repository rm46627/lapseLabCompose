package com.michredk.lapselab.ui.details

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.michredk.lapselab.files.MediaManagerFactory
import com.michredk.lapselab.R
import com.michredk.lapselab.TAG
import com.michredk.lapselab.ui.DetailsGraph
import com.michredk.lapselab.ui.PhotosGraph
import com.michredk.lapselab.ui.common.FreqUtils
import com.michredk.lapselab.ui.theme.LapseLabComposeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.lang.NullPointerException

@Serializable
data class PhotoBrowserDestination(val index: Int = 0)

@Composable
fun PhotoBrowserRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    index: Int
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(DetailsGraph)
    }
    if (parentEntry != null) {
        val viewModel: DetailsViewModel = hiltViewModel(parentEntry)
        val photosState by viewModel.photos.collectAsStateWithLifecycle()

        photosState?.let { photos ->
            val photoPath = photos[index].absolutePath
            PhotoBrowserScreen(
                onNextButtonClicked = {
                    navController.navigate(PhotoBrowserDestination(index + 1)) {
                        popUpTo(PhotosGraph) {
                            inclusive = true
                        }
                    }
                },
                onPreviousButtonClicked = {
                    navController.navigate(PhotoBrowserDestination(index - 1)) {
                        popUpTo(PhotosGraph) {
                            inclusive = true
                        }
                    }
                },
                onDeleteButtonClicked = {
                    scope.launch(Dispatchers.IO) {
                        MediaManagerFactory(context).deletePhoto(photos[index].absolutePath)
                        val updatedPhotos = photos.filterIndexed { deletedIdx, _ -> deletedIdx != index }
                        if (photos.size > 1) {
                            val destIndex = if (index == 0) 0 else index - 1
                            Log.d(TAG, "idx: $index, dest: $destIndex")
                            withContext(Dispatchers.Main) {
                                navController.navigate(PhotoBrowserDestination(destIndex)) {
                                    popUpTo(PhotosGraph) {
                                        inclusive = true
                                    }
                                }
                            }
                            viewModel.setPhotos(updatedPhotos)
                        } else {
                            withContext(Dispatchers.Main) {
                                navController.popBackStack()
                            }
                        }
                    }
                },
                photoPath = photoPath,
                date = FreqUtils.filePathToFormatedDateTime(photoPath),
                index,
                index == photos.size - 1
            )
        }
    } else {
        navController.popBackStack()
    }
}

@Composable
fun PhotoBrowserScreen(
    onNextButtonClicked: () -> Unit,
    onPreviousButtonClicked: () -> Unit,
    onDeleteButtonClicked: () -> Unit,
    photoPath: String,
    date: String,
    index: Int,
    isLastPhoto: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoPath)
                .build(),
            contentDescription = "Gallery photo",
            contentScale = ContentScale.Crop,
        )
        PrevNextButtons(index, onPreviousButtonClicked, isLastPhoto, onNextButtonClicked)
        var isMenuVisible by remember {
            mutableStateOf(false)
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp)
        ) {
            IconButton(
                onClick = { isMenuVisible = true },
                modifier = Modifier
                    .padding(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(30)
                    ),
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.three_dots),
                    contentDescription = "Menu"
                )
            }
            DropdownMenu(
                modifier = Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer),
                expanded = isMenuVisible,
                onDismissRequest = { isMenuVisible = false }) {
                Column(Modifier.padding(4.dp)) {
                    Text(
                        style = TextStyle(color = MaterialTheme.colorScheme.primary),
                        text = stringResource(R.string.photo_added)
                    )
                    Text(text = date)
                }
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.remove_photo)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Delete photo"
                        )
                    },
                    onClick = {
                        onDeleteButtonClicked()
                        isMenuVisible = false
                    },

                    )

            }
        }

    }
}

@Composable
private fun BoxScope.PrevNextButtons(
    index: Int,
    onPreviousButtonClicked: () -> Unit,
    isLastPhoto: Boolean,
    onNextButtonClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val prevEnabled = index != 0
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clickable {
                    if (prevEnabled) onPreviousButtonClicked()
                }) {}
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .clickable { if (!isLastPhoto) onNextButtonClicked() })
        {}
    }
}

@Preview
@Composable
private fun nextPrev() {
    LapseLabComposeTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            PrevNextButtons(
                index = 1,
                onPreviousButtonClicked = {  },
                isLastPhoto = false
            ) {

            }
        }
    }

}
package com.example.lapselabcompose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.database.Album
import com.example.database.Repository
import com.example.lapselab.files.MediaManagerFactory
import com.example.lapselabcompose.ui.setup.AlbumCreationViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class AlbumDetailsDestination(val id: Int = 0)

@Composable
fun AlbumDetailsRoute(backStackEntry: NavBackStackEntry, navController: NavHostController, id: Int) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(AlbumDetailsGraph)
    }
    val albumDetailsViewModel: AlbumDetailsViewModel = hiltViewModel(parentEntry)
    albumDetailsViewModel.setAlbumId(id)
    val album by albumDetailsViewModel.album.collectAsStateWithLifecycle()
    val context = LocalContext.current
//    val images = MediaManagerFactory(context).getPhotoFiles()
    AlbumDetails()


}

@Composable
fun AlbumDetails() {

}

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val _albumId = MutableStateFlow<Int?>(null)
    val albumId: StateFlow<Int?> = _albumId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _album = _albumId.flatMapLatest { albumId ->
        if (albumId != null) {
            repository.getAlbum(albumId)
        } else {
            flowOf(null)
        }}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val album: StateFlow<Album?> = _album

    fun setAlbumId(id: Int) {
        _albumId.value = id
    }

    fun updateAlbum(album: Album) {
        viewModelScope.launch {
            repository.updateAlbum(album)
        }
    }

}
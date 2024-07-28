package com.example.lapselabcompose.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.Album
import com.example.database.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val albums: Flow<List<Album>> = repository.getAlbums()
    var albumName: String? = null

    fun createNewAlbum(imagePath: String) {
        viewModelScope.launch {
            if (albumName == null) {
                throw IllegalArgumentException()
            }
            val newAlbum = Album(
                directoryName = albumName!!,
                coverPhotoPath = imagePath
            )
            repository.addAlbum(newAlbum)
        }
    }
}
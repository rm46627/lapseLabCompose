package com.example.lapselabcompose.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.Album
import com.example.database.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Scope

@HiltViewModel
class AlbumCreationViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    var albums: Flow<List<Album>> = repository.readAlbums()
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
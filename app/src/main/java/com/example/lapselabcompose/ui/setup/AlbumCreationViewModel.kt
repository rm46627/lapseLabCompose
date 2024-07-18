package com.example.lapselabcompose.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.database.Album
import com.example.database.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Scope

@HiltViewModel
class AlbumCreationViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private var albums: Flow<List<Album>> = repository.readAlbums()
    var albumName: String? = null
    private var imagePath: String? = null

    suspend fun createNewAlbum() {
        if (imagePath == null || albumName == null) {
            throw IllegalArgumentException()
        }
        val newAlbum = Album(
            directoryName = albumName!!,
            coverPhotoPath = imagePath!!
        )
        repository.addAlbum(newAlbum)
    }

    suspend fun checkUniqueness(name: String): Boolean {
        val albumNameTaken = albums.first().none { album ->
            album.directoryName == name
        }
        albumName = name
        return albumNameTaken
    }
}
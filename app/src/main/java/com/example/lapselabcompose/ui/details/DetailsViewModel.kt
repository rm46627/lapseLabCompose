package com.example.lapselabcompose.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.Album
import com.example.database.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val _albumName = MutableStateFlow<String?>(null)
    val albumName: StateFlow<String?> = _albumName.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _album = _albumName.flatMapLatest { albumId ->
        if (albumId != null) {
            repository.getAlbum(albumId)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val album = _album

    fun setAlbumName(name: String) {
        _albumName.value = name
    }

    fun updateAlbum(album: Album) {
        viewModelScope.launch {
            repository.updateAlbum(album)
        }
    }
}
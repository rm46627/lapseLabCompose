package com.michredk.lapselabcompose.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michredk.database.Album
import com.michredk.database.DataStoreRepository
import com.michredk.database.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: Repository,
    private val dataStore: DataStoreRepository
) : ViewModel() {

    val getAlbums = repository.getAlbums()
    val isContextMenuTipCompleted = dataStore.readContextMenuTipState()

    fun deleteAlbum(albumName: String) {
        viewModelScope.launch {
            repository.deleteAlbum(albumName)
        }
    }

    fun updateAlbum(album: Album) {
        viewModelScope.launch {
            repository.updateAlbum(album)
        }
    }

    fun updateContextMenuTipValue(isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.saveContextMenuTipState(isCompleted)
        }
    }

    fun resetAllTipsValues() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.resetAllTips()
        }
    }
}
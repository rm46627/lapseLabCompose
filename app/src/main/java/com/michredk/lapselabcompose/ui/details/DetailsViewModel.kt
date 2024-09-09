package com.michredk.lapselabcompose.ui.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michredk.database.Album
import com.michredk.database.Repository
import com.michredk.lapselabcompose.TAG
import com.michredk.lapselabcompose.ui.common.FreqUtils
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
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val _albumName = MutableStateFlow<String?>(null)
    val albumName: StateFlow<String?> = _albumName.asStateFlow()

    private val _photos = MutableStateFlow<List<File>?>(null)
    val photos: StateFlow<List<File>?> = _photos.asStateFlow()

    private val _labUiState = MutableStateFlow(LabUiState())
    val labUiState: StateFlow<LabUiState> = _labUiState.asStateFlow()

    private val _videoProperties = MutableStateFlow(LabUiState())
    val videoProperties: StateFlow<LabUiState> = _videoProperties.asStateFlow()

    var daysBetweenReminders: Long = 0

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _album = _albumName.flatMapLatest { albumId ->
        if (albumId != null) {
            repository.getAlbum(albumId)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val album = _album

    init {
        viewModelScope.launch {
            _album.collect { album ->
                album?.let {
                    val state = LabUiState(
                        framesPerImage = it.framesPerImage, bitrate = it.bitrate
                    )
                    _labUiState.value = state
                    _videoProperties.value = state
                    daysBetweenReminders = album.daysBetweenReminders
                }
            }
        }
    }

    fun setAlbumName(name: String) {
        _albumName.value = name
    }

    fun setPhotos(files: List<File>) {
        _photos.value = files
    }

    fun updateAlbum(album: Album, freq: String? = null) {
        viewModelScope.launch {
            if (freq != null){
                val days = FreqUtils.freqStrToDays(freq)
                daysBetweenReminders = days
                Log.d(TAG, "freq: $freq days: $days")
            }
            val updatedAlbum = album.copy(daysBetweenReminders = daysBetweenReminders, framesPerImage = labUiState.value.framesPerImage, bitrate = labUiState.value.bitrate)
            Log.d(TAG, "updated: $updatedAlbum")
            repository.updateAlbum(updatedAlbum)
        }
    }

    fun updateCoverPhoto(path: String) {
        viewModelScope.launch {
            _albumName.value?.let { repository.updateCoverPhoto(it, path) }
        }
    }

    fun updateLabUiState(newState: LabUiState) {
        _labUiState.value = newState
    }

    fun frequencyIsValid(value: String): Boolean {
        val isValid = FreqUtils.frequencyIsValid(value)
        if (isValid){
            daysBetweenReminders = freqStrToDays(value)
        }
        return isValid
    }

    private fun freqStrToDays(freq: String): Long = FreqUtils.freqStrToDays(freq)

}

data class LabUiState(
    val framesPerImage: Int = 10,
    val bitrate: Int = 1500000
)
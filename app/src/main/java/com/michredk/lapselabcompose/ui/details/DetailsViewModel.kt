package com.michredk.lapselabcompose.ui.details

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.exoplayer.ExoPlayer
import com.michredk.database.Album
import com.michredk.database.DataStoreRepository
import com.michredk.database.Repository
import com.michredk.lapselabcompose.services.alarm.AlarmScheduler
import com.michredk.lapselabcompose.ui.common.FreqUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(@ApplicationContext private val applicationContext: Context, private val repository: Repository, private val dataStore: DataStoreRepository) : ViewModel() {
    private val _albumName = MutableStateFlow<String?>(null)
    val albumName: StateFlow<String?> = _albumName.asStateFlow()

    private val _photos = MutableStateFlow<List<File>?>(null)
    val photos: StateFlow<List<File>?> = _photos.asStateFlow()

    private val _labUiState = MutableStateFlow(LabUiState())
    val labUiState: StateFlow<LabUiState> = _labUiState.asStateFlow()

    private val _videoProperties = MutableStateFlow(LabUiState())
    val videoProperties: StateFlow<LabUiState> = _videoProperties.asStateFlow()

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(applicationContext).build().apply {
            playWhenReady = true
            repeatMode = REPEAT_MODE_ONE
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _album = _albumName.flatMapLatest { albumName ->
        if (albumName != null) {
            repository.getAlbum(albumName)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val album = _album

    fun setAlbumName(name: String) {
        _albumName.value = name
    }

    fun setPhotos(files: List<File>) {
        _photos.value = files
    }

    fun updateAlbum(album: Album, freq: String? = null, notificationTime: LocalTime? = null, scheduler: AlarmScheduler? = null, newPhotoTaken: Boolean? = null) {
        viewModelScope.launch {
            val days = if(freq != null)
                FreqUtils.freqStrToDays(freq)
            else
                album.daysBetweenReminders

            val updatedAlbum = album.copy(
                reminderTime = notificationTime ?: album.reminderTime,
                daysBetweenReminders = days,
            )
            repository.updateAlbum(updatedAlbum)
        }
    }

    fun updateCoverAndCounter(path: String) {
        viewModelScope.launch {
            album.value?.let {
                repository.updateAlbum(it.copy(coverPhotoPath = path, photoCount = _photos.value?.size ?: 0))
            }
        }
    }

    fun updateLabUiState(newState: LabUiState) {
        _labUiState.value = newState
    }

    fun updateVideoProperties(newState: LabUiState) {
        _videoProperties.value = newState
    }

    override fun onCleared() {
        super.onCleared()
    }

}

data class LabUiState(
    val framesPerImage: Int = 0,
    val bitrate: Int = 0
)
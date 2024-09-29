package com.michredk.lapselabcompose.ui.setup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michredk.database.Album
import com.michredk.database.Repository
import com.michredk.lapselabcompose.TAG
import com.michredk.lapselabcompose.services.alarm.AlarmScheduler
import com.michredk.lapselabcompose.ui.common.FreqUtils
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
    private var notificationFrequency: String? = null

    fun createNewAlbum(imagePath: String, alarmScheduler: AlarmScheduler) {
        viewModelScope.launch {
            Log.d(TAG, "create new album: ${Thread.currentThread().name}")
            val name = albumName ?: throw IllegalArgumentException("AlbumName is null")
            val freq = notificationFrequency ?: throw IllegalArgumentException("Frequency is null")
            val daysBetweenReminders = freqStrToDays(freq)
            val newAlbum = Album(
                directoryName = name,
                coverPhotoPath = imagePath,
                daysBetweenReminders = daysBetweenReminders
            )

            repository.addAlbum(newAlbum)
            Log.d(TAG, "daysBetweenReminders: $daysBetweenReminders ")
            if (daysBetweenReminders != 0L) {
                alarmScheduler.schedule(name, daysBetweenReminders)
            }
        }
    }

    fun frequencyIsValid(value: String): Boolean {
        val isValid = FreqUtils.frequencyIsValid(value)
        Log.d(TAG, "isValid $isValid")
        if (isValid){
            notificationFrequency = value
        }
        return isValid
    }

    private fun freqStrToDays(freq: String): Long = FreqUtils.freqStrToDays(freq)
}
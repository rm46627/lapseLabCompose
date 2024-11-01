package com.michredk.lapselab.ui.setup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michredk.database.Album
import com.michredk.database.DataStoreRepository
import com.michredk.database.Repository
import com.michredk.lapselab.TAG
import com.michredk.lapselab.services.alarm.AlarmScheduler
import com.michredk.lapselab.ui.common.FreqUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val repository: Repository,
    private val dataStore: DataStoreRepository
) : ViewModel() {

    val albums: Flow<List<Album>> = repository.getAlbums()
    var albumName: String? = null
    private var notificationFrequency: String? = null

    val wasFirstAlbumEverCreated = dataStore.readFirstAlbumEverCreated()

    fun updateWasFirstAlbumEverCreated() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.saveFirstAlbumEverCreated()
        }
    }

    fun createNewAlbum(imagePath: String, alarmScheduler: AlarmScheduler) {
        viewModelScope.launch {
            Log.d(TAG, "create new album: ${Thread.currentThread().name}")
            val name = albumName ?: throw IllegalArgumentException("AlbumName is null")
            val freq = notificationFrequency ?: throw IllegalArgumentException("Frequency is null")
            val daysBetweenReminders = freqStrToDays(freq)
            val now = LocalDateTime.now()
            val newAlbum = Album(
                directoryName = name,
                coverPhotoPath = imagePath,
                daysBetweenReminders = daysBetweenReminders,
                lastReminderSentOn = now
            )

            repository.addAlbum(newAlbum)
            Log.d(TAG, "daysBetweenReminders: $daysBetweenReminders ")
            if (daysBetweenReminders != 0L) {
                alarmScheduler.schedule(
                    albumName = name,
                    daysBetweenAlarms = daysBetweenReminders,
                    notifyTime = LocalTime.now(),
                    lastReminderSentOn = now
                )
            }
        }
    }

    fun frequencyIsValid(value: String): Boolean {
        val isValid = FreqUtils.frequencyIsValid(value)
        Log.d(TAG, "isValid $isValid")
        if (isValid) {
            notificationFrequency = value
        }
        return isValid
    }

    private fun freqStrToDays(freq: String): Long = FreqUtils.freqStrToDays(freq)
}
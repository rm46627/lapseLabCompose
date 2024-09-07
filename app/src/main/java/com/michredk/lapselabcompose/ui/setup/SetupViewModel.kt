package com.michredk.lapselabcompose.ui.setup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michredk.database.Album
import com.michredk.database.Repository
import com.michredk.lapselabcompose.TAG
import com.michredk.lapselabcompose.services.alarm.AlarmScheduler
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
    var notificationFrequency: String? = null

    fun frequencyIsValid(value: String): Boolean {
        val patterns = listOf(
            "I don't need a reminder",
            "Everyday",
            "Every\\s+\\d+\\s+days",
            "Once\\s+a\\s+week",
            "Every\\s+\\d+\\s+weeks",
            "Once\\s+a\\s+month",
            "Every\\s+\\d+\\s+months"
        )
        val cleanedValue = value.trim().replace("\\s+".toRegex(), " ")
        val isValid = patterns.any { cleanedValue.matches(it.toRegex(RegexOption.IGNORE_CASE)) }
        if (isValid) {
            notificationFrequency = value
        }
        return isValid
    }

    fun createNewAlbum(imagePath: String, alarmScheduler: AlarmScheduler) {
        viewModelScope.launch {
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

//    "I don't need a reminder",
//    "Everyday",
//    "Every 2 days",
//    "Once a week",
//    "Every 3 weeks",
//    "Once a month",
//    "Every 6 months"

    private fun freqStrToDays(freq: String): Long {
        val words = freq.split(" ")
        val timeMap = mapOf(
            "day" to 1L,
            "days" to 1L,
            "week" to 7L,
            "weeks" to 7L,
            "month" to 30L,
            "months" to 30L
        )
        return when (words.size) {
            1 -> 1
            3 -> {
                val timeUnit = timeMap[words[2]] ?: 0
                val days = if (words[0] == "Once") {
                    timeUnit
                } else timeUnit * words[1].toLong()
                days
            }

            else -> 0
        }
    }
}
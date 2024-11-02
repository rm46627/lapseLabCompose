package com.michredk.lapselab.services.alarm

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.michredk.database.Repository
import com.michredk.lapselab.TAG
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AlarmReschedulerWorker(
    private val context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun repository(): Repository
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {

            val hiltEntryPoint =
                EntryPointAccessors.fromApplication(context, WorkerEntryPoint::class.java)
            val repository = hiltEntryPoint.repository()

            val albums = repository.getAlbums().first()
            val alarmScheduler = AlarmScheduler(context)
            albums.forEach { album ->
                Log.d(TAG, "${album.directoryName}, days:${album.daysBetweenReminders}, notif: ${album.reminderTime}, last: ${album.lastReminderSentOn}")
                alarmScheduler.schedule(
                    albumName = album.directoryName,
                    daysBetweenAlarms = album.daysBetweenReminders,
                    notifyTime = album.reminderTime,
                    lastReminderSentOn = album.lastReminderSentOn
                )
            }
            return@withContext Result.success()
        }
    }
}
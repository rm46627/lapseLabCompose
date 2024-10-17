package com.michredk.lapselab.services.alarm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.michredk.database.Repository
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
                alarmScheduler.schedule(
                    albumName = album.directoryName,
                    daysBetweenAlarms = album.daysBetweenReminders,
                    time = album.lastReminderSentOn.toLocalTime(),
                    lastReminderSentOn = album.lastReminderSentOn
                )
            }
            return@withContext Result.success()
        }
    }
}
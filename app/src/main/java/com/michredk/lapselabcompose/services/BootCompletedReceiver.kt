package com.michredk.lapselabcompose.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.michredk.database.Repository
import com.michredk.lapselabcompose.services.alarm.AlarmReschedulerWorker
import com.michredk.lapselabcompose.services.alarm.AlarmScheduler
import kotlinx.coroutines.flow.take
import javax.inject.Inject

class BootCompletedReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            val workManager = WorkManager.getInstance(context)
            val request = OneTimeWorkRequestBuilder<AlarmReschedulerWorker>()
                .build()
            workManager.enqueue(request)
        }
    }
}
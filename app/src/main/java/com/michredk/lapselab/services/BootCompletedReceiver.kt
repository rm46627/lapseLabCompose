package com.michredk.lapselab.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.michredk.lapselab.services.alarm.AlarmReschedulerWorker

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
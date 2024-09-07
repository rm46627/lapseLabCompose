package com.michredk.lapselabcompose.services.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.michredk.lapselabcompose.TAG
import java.time.LocalDateTime
import java.time.ZoneId

// TODO: Schedule an alarm to go off at a time picked by the user.
// TODO: Reschedule alarms after phone restart - save last notification time in entity

class AlarmScheduler(
    private val context: Context
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(albumName: String, daysBetweenAlarms: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALBUM_NAME", albumName)
            putExtra("DAYS_BETWEEN", daysBetweenAlarms)
        }

        val notifyAt = LocalDateTime.now().plusDays(daysBetweenAlarms)
        val zonedDateTime = notifyAt.atZone(ZoneId.systemDefault())
        val timeInMillis = zonedDateTime.toInstant().toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            PendingIntent.getBroadcast(
                context,
                albumName.hashCode(), // identifier for adding new or updating existing alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    fun cancel(albumName: String) {
        Log.d(TAG, "alarm cancel: $albumName")
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                albumName.hashCode(), // identifier for canceling alarm
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}
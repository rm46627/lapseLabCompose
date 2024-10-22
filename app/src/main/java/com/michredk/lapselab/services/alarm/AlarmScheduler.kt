package com.michredk.lapselab.services.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.michredk.lapselab.TAG
import okhttp3.internal.notify
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

// TODO: Reschedule alarms after phone restart - save last notification time in entity

class AlarmScheduler(
    private val context: Context
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(
        albumName: String,
        daysBetweenAlarms: Long,
        notifyTime: LocalTime,
        lastReminderSentOn: LocalDateTime
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALBUM_NAME", albumName)
            putExtra("DAYS_BETWEEN", daysBetweenAlarms)
            putExtra("HOUR", notifyTime.hour)
            putExtra("MINUTE", notifyTime.minute)
        }

        val nowTime = LocalTime.now()

        val notifyDate = if (nowTime.isBefore(notifyTime))
            lastReminderSentOn.plusDays(daysBetweenAlarms - 1).toLocalDate()
        else
            lastReminderSentOn.plusDays(daysBetweenAlarms).toLocalDate()

        val notifyAt = LocalDateTime.of(notifyDate, notifyTime)
        val zonedDateTime = notifyAt.atZone(ZoneId.systemDefault())
        val timeInMillis = zonedDateTime.toInstant().toEpochMilli()

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            PendingIntent.getBroadcast(
                context,
                albumName.hashCode(), // identifier for adding new or updating existing alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        Log.d(TAG, "schedudled alarm for $notifyDate and $nowTime")
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
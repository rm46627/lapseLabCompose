package com.michredk.lapselab.services.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.michredk.lapselab.TAG
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

// TODO: Reschedule alarms after phone restart - save last notification time in entity

class AlarmScheduler(
    private val context: Context
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(albumName: String, daysBetweenAlarms: Long, time: LocalTime? = null, lastReminderSentOn: LocalDateTime = LocalDateTime.now()) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALBUM_NAME", albumName)
            putExtra("DAYS_BETWEEN", daysBetweenAlarms)
            time?.let {
                putExtra("HOUR", time.hour)
                putExtra("MINUTE", time.minute)
            }
        }

        val now = lastReminderSentOn
        val notifyTime = LocalTime.of(time?.hour ?: now.hour, time?.minute ?: now.minute)
        val timeHasPassedToday = !now.isBefore(LocalDateTime.of(LocalDate.now(), notifyTime))
        val notifyDate = if (timeHasPassedToday){
            now.toLocalDate().plusDays(daysBetweenAlarms)
        } else {
            now.toLocalDate().plusDays(daysBetweenAlarms-1)
        }

        val notifyAt = LocalDateTime.of(notifyDate, notifyTime)
//        val notifyAt = LocalDateTime.of(now.toLocalDate(), now.plusSeconds(10).toLocalTime())
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
        Log.d(TAG, "schedudled alarm")
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
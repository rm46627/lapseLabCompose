package com.example.lapselabcompose.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.lapselabcompose.TAG
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit


class AlarmScheduler(
    private val context: Context
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(albumName: String, daysBetweenAlarms: Long) {

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALBUM_NAME", albumName)
            putExtra("NEW_ALARM_TIME", daysBetweenAlarms)
        }

        val now = LocalDateTime.now()
        val notificationDate = LocalDateTime.of(now.year, now.month, now.dayOfMonth, now.hour, now.minute, now.second.plus(15))
        val secToRun = ChronoUnit.MILLIS.between(LocalDateTime.now(), notificationDate) / 1000

        Log.d(TAG, "alarm schedule: $albumName $notificationDate and now is $now, sec: $secToRun")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            secToRun,
            PendingIntent.getBroadcast(
                context,
                albumName.hashCode(), // identifier for adding new or updating existing alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    fun reschedule(albumName: String, daysBetweenAlarms: Long, lastAlarmSentOn: LocalDate) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", albumName)
        }
        val now = LocalDate.now()
        val passedDays = ChronoUnit.DAYS.between(lastAlarmSentOn, now)
        val notificationDate = LocalDateTime.of(now.plusDays(daysBetweenAlarms - passedDays), LocalTime.NOON)

        Log.d(TAG, "alarm reschedule: $albumName $notificationDate")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            ChronoUnit.MILLIS.between(now, notificationDate),
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
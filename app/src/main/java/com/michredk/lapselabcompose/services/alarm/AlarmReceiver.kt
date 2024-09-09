package com.michredk.lapselabcompose.services.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.michredk.lapselabcompose.NOTIFICATION_CHANNEL
import com.michredk.lapselabcompose.R
import com.michredk.lapselabcompose.TAG
import java.time.LocalTime


class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val ctx = context ?: throw IllegalStateException("Context cannot be null")
        val albumName = intent?.getStringExtra("ALBUM_NAME") ?: return
        val daysBetween = intent.getLongExtra("DAYS_BETWEEN", 0)
        val hour = intent.getIntExtra("HOUR", -1)
        val minute = intent.getIntExtra("MINUTE", -1)

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL)
            .setContentText("Message")
            .setContentTitle("Reminder")
            .setSmallIcon(R.drawable.logofinal)
            .build()
        notificationManager.notify(1, notification)

        val alarmScheduler = AlarmScheduler(context)
        if (hour == -1){
            alarmScheduler.schedule(albumName, daysBetween)
        } else {
            alarmScheduler.schedule(albumName, daysBetween, LocalTime.of(hour, minute))
        }
    }
}
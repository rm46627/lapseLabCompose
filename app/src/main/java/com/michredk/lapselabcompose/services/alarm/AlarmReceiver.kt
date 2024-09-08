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


class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "alarm Received ")
        val ctx = context ?: throw IllegalStateException("Context cannot be null")
        Log.d(TAG, "extra reveived 1")
        val albumName = intent?.getStringExtra("ALBUM_NAME") ?: return
        Log.d(TAG, "extra reveived 2")
        val daysBetween = intent.getLongExtra("DAYS_BETWEEN", 0)
        Log.d(TAG, "extra reveived 3")

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL)
            .setContentText("Message")
            .setContentTitle("Reminder")
            .setSmallIcon(R.drawable.logofinal)
            .build()
        notificationManager.notify(1, notification)

        val alarmScheduler = AlarmScheduler(context)
        alarmScheduler.schedule(albumName, daysBetween)
    }
}
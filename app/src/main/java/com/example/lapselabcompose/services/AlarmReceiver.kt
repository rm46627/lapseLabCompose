package com.example.lapselabcompose.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.lapselabcompose.NOTIFICATION_CHANNEL
import com.example.lapselabcompose.R
import com.example.lapselabcompose.TAG


class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val ctx = context ?: throw IllegalStateException("Context cannot be null")
        val albumName = intent?.getStringExtra("ALBUM_NAME") ?: return
        val daysBetween = intent.getStringExtra("DAYS_BETWEEN") ?: return

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL)
            .setContentText("Message")
            .setContentTitle("Reminder")
            .setSmallIcon(R.drawable.logofinal)
            .build()
        notificationManager.notify(1, notification)

        val alarmScheduler = AlarmScheduler(context)
        alarmScheduler.schedule(albumName, daysBetween.toLong())
    }
}
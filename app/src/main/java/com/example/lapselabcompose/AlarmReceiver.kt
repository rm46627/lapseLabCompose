package com.example.lapselabcompose

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import okhttp3.internal.notify


class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val ctx = context ?: throw IllegalStateException("Context cannot be null")

        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificaton = NotificationCompat.Builder(ctx, "channel_id")
            .setContentText(message)
            .setContentTitle("Reminder")
            .setSmallIcon(R.drawable.logofinal)
            .build()
        notificationManager.notify(1, notificaton)

        // TODO: shedule new alarm after

    }
}
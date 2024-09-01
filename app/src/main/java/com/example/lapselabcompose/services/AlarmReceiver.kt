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
        Log.d(TAG, "onReceive")

        val ctx = context ?: throw IllegalStateException("Context cannot be null")
        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificaton = NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL)
            .setContentText(message)
            .setContentTitle("Reminder")
            .setSmallIcon(R.drawable.logofinal)
            .build()
        notificationManager.notify(1, notificaton)
        Log.d(TAG,"notified: $message")
        // TODO: shedule new alarm after

    }
}
package com.michredk.lapselab.services.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.michredk.database.Repository
import com.michredk.lapselab.NOTIFICATION_CHANNEL
import com.michredk.lapselab.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// TODO: add button to notification to send reminder again in one hour

@AndroidEntryPoint
class AlarmReceiver: BroadcastReceiver() {

    @Inject
    lateinit var repository: Repository

    override fun onReceive(context: Context?, intent: Intent?) = goAsync {
        val ctx = context ?: throw IllegalStateException("Context cannot be null")
        val albumName = intent?.getStringExtra("ALBUM_NAME") ?: return@goAsync
        val daysBetween = intent.getLongExtra("DAYS_BETWEEN", 1)
        val hour = intent.getIntExtra("HOUR", 12)
        val minute = intent.getIntExtra("MINUTE", 0)

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL)
            .setContentText("It's your reminder to take a new photo for \"$albumName\".")
            .setContentTitle("Your album is waiting!")
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.logo_icon_light))
            .setSmallIcon(R.drawable.logo_icon_light)
            .build()
        notificationManager.notify(1, notification)

        val album = repository.getAlbum(albumName).first()
        val now = LocalDateTime.now()
        repository.updateAlbum(album.copy(lastReminderSentOn = now))

        val alarmScheduler = AlarmScheduler(context)
        alarmScheduler.schedule(albumName, daysBetween, LocalTime.of(hour, minute), lastReminderSentOn = now)
    }
}

fun BroadcastReceiver.goAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    @OptIn(DelicateCoroutinesApi::class) // Must run globally; there's no teardown callback.
    GlobalScope.launch(context) {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}
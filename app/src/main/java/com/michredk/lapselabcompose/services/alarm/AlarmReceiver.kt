package com.michredk.lapselabcompose.services.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.core.app.NotificationCompat
import com.michredk.database.Repository
import com.michredk.lapselabcompose.NOTIFICATION_CHANNEL
import com.michredk.lapselabcompose.R
import com.michredk.lapselabcompose.TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val daysBetween = intent.getLongExtra("DAYS_BETWEEN", 0)
        val hour = intent.getIntExtra("HOUR", -1)
        val minute = intent.getIntExtra("MINUTE", -1)

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL)
            .setContentText("It's your reminder to take a new photo for \"$albumName\".")
            .setContentTitle("Your album is waiting!")
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.logo_icon_light))
            .setSmallIcon(R.drawable.logo_icon_light)
            .build()
        notificationManager.notify(1, notification)

        val album = repository.getAlbum(albumName).first()
        repository.updateAlbum(album.copy(lastReminderSentOn = LocalDateTime.now()))

        val alarmScheduler = AlarmScheduler(context)
        if (hour == -1){
            alarmScheduler.schedule(albumName, daysBetween)
        } else {
            alarmScheduler.schedule(albumName, daysBetween, LocalTime.of(hour, minute))
        }
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
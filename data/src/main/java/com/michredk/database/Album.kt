package com.michredk.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
data class Album (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "directory_name") val directoryName: String = "",
    @ColumnInfo(name = "cover_photo_path") var coverPhotoPath: String = "",
    @ColumnInfo(name = "video_width") val videoWidth: Int? = null,
    @ColumnInfo(name = "video_height") val videoHeight: Int? = null,
    @ColumnInfo(name = "photo_count") val photoCount: Int = 1,
    @ColumnInfo(name = "video_frames_per_image") val videoFramesPerImage: Int = 0,
    @ColumnInfo(name = "video_bitrate") val videoBitrate: Int = 1500000,
    @ColumnInfo(name = "days_between_reminders") val daysBetweenReminders: Long = 0,
    @ColumnInfo(name = "reminder_time") val reminderTime: LocalTime = LocalTime.now(),
    @ColumnInfo(name = "last_reminder_sent_on") val lastReminderSentOn: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "streak") val streak: Int = 1
)
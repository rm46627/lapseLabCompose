package com.example.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
data class Album (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "directory_name") val directoryName: String = "",
    @ColumnInfo(name = "days_between_reminders") val daysBetweenReminders: Long = 0,
    @ColumnInfo(name = "last_reminder_sent_on") val lastReminderSentOn: LocalDate = LocalDate.now(),
    @ColumnInfo(name = "cover_photo_path") var coverPhotoPath: String = "",
    @ColumnInfo(name = "video_width") val videoWidth: Int? = null,
    @ColumnInfo(name = "video_height") val videoHeight: Int? = null
)
package com.example.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Album (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "directory_name") val directoryName: String = "",
    @ColumnInfo(name = "cover_photo_path") val coverPhotoPath: String = "",
    @ColumnInfo(name = "video_width") val videoWidth: Int? = null,
    @ColumnInfo(name = "video_height") val videoHeight: Int? = null
)
package com.michredk.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Album::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LapseDatabase: RoomDatabase() {
    abstract fun albumDao(): AlbumDao
}
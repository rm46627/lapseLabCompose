package com.example.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Album::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LapseDatabase: RoomDatabase() {
    abstract fun albumDao(): AlbumDao
}
package com.example.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Album::class],
    version = 1,
    exportSchema = false
)
abstract class LapseDatabase: RoomDatabase() {
    abstract fun albumDao(): AlbumDao
}
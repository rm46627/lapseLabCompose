package com.michredk.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


class Converters {

    @TypeConverter
    fun toDate(dateString: String?): LocalDate? {
        return if (dateString == null) {
            null
        } else {
            LocalDate.parse(dateString)
        }
    }

    @TypeConverter
    fun toDateString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return if (dateTimeString == null) {
            null
        } else {
            LocalDateTime.parse(dateTimeString)
        }
    }

    @TypeConverter
    fun toLocalDateTimeString(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return if (timeString == null) {
            null
        } else {
            LocalTime.parse(timeString)
        }
    }

    @TypeConverter
    fun toLocalTimeString(time: LocalTime?): String? {
        return time?.toString()
    }
}
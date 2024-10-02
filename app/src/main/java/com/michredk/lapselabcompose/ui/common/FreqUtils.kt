package com.michredk.lapselabcompose.ui.common

import com.michredk.lapselab.files.FILES_NAME_DATE_FORMAT
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object FreqUtils {
    fun frequencyIsValid(value: String): Boolean {
        if (value.isEmpty()) return false
        val patterns = listOf(
            "I don't need a reminder",
            "Everyday",
            "Every\\s+\\d+\\s+days",
            "Once\\s+a\\s+week",
            "Every\\s+\\d+\\s+weeks",
            "Once\\s+a\\s+month",
            "Every\\s+\\d+\\s+months"
        )
        val cleanedValue = value.trim().replace("\\s+".toRegex(), " ")
        val isValid = patterns.any { cleanedValue.matches(it.toRegex(RegexOption.IGNORE_CASE)) }
        return isValid
    }

//    "I don't need a reminder",
//    "Everyday",
//    "Every 2 days",
//    "Once a week",
//    "Every 3 weeks",
//    "Once a month",
//    "Every 6 months"

    fun freqStrToDays(freq: String): Long {
        val words = freq.split(" ")
        val timeMap = mapOf(
            "day" to 1L,
            "days" to 1L,
            "week" to 7L,
            "weeks" to 7L,
            "month" to 30L,
            "months" to 30L
        )
        return when (words.size) {
            1 -> 1
            3 -> {
                val timeUnit = timeMap[words[2]] ?: 0
                val days = if (words[0] == "Once") {
                    timeUnit
                } else timeUnit * words[1].toLong()
                days
            }
            else -> 0
        }
    }

    fun filePathToLocalDateTime(photoPath: String): LocalDateTime {
        val dateFormat = DateTimeFormatter.ofPattern(FILES_NAME_DATE_FORMAT, Locale.US)
        val filename = photoPath.dropLast(4).substring(photoPath.lastIndexOf('/')+1) // Example of a file name representing a date
        val localDateTime = LocalDateTime.parse(filename, dateFormat)
        return localDateTime
    }

    fun localDateToDaysPassed(date: LocalDateTime): Long {
        val inputDate = date.toLocalDate()
        return ChronoUnit.DAYS.between(inputDate, LocalDate.now())
    }

    fun filePathToFormatedDateTime(photoPath: String): String {
        val localDateTime = filePathToLocalDateTime(photoPath)
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        return localDateTime.format(formatter)
    }
}
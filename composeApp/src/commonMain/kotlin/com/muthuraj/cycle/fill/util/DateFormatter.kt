/* $Id$ */
package com.muthuraj.cycle.fill.util

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toLocalDateTime

/**
 * Created by Muthuraj on 08/12/24.
 */
fun Timestamp.toDate(): String {
    val date = this
    val instant = Instant.fromEpochSeconds(date.seconds, date.nanoseconds)
    val localDate = instant.toLocalDateTime(timeZone)
    val monthName = localDate.month.name.substring(0, 3).lowercase().capitalize()
    return "$monthName ${localDate.dayOfMonth}, ${localDate.year}"
}

fun String.toDate(): String {
    val localDate = LocalDate.parse(this)
    val monthName = localDate.month.name.substring(0, 3).lowercase().capitalize()
    return "$monthName ${localDate.dayOfMonth}, ${localDate.year}"
}

fun Timestamp.toDateWithDayName(): Pair<String, String> {
    val date = this
    val instant = Instant.fromEpochSeconds(date.seconds, date.nanoseconds)
    val localDate = instant.toLocalDateTime(timeZone)
    val monthName = localDate.month.name.substring(0, 3).lowercase().capitalize()
    return "$monthName ${localDate.dayOfMonth}, ${localDate.year}" to localDate.dayOfWeek.name.lowercase()
        .capitalize()
}

fun String.toDateWithDayName(): Pair<String, String> {
    val localDate = LocalDate.parse(this)
    val monthName = localDate.month.name.substring(0, 3).lowercase().capitalize()
    return "$monthName ${localDate.dayOfMonth}, ${localDate.year}" to localDate.dayOfWeek.name.lowercase()
        .capitalize()
}

fun Timestamp.toSQLDate(): String {
    val date = this
    val instant = Instant.fromEpochSeconds(date.seconds, date.nanoseconds)
    val localDate = instant.toLocalDateTime(timeZone)
    val month = if (localDate.monthNumber < 10) {
        "0${localDate.monthNumber}"
    } else {
        "${localDate.monthNumber}"
    }
    val day = if (localDate.dayOfMonth < 10) {
        "0${localDate.dayOfMonth}"
    } else {
        "${localDate.dayOfMonth}"
    }
    return "${localDate.year}-$month-$day"
}

fun String.capitalize() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

fun Timestamp.getDaysElapsed(latestInstant: Instant = Clock.System.now()): Pair<Int, String> {
    val instant = Instant.fromEpochSeconds(this.seconds, this.nanoseconds)
    val days = instant.daysUntil(latestInstant, timeZone)
    val period = instant.periodUntil(latestInstant, timeZone)
    val formattedDays = buildString {
        if (period.years > 0) {
            append("${period.years} year ")
        }
        if (period.months > 0) {
            append("${period.months} month ")
        }
        if (period.days > 0) {
            append("${period.days}")
            if (period.days == 1) {
                append(" day")
            } else {
                append(" days")
            }
        }
    }
    return days to formattedDays
}

fun String.getDaysElapsed(latestInstant: Instant = Clock.System.now()): Pair<Int, String> {
    val localDate = LocalDate.parse(this)
    val currentDate = latestInstant.toLocalDateTime(timeZone).date
    return localDate.getDaysElapsed(currentDate)
}

fun LocalDate.getDaysElapsed(latestDate: LocalDate): Pair<Int, String> {
    val days = daysUntil(latestDate)
    val period = periodUntil(latestDate)
    val formattedDays = buildString {
        if (period.years > 0) {
            append("${period.years} year ")
        }
        if (period.months > 0) {
            append("${period.months} month ")
        }
        if (period.days > 0) {
            append("${period.days}")
            if (period.days == 1) {
                append(" day")
            } else {
                append(" days")
            }
        }
    }
    return days to formattedDays
}

fun Timestamp.getDaysElapsedUntil(latestTimestamp: Timestamp): Pair<Int, String> {
    val latestInstant =
        Instant.fromEpochSeconds(latestTimestamp.seconds, latestTimestamp.nanoseconds)
    return getDaysElapsed(latestInstant = latestInstant)
}

fun String.getDaysElapsedUntil(latestTimestamp: String): Pair<Int, String> {
    return LocalDate.parse(this).getDaysElapsed(LocalDate.parse(latestTimestamp))
}

fun Timestamp.daysAgoFrom(greaterTimeStamp: Timestamp): Int {
    val smallerInstant = Instant.fromEpochSeconds(this.seconds, this.nanoseconds)
    val greaterInstant =
        Instant.fromEpochSeconds(greaterTimeStamp.seconds, greaterTimeStamp.nanoseconds)
    return smallerInstant.daysUntil(greaterInstant, timeZone)
}

fun Timestamp.toStringKey() = "${this.seconds}_${this.nanoseconds}"

fun String.toTimestamp(): Timestamp {
    val split = this.split("_")
    return Timestamp(split[0].toLong(), split[1].toInt())
}

private val timeZone = TimeZone.currentSystemDefault()
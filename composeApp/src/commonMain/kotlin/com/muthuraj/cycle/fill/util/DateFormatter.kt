/* $Id$ */
package com.muthuraj.cycle.fill.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toLocalDateTime

/**
 * Created by Muthuraj on 08/12/24.
 */

fun String.toDate(): String {
    val localDate = LocalDate.parse(this)
    val monthName = localDate.month.name.substring(0, 3).lowercase().capitalize()
    return "$monthName ${localDate.dayOfMonth}, ${localDate.year}"
}


fun String.toDateWithDayName(): Pair<String, String> {
    val localDate = LocalDate.parse(this)
    val monthName = localDate.month.name.substring(0, 3).lowercase().capitalize()
    return "$monthName ${localDate.dayOfMonth}, ${localDate.year}" to localDate.dayOfWeek.name.lowercase()
        .capitalize()
}

fun String.capitalize() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

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

fun String.getDaysElapsedUntil(latestTimestamp: String): Pair<Int, String> {
    return LocalDate.parse(this).getDaysElapsed(LocalDate.parse(latestTimestamp))
}

private val timeZone = TimeZone.currentSystemDefault()
package com.example.gymtracker.utility

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Instant.toDateAndTimeString(pattern: String = "EEE, d MMM yyyy HH:mm"): String =
    this.formatter(pattern)

fun Instant.toDateString(pattern: String = "EEE, d MMM yyyy"): String =
    this.formatter(pattern)

private val zoneId = ZoneId.systemDefault()

private fun Instant.formatter(pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
    return formatter.format(this)
}

fun Duration.toReadableString(): String {
    val hours = toHours()
    val minutes = toMinutes() % 60
    val seconds = seconds % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0) append("${minutes}m ")
        append("${seconds}s")
    }.trim()
}
package com.example.gymtracker.utility

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Instant.toDateAndTimeString(pattern: String = "EEE, d MMM yyyy HH:mm"): String =
    this.formatter(pattern)

private val zoneId = ZoneId.systemDefault()

private fun Instant.formatter(pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
    return formatter.format(this)
}
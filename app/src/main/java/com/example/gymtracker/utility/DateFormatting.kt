package com.example.gymtracker.utility

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Instant.toDateAndTimeString(pattern: String = "HH:mm EEE, d MMM yy"): String =
    this.formatter(pattern)

private val zoneId = ZoneId.systemDefault()

private fun Instant.formatter(pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
    return formatter.format(this)
}
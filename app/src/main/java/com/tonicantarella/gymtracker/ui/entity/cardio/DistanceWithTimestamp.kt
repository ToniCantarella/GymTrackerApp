package com.tonicantarella.gymtracker.ui.entity.cardio

import java.time.Instant

data class DistanceWithTimestamp(
    val value: Double?,
    val timestamp: Instant? = null
)

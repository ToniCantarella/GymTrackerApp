package com.example.gymtracker.ui.entity.cardio

import java.time.Duration
import java.time.Instant

data class DurationWithTimestamp(
    val value: Duration?,
    val timestamp: Instant?
)

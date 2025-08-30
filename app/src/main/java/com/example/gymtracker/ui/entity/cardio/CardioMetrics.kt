package com.example.gymtracker.ui.entity.cardio

import java.time.Duration
import java.time.Instant

data class CardioMetrics(
    val steps: Int?,
    val distance: Double?,
    val duration: Duration?,
    val timestamp: Instant? = null
)
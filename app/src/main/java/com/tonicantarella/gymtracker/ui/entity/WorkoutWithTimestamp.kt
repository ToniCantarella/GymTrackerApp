package com.tonicantarella.gymtracker.ui.entity

import java.time.Instant

data class WorkoutWithTimestamp(
    val id: Int,
    val name: String,
    val timestamp: Instant?
)
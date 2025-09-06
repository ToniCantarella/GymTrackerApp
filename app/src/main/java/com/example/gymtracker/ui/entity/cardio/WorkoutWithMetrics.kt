package com.example.gymtracker.ui.entity.cardio

import java.time.Instant

data class WorkoutWithMetrics (
    val id: Int,
    val name: String,
    val timestamp: Instant?,
    val metrics: CardioMetrics
)
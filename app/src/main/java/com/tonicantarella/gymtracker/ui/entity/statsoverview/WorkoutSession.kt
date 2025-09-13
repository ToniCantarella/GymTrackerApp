package com.tonicantarella.gymtracker.ui.entity.statsoverview

import java.time.Instant

data class WorkoutSession(
    val sessionId : Int,
    val workoutId: Int,
    val workoutName: String,
    val timestamp: Instant,
    val type: WorkoutType
)

enum class WorkoutType {
    GYM,
    CARDIO
}
package com.example.gymtracker.ui.entity

import java.time.Instant

data class WorkoutSession(
    val id : Int,
    val workoutId: Int,
    val timestamp: Instant
)
package com.tonicantarella.gymtracker.ui.entity.gym

import java.time.Instant

data class WorkoutWithExercises(
    val id: Int,
    val name: String,
    val timestamp: Instant?,
    val exercises: List<Exercise>
)
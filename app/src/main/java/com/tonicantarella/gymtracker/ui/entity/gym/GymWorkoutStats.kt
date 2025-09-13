package com.tonicantarella.gymtracker.ui.entity.gym


data class GymWorkoutStats(
    val id: Int,
    val name: String,
    val exercises: List<ExerciseWithHistory>
)
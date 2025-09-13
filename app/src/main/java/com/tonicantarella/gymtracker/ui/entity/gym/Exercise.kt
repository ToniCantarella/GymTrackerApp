package com.tonicantarella.gymtracker.ui.entity.gym

import java.util.UUID

data class Exercise(
    val uuid: UUID,
    val name: String,
    val description: String?,
    val sets: List<WorkoutSet>
) {
    companion object {
        fun emptyExercise() = Exercise(
            uuid = UUID.randomUUID(),
            name = "",
            description = null,
            sets = listOf(
                WorkoutSet.emptySet()
            )
        )
    }
}
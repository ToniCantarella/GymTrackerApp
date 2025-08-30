package com.example.gymtracker.ui.entity.gym

import java.util.UUID

data class WorkoutSet(
    val uuid: UUID,
    val checked: Boolean = false,
    val weight: Double,
    val repetitions: Int
) {
    companion object{
        fun emptySet(): WorkoutSet = WorkoutSet(
            uuid = UUID.randomUUID(),
            weight = 0.0,
            repetitions = 0
        )
    }
}
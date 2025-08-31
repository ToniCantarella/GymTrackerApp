package com.example.gymtracker.ui.entity.gym

import java.util.UUID

data class WorkoutSet(
    val uuid: UUID,
    val weight: Double,
    val repetitions: Int,
    val checked: Boolean = false
) {
    companion object{
        fun emptySet(): WorkoutSet = WorkoutSet(
            uuid = UUID.randomUUID(),
            weight = 0.0,
            repetitions = 0
        )
    }
}
package com.tonicantarella.gymtracker.ui.entity.statsoverview

import java.time.Duration

data class GymWorkoutWithGeneralStats (
    val id: Int,
    val name: String,
    val exerciseCount: Int,
    val avgWeight: Double,
    val avgSets: Int,
    val avgReps: Int,
    val avgDuration: Duration
)
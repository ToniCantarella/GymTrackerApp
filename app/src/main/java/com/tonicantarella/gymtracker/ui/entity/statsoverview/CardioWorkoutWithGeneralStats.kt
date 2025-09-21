package com.tonicantarella.gymtracker.ui.entity.statsoverview

import java.time.Duration

data class CardioWorkoutWithGeneralStats(
    val id: Int,
    val name: String,
    val avgSteps: Int,
    val avgDuration: Duration,
    val avgDistance: Double
)

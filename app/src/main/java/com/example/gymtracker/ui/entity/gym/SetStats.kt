package com.example.gymtracker.ui.entity.gym

import java.time.Instant

data class SetStats(
    val minWeight: Double,
    val maxWeight: Double,
    val minRepetitions: Int,
    val maxRepetitions: Int,
    val timestamp: Instant
)
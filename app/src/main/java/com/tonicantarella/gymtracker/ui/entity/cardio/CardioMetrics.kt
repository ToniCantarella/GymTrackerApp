package com.tonicantarella.gymtracker.ui.entity.cardio

import java.time.Duration

data class CardioMetrics(
    val steps: Int?,
    val distance: Double?,
    val duration: Duration?
)
package com.example.gymtracker.ui.entity.cardio

import java.time.Instant

data class StepsWithTimestamp(
    val value: Int?,
    val timestamp: Instant?
)
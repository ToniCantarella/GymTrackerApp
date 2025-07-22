package com.example.gymtracker.ui.cardio.entity

import java.time.Duration
import java.time.Instant

data class Cardio (
    val name: String,
    val steps: Int?,
    val stepsTimestamp: Instant?,
    val distance: Double?,
    val distanceTimestamp: Instant?,
    val duration: Duration?,
    val durationTimestamp: Instant?,
    val latestTimestamp: Instant?
) {
    companion object {
        fun emptyCardio() = Cardio(
            name = "",
            steps = 0,
            stepsTimestamp = null,
            distance = 0.0,
            distanceTimestamp = null,
            duration = Duration.ofMillis(0L),
            durationTimestamp = null,
            latestTimestamp = null
        )
    }
}
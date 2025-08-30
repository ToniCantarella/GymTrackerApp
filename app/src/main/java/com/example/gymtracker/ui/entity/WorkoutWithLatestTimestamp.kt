package com.example.gymtracker.ui.entity

import java.time.Instant

data class WorkoutWithLatestTimestamp(
    val id: Int,
    val name: String,
    val latestTimestamp: Instant?,
    val selected: Boolean = false
)
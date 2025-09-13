package com.tonicantarella.gymtracker.ui.entity.cardio

data class CardioWorkoutStats (
    val id: Int,
    val name: String,
    val stepsHistory : List<StepsWithTimestamp>,
    val distanceHistory : List<DistanceWithTimestamp>,
    val durationHistory : List<DurationWithTimestamp>
)
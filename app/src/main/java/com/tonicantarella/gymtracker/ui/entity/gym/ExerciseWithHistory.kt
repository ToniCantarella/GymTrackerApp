package com.tonicantarella.gymtracker.ui.entity.gym

data class ExerciseWithHistory(
    val name: String,
    val setHistory: List<SetStats>
)
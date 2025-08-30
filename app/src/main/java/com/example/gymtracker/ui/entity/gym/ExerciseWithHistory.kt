package com.example.gymtracker.ui.entity.gym

data class ExerciseWithHistory(
    val name: String,
    val setHistory: List<SetStats>
)
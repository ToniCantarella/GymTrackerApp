package com.example.gymtracker.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WorkoutType {
    GYM,
    CARDIO
}

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: WorkoutType
)
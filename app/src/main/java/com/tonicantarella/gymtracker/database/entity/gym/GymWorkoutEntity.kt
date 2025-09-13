package com.tonicantarella.gymtracker.database.entity.gym

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "GymWorkouts")
data class GymWorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
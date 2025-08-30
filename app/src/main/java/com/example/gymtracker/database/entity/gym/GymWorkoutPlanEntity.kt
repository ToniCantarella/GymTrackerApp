package com.example.gymtracker.database.entity.gym

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "GymWorkoutPlans")
data class GymWorkoutPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
package com.example.gymtracker.database.entity.cardio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CardioWorkouts")
data class CardioWorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
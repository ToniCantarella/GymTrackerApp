package com.tonicantarella.gymtracker.database.entity.gym

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "gym_sessions",
    foreignKeys = [ForeignKey(
        entity = GymWorkoutEntity::class,
        parentColumns = ["id"],
        childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("workoutId"),
        Index("timestamp")
    ]
)
data class GymSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workoutId: Int,
    val timestamp: Instant
)
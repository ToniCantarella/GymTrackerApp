package com.tonicantarella.gymtracker.database.entity.cardio

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.Instant

@Entity(
    tableName = "cardio_sessions",
    foreignKeys = [ForeignKey(
        entity = CardioWorkoutEntity::class,
        parentColumns = ["id"],
        childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("workoutId"),
        Index("timestamp")
    ]
)
data class CardioSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workoutId: Int,
    val timestamp: Instant,
    val steps: Int? = null,
    val distance: Double? = null,
    val duration: Duration? = null
)

data class AverageCardioStats(
    val avgDistance: Double?,
    val avgSteps: Double?,
    val avgDurationMillis: Long?
)
package com.example.gymtracker.database.entity.cardio

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.Instant

@Entity(
    tableName = "cardios",
    foreignKeys = [ForeignKey(
        entity = CardioWorkoutPlan::class,
        parentColumns = ["id"],
        childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("workoutId")
    ]
)
data class CardioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workoutId: Int = 0,
    val steps: Int? = null,
    val distance: Double? = null,
    val duration: Duration? = null
)

@Entity(
    tableName = "cardio_sessions",
    foreignKeys = [ForeignKey(
        entity = CardioEntity::class,
        parentColumns = ["id"],
        childColumns = ["cardioId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("cardioId"),
        Index("timestamp")
    ]
)
data class CardioSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cardioId: Int,
    val timestamp: Instant,
    val steps: Int? = null,
    val distance: Double? = null,
    val duration: Duration? = null
)

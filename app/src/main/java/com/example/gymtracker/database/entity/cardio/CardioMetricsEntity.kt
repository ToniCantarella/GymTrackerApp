package com.example.gymtracker.database.entity.cardio

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Duration

@Entity(
    tableName = "cardio_metrics",
    foreignKeys = [ForeignKey(
        entity = CardioWorkoutPlanEntity::class,
        parentColumns = ["id"],
        childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("workoutId")
    ]
)
data class CardioMetricsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workoutId: Int = 0,
    val steps: Int? = null,
    val distance: Double? = null,
    val duration: Duration? = null
)
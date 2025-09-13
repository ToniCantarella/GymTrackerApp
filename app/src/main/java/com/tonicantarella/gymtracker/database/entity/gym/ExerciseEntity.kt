package com.tonicantarella.gymtracker.database.entity.gym

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import java.util.UUID

@Entity(
    tableName = "exercises",
    foreignKeys = [ForeignKey(
        entity = GymWorkoutEntity::class,
        parentColumns = ["id"],
        childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("workoutId")]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workoutId: Int,
    @Contextual
    val uuid: UUID,
    val name: String,
    val description: String?
)
package com.example.gymtracker.database.entity.gym

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import java.util.UUID

@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId")]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val exerciseId: Int,
    @Contextual
    val uuid: UUID,
    val weight: Double,
    val repetitions: Int
)

@Entity(
    tableName = "set_sessions",
    foreignKeys = [
        ForeignKey(
            entity = SetEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GymSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("setId"),
        Index("sessionId"),
        Index(value = ["setId", "sessionId"])
    ]
)
data class SetSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val setId: Int,
    val sessionId: Int,
    @Contextual
    val uuid: UUID,
    val weight: Double,
    val repetitions: Int
)
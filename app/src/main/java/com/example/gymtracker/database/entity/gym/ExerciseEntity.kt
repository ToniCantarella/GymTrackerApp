package com.example.gymtracker.database.entity.gym

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import java.util.UUID

@Entity(
    tableName = "exercises",
    foreignKeys = [ForeignKey(
        entity = SplitEntity::class,
        parentColumns = ["id"],
        childColumns = ["splitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("splitId")]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val splitId: Int,
    @Contextual
    val uuid: UUID,
    val name: String,
    val description: String?
)
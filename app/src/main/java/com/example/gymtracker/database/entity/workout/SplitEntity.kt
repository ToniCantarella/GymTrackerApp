package com.example.gymtracker.database.entity.workout

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "splits")
data class SplitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

@Entity(
    tableName = "split_sessions",
    foreignKeys = [ForeignKey(
        entity = SplitEntity::class,
        parentColumns = ["id"],
        childColumns = ["splitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("splitId"),
        Index("timestamp")
    ]
)
data class SplitSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val splitId: Int,
    val timestamp: Instant
)
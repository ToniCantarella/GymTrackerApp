package com.example.gymtracker.database.entity.cardio

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.Instant

@Entity(tableName = "cardios")
data class CardioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val latestTimestamp: Instant?
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
    val steps: Int?,
    val distance: Double?,
    val duration: Duration?
)

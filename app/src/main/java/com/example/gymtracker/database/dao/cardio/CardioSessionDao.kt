package com.example.gymtracker.database.dao.cardio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gymtracker.database.entity.cardio.CardioSessionEntity
import java.time.Instant

@Dao
interface CardioSessionDao {
    @Insert
    suspend fun insert(session: CardioSessionEntity): Long

    @Query("SELECT * FROM cardio_sessions")
    suspend fun getAllSessions(): List<CardioSessionEntity?>

    @Query("SELECT * FROM cardio_sessions WHERE id = :id")
    suspend fun getById(id: Int): CardioSessionEntity

    @Query("SELECT * FROM cardio_sessions WHERE workoutId = :cardioId ORDER BY timestamp ASC")
    suspend fun getAllSessionsForCardio(cardioId: Int): List<CardioSessionEntity?>

    @Query("SELECT * FROM cardio_sessions WHERE workoutId = :cardioId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSession(cardioId: Int): CardioSessionEntity?

    @Query("SELECT * FROM cardio_sessions WHERE timestamp BETWEEN :start AND :end")
    suspend fun getSessionsForTimespan(start: Instant, end: Instant): List<CardioSessionEntity?>
}
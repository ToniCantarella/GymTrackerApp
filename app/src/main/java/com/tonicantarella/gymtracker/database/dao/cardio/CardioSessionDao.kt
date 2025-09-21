package com.tonicantarella.gymtracker.database.dao.cardio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tonicantarella.gymtracker.database.entity.cardio.AverageCardioStats
import com.tonicantarella.gymtracker.database.entity.cardio.CardioSessionEntity
import java.time.Instant

@Dao
interface CardioSessionDao {
    @Insert
    suspend fun insert(session: CardioSessionEntity): Long

    @Query("SELECT * FROM cardio_sessions")
    suspend fun getAllSessions(): List<CardioSessionEntity>?

    @Query("SELECT * FROM cardio_sessions WHERE id = :id")
    suspend fun getById(id: Int): CardioSessionEntity

    @Query("SELECT * FROM cardio_sessions WHERE workoutId = :workoutId ORDER BY timestamp ASC")
    suspend fun getAllSessionsForCardio(workoutId: Int): List<CardioSessionEntity>?

    @Query("SELECT * FROM cardio_sessions WHERE workoutId = :workoutId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSession(workoutId: Int): CardioSessionEntity?

    @Query("SELECT * FROM cardio_sessions WHERE timestamp BETWEEN :start AND :end")
    suspend fun getSessionsForTimespan(start: Instant, end: Instant): List<CardioSessionEntity>?

    @Query("""
        SELECT 
            AVG(cs.distance) as avgDistance,
            AVG(cs.steps) as avgSteps,
            AVG(cs.durationMillis) as avgDurationMillis 
        FROM cardio_sessions cs
        WHERE cs.workoutId = :cardioWorkoutId
    """)
    suspend fun getAverageStatsForCardioWorkout(cardioWorkoutId: Int): AverageCardioStats?

}
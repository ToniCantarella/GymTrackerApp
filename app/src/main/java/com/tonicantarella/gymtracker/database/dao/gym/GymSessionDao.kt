package com.tonicantarella.gymtracker.database.dao.gym

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tonicantarella.gymtracker.database.entity.gym.GymSessionEntity
import java.time.Instant

@Dao
interface GymSessionDao {
    @Insert
    suspend fun insert(session: GymSessionEntity): Long

    @Query("SELECT * FROM gym_sessions WHERE id = :id")
    suspend fun getById(id: Int): GymSessionEntity

    @Query("SELECT * FROM gym_sessions WHERE workoutId = :workoutId ORDER BY timestamp ASC")
    suspend fun getByWorkoutId(workoutId: Int): List<GymSessionEntity>

    @Query("SELECT * FROM gym_sessions")
    suspend fun getAllSessions(): List<GymSessionEntity>?

    @Query("SELECT * FROM gym_sessions WHERE workoutId = :workoutId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSession(workoutId: Int): GymSessionEntity?

    @Query("SELECT * FROM gym_sessions WHERE timestamp BETWEEN :start AND :end")
    suspend fun getSessionsForTimespan(start: Instant, end: Instant): List<GymSessionEntity>?
}
package com.example.gymtracker.database.dao.gym

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gymtracker.database.entity.gym.SetSessionEntity

@Dao
interface SetSessionDao {
    @Insert
    suspend fun insert(set: SetSessionEntity): Long

    @Query("SELECT * FROM set_sessions WHERE sessionId = :sessionId")
    suspend fun getSetsForSession(sessionId: Int): List<SetSessionEntity>
}
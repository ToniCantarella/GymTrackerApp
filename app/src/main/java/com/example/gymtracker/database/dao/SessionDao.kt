package com.example.gymtracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gymtracker.database.entity.workout.SplitSessionEntity

@Dao
interface SplitSessionDao {
    @Insert
    suspend fun insert(session: SplitSessionEntity): Long

    @Query("SELECT * FROM split_sessions WHERE splitId = :splitId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSession(splitId: Int): SplitSessionEntity?
}

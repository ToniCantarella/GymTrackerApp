package com.example.gymtracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gymtracker.database.entity.workout.SplitEntity
import com.example.gymtracker.ui.workouts.splitslist.SplitListItem

@Dao
interface SplitDao {
    @Insert suspend fun insert(split: SplitEntity): Long
    @Query("DELETE FROM splits WHERE id = :splitId") suspend fun deleteById(splitId: Int)
    @Query("SELECT * FROM splits WHERE id = :id") suspend fun getSplitById(id: Int): SplitEntity

    @Query("""
        SELECT split.id AS id, split.name AS name,
        (
            SELECT MAX(session.timestamp)
            FROM split_sessions session
            WHERE session.splitId = split.id
        ) AS latestTimestamp
        FROM splits split
    """)
    suspend fun getSplitsWithLatestSession(): List<SplitListItem>
}
package com.example.gymtracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gymtracker.database.entity.workout.SplitEntity

@Dao
interface SplitDao {
    @Insert
    suspend fun insert(split: SplitEntity): Long

    @Query("SELECT * FROM splits")
    suspend fun getAllSplits(): List<SplitEntity>

    @Query("SELECT * FROM splits WHERE id = :id")
    suspend fun getSplitById(id: Int): SplitEntity

    @Query("DELETE FROM splits WHERE id = :splitId")
    suspend fun deleteById(splitId: Int)
}
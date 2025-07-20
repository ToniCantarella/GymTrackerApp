package com.example.gymtracker.database.dao.gym

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtracker.database.entity.gym.SplitEntity

@Dao
interface SplitDao {
    @Insert
    suspend fun insert(split: SplitEntity): Long

    @Update
    suspend fun updateSplit(split: SplitEntity)

    @Query("SELECT * FROM splits")
    suspend fun getAllSplits(): List<SplitEntity>

    @Query("SELECT * FROM splits WHERE id = :id")
    suspend fun getSplitById(id: Int): SplitEntity

    @Query("DELETE FROM splits WHERE id = :splitId")
    suspend fun deleteById(splitId: Int)
}
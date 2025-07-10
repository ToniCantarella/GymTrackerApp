package com.example.gymtracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtracker.database.entity.workout.SetEntity

@Dao
interface SetDao {
    @Insert
    suspend fun insert(set: SetEntity): Long

    @Update
    suspend fun updateSet(set: SetEntity)

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId")
    suspend fun getSetsForExercise(exerciseId: Int): List<SetEntity>
}
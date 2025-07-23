package com.example.gymtracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtracker.database.entity.WorkoutEntity

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insert(workout: WorkoutEntity): Long

    @Update
    suspend fun updateSplit(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE type = 'GYM'")
    suspend fun getAllSplits(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE type = 'CARDIO'")
    suspend fun getAllCardio(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Int): WorkoutEntity

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteById(workoutId: Int)
}

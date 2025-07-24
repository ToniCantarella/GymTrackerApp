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
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts ORDER BY type DESC")
    suspend fun getAllWorkouts(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE type = 'GYM'")
    suspend fun getAllGymWorkouts(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE type = 'CARDIO'")
    suspend fun getAllCardioWorkouts(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Int): WorkoutEntity

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteById(workoutId: Int)
}

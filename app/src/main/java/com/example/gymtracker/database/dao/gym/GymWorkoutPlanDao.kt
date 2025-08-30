package com.example.gymtracker.database.dao.gym

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtracker.database.entity.gym.GymWorkoutPlanEntity

@Dao
interface GymWorkoutPlanDao {
    @Insert
    suspend fun insert(workout: GymWorkoutPlanEntity): Long

    @Update
    suspend fun update(workout: GymWorkoutPlanEntity)

    @Query("SELECT * FROM GymWorkoutPlans ORDER BY id DESC")
    suspend fun getAll(): List<GymWorkoutPlanEntity>

    @Query("SELECT * FROM GymWorkoutPlans WHERE id = :id")
    suspend fun getById(id: Int): GymWorkoutPlanEntity?

    @Query("DELETE FROM GymWorkoutPlans WHERE id = :id")
    suspend fun deleteById(id: Int)
}
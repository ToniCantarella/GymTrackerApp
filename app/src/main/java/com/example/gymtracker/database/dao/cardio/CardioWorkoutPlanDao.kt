package com.example.gymtracker.database.dao.cardio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtracker.database.entity.cardio.CardioWorkoutPlan

@Dao
interface CardioWorkoutPlanDao {
    @Insert
    suspend fun insert(workout: CardioWorkoutPlan): Long

    @Update
    suspend fun update(workout: CardioWorkoutPlan)

    @Query("SELECT * FROM CardioWorkoutPlans ORDER BY id DESC")
    suspend fun getAll(): List<CardioWorkoutPlan>

    @Query("SELECT * FROM CardioWorkoutPlans WHERE id = :id")
    suspend fun getById(id: Int): CardioWorkoutPlan?

    @Query("DELETE FROM CardioWorkoutPlans WHERE id = :id")
    suspend fun deleteById(id: Int)
}
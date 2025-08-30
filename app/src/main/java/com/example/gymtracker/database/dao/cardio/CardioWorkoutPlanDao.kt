package com.example.gymtracker.database.dao.cardio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtracker.database.entity.cardio.CardioWorkoutPlanEntity

@Dao
interface CardioWorkoutPlanDao {
    @Insert
    suspend fun insert(workout: CardioWorkoutPlanEntity): Long

    @Update
    suspend fun update(workout: CardioWorkoutPlanEntity)

    @Query("SELECT * FROM CardioWorkoutPlans ORDER BY id DESC")
    suspend fun getAll(): List<CardioWorkoutPlanEntity>

    @Query("SELECT * FROM CardioWorkoutPlans WHERE id = :id")
    suspend fun getById(id: Int): CardioWorkoutPlanEntity?

    @Query("DELETE FROM CardioWorkoutPlans WHERE id = :id")
    suspend fun deleteById(id: Int)
}
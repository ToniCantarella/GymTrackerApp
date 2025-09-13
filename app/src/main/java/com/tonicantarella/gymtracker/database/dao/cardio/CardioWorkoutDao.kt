package com.tonicantarella.gymtracker.database.dao.cardio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tonicantarella.gymtracker.database.entity.cardio.CardioWorkoutEntity

@Dao
interface CardioWorkoutDao {
    @Insert
    suspend fun insert(workout: CardioWorkoutEntity): Long

    @Update
    suspend fun update(workout: CardioWorkoutEntity)

    @Query("SELECT * FROM CardioWorkouts ORDER BY id DESC")
    suspend fun getAll(): List<CardioWorkoutEntity>

    @Query("SELECT * FROM CardioWorkouts WHERE id = :id")
    suspend fun getById(id: Int): CardioWorkoutEntity?

    @Query("DELETE FROM CardioWorkouts WHERE id = :id")
    suspend fun deleteById(id: Int)
}
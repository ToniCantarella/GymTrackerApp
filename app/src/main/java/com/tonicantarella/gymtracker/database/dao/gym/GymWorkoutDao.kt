package com.tonicantarella.gymtracker.database.dao.gym

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tonicantarella.gymtracker.database.entity.gym.GymWorkoutEntity

@Dao
interface GymWorkoutDao {
    @Insert
    suspend fun insert(workout: GymWorkoutEntity): Long

    @Update
    suspend fun update(workout: GymWorkoutEntity)

    @Query("SELECT * FROM GymWorkouts")
    suspend fun getAll(): List<GymWorkoutEntity>

    @Query("SELECT * FROM GymWorkouts WHERE id = :id")
    suspend fun getById(id: Int): GymWorkoutEntity?

    @Query("DELETE FROM GymWorkouts WHERE id = :id")
    suspend fun deleteById(id: Int)
}
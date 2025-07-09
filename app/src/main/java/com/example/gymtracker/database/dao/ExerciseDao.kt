package com.example.gymtracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gymtracker.database.entity.workout.ExerciseEntity

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insert(exercise: ExerciseEntity): Long

    @Query("SELECT * FROM exercises WHERE splitId = :splitId")
    suspend fun getBySplitId(splitId: Int): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getById(exerciseId: Int): ExerciseEntity
}
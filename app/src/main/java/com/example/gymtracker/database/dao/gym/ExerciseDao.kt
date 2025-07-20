package com.example.gymtracker.database.dao.gym

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtracker.database.entity.gym.ExerciseEntity

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insert(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercises(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises WHERE splitId = :splitId")
    suspend fun getExercisesBySplitId(splitId: Int): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExercisesById(exerciseId: Int): ExerciseEntity
}
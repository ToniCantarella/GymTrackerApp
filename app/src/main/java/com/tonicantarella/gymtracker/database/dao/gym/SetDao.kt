package com.tonicantarella.gymtracker.database.dao.gym

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tonicantarella.gymtracker.database.entity.gym.SetEntity

@Dao
interface SetDao {
    @Insert
    suspend fun insert(set: SetEntity): Long

    @Delete
    suspend fun deleteSets(sets: List<SetEntity>)

    @Update
    suspend fun updateSet(set: SetEntity)

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId")
    suspend fun getSetsForExercise(exerciseId: Int): List<SetEntity>
}
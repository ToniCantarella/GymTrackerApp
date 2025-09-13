package com.tonicantarella.gymtracker.database.dao.cardio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tonicantarella.gymtracker.database.entity.cardio.CardioMetricsEntity

@Dao
interface CardioMetricsDao {
    @Insert
    suspend fun insert(cardio: CardioMetricsEntity): Long

    @Query("SELECT * FROM cardio_metrics WHERE id = :id")
    suspend fun getById(id: Int): CardioMetricsEntity

    @Update
    suspend fun updateCardio(cardio: CardioMetricsEntity)

    @Query("SELECT * FROM cardio_metrics")
    suspend fun getAllCardio(): List<CardioMetricsEntity>

    @Query("SELECT * FROM cardio_metrics WHERE workoutId = :workoutId")
    suspend fun getCardioByWorkoutId(workoutId: Int): CardioMetricsEntity

    @Query("DELETE FROM cardio_metrics WHERE id = :id")
    suspend fun deleteById(id: Int)
}
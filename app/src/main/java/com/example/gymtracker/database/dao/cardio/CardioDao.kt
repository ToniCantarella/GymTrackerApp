package com.example.gymtracker.database.dao.cardio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtracker.database.entity.cardio.CardioEntity

@Dao
interface CardioDao {
    @Insert
    suspend fun insert(cardio: CardioEntity): Long

    @Update
    suspend fun updateCardio(cardio: CardioEntity)

    @Query("SELECT * FROM cardios")
    suspend fun getAllCardios(): List<CardioEntity>

    @Query("SELECT * FROM cardios WHERE id = :id")
    suspend fun getCardioById(id: Int): CardioEntity

    @Query("DELETE FROM cardios WHERE id = :id")
    suspend fun deleteById(id: Int)
}
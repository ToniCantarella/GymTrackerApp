package com.example.gymtracker.repository

import com.example.gymtracker.database.GymDatabase

interface AppRepository {
    suspend fun deleteAllData()
}

class AppRepositoryImpl(
    private val db: GymDatabase,
) : AppRepository {

    override suspend fun deleteAllData() = db.clearAllTables()

}
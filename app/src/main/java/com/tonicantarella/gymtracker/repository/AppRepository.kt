package com.tonicantarella.gymtracker.repository

import com.tonicantarella.gymtracker.database.GymDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AppRepository {
    suspend fun deleteAllData()
}

class AppRepositoryImpl(
    private val db: GymDatabase,
) : AppRepository {

    override suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            db.clearAllTables()
        }
    }
}
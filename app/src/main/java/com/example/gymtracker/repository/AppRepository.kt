package com.example.gymtracker.repository

import com.example.gymtracker.database.GymDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface AppRepository {
    suspend fun deleteAllData()
}

class AppRepositoryImpl(
    private val db: GymDatabase,
): AppRepository {

    override suspend fun deleteAllData() {
        CoroutineScope(Dispatchers.IO).launch {
            db.clearAllTables()
        }
    }
}
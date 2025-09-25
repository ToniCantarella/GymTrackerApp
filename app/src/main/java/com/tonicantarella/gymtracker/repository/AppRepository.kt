package com.tonicantarella.gymtracker.repository

import com.tonicantarella.gymtracker.database.GymDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(
    private val db: GymDatabase,
) {
    suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            db.clearAllTables()
        }
    }
}
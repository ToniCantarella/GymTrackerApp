package com.example.gymtracker.repository

import com.example.gymtracker.database.dao.cardio.CardioMetricsDao
import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.cardio.CardioWorkoutDao

interface CardioWorkoutRepository {
    suspend fun getCardioListWithLatestTimestamp(): List<WorkoutWithLatestTimestamp>
}

class CardioWorkoutRepositoryImpl (
    private val workoutDao: CardioWorkoutDao,
    private val metricsDao: CardioMetricsDao,
    private val sessionDao: CardioSessionDao
): CardioWorkoutRepository {
    override suspend fun getCardioListWithLatestTimestamp(): List<WorkoutWithLatestTimestamp> {
        val workouts = workoutDao.getAll()

        return workouts.map {
            val cardio = metricsDao.getCardioByWorkoutId(it.id)
            val session = sessionDao.getLastSession(cardio.id)

            WorkoutWithLatestTimestamp(
                id = it.id,
                name = it.name,
                latestTimestamp = session?.timestamp
            )
        }
    }
}
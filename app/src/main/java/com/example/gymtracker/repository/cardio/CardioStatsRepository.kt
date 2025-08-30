package com.example.gymtracker.repository.cardio

import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.cardio.CardioWorkoutDao
import com.example.gymtracker.ui.entity.cardio.CardioWorkoutStats
import com.example.gymtracker.ui.entity.cardio.DistanceWithTimestamp
import com.example.gymtracker.ui.entity.cardio.DurationWithTimestamp
import com.example.gymtracker.ui.entity.cardio.StepsWithTimestamp

interface CardioStatsRepository {
    suspend fun getWorkoutStats(workoutId: Int): CardioWorkoutStats?
}

class CardioStatsRepositoryImpl(
    private val workoutDao: CardioWorkoutDao,
    private val sessionDao: CardioSessionDao
) : CardioStatsRepository {
    override suspend fun getWorkoutStats(workoutId: Int): CardioWorkoutStats? {
        val workout = workoutDao.getById(workoutId)
        if (workout == null) return null

        val sessions = sessionDao.getAllSessionsForCardio(workoutId)
        return CardioWorkoutStats(
            id = workoutId,
            name = workout.name,
            stepsHistory = sessions.mapNotNull {
                StepsWithTimestamp(
                    value = it?.steps,
                    timestamp = it?.timestamp
                )
            },
            distanceHistory = sessions.mapNotNull {
                DistanceWithTimestamp(
                    value = it?.distance,
                    timestamp = it?.timestamp
                )
            },
            durationHistory = sessions.mapNotNull {
                DurationWithTimestamp(
                    value = it?.duration,
                    timestamp = it?.timestamp
                )
            }
        )
    }
}
package com.tonicantarella.gymtracker.repository.cardio

import com.tonicantarella.gymtracker.database.dao.cardio.CardioSessionDao
import com.tonicantarella.gymtracker.database.dao.cardio.CardioWorkoutDao
import com.tonicantarella.gymtracker.ui.entity.cardio.CardioWorkoutStats
import com.tonicantarella.gymtracker.ui.entity.cardio.DistanceWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.cardio.DurationWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.cardio.StepsWithTimestamp
import java.time.Duration

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
            stepsHistory = sessions?.map {
                StepsWithTimestamp(
                    value = it.steps,
                    timestamp = it.timestamp
                )
            }.orEmpty(),
            distanceHistory = sessions?.map {
                DistanceWithTimestamp(
                    value = it.distance,
                    timestamp = it.timestamp
                )
            }.orEmpty(),
            durationHistory = sessions?.map {
                DurationWithTimestamp(
                    value = if (it.durationMillis != null) Duration.ofMillis(it.durationMillis) else null,
                    timestamp = it.timestamp
                )
            }.orEmpty()
        )
    }
}
package com.example.gymtracker.repository.cardio

import com.example.gymtracker.database.dao.cardio.CardioMetricsDao
import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.cardio.CardioWorkoutDao
import com.example.gymtracker.database.entity.cardio.CardioWorkoutEntity
import com.example.gymtracker.ui.entity.WorkoutWithTimestamp
import com.example.gymtracker.ui.entity.cardio.DistanceWithTimestamp
import com.example.gymtracker.ui.entity.cardio.DurationWithTimestamp
import com.example.gymtracker.ui.entity.cardio.StepsWithTimestamp
import com.example.gymtracker.ui.entity.cardio.WorkoutWithMetrics

interface CardioWorkoutRepository {
    suspend fun getAllWorkouts(): List<WorkoutWithTimestamp>
    suspend fun addWorkout(workoutName: String)
    suspend fun updateWorkout(workoutId: Int, workoutName: String)
    suspend fun getLatestWorkoutWithMetrics(workoutId: Int): WorkoutWithMetrics?
    suspend fun deleteWorkout(workoutId: Int)
}

class CardioWorkoutRepositoryImpl(
    private val workoutDao: CardioWorkoutDao,
    private val metricsDao: CardioMetricsDao,
    private val sessionDao: CardioSessionDao
) : CardioWorkoutRepository {
    override suspend fun getAllWorkouts(): List<WorkoutWithTimestamp> {
        val workouts = workoutDao.getAll()

        return workouts.map {
            val cardio = metricsDao.getCardioByWorkoutId(it.id)
            val session = sessionDao.getLastSession(cardio.id)

            WorkoutWithTimestamp(
                id = it.id,
                name = it.name,
                timestamp = session?.timestamp
            )
        }
    }

    override suspend fun addWorkout(workoutName: String) {
        workoutDao.insert(
            CardioWorkoutEntity(
                name = workoutName.trim()
            )
        )
    }

    override suspend fun updateWorkout(workoutId: Int, workoutName: String) {
        workoutDao.update(
            workoutDao.getById(workoutId)?.copy(
                name = workoutName.trim()
            ) ?: return
        )
    }

    override suspend fun getLatestWorkoutWithMetrics(workoutId: Int): WorkoutWithMetrics? {
        val workout = workoutDao.getById(workoutId)
        val sessions = sessionDao.getAllSessionsForCardio(workoutId)
        val session = sessions.firstOrNull()

        val stepsSession = if (session?.steps == null) {
            sessions.firstOrNull { it?.steps != null }
        } else {
            session
        }
        val distanceSession = if (session?.distance == null) {
            sessions.firstOrNull { it?.distance != null }
        } else {
            session
        }
        val durationSession = if (session?.duration == null) {
            sessions.firstOrNull { it?.duration != null }
        } else {
            session
        }

        return WorkoutWithMetrics(
            id = workoutId,
            name = workout?.name ?: "",
            timestamp = session?.timestamp,
            steps = StepsWithTimestamp(
                value = stepsSession?.steps,
                timestamp = stepsSession?.timestamp
            ),
            distance = DistanceWithTimestamp(
                value = distanceSession?.distance,
                timestamp = distanceSession?.timestamp
            ),
            duration = DurationWithTimestamp(
                value = durationSession?.duration,
                timestamp = durationSession?.timestamp
            )
        )
    }

    override suspend fun deleteWorkout(workoutId: Int) {
        workoutDao.deleteById(workoutId)
    }
}
package com.tonicantarella.gymtracker.repository.cardio

import com.tonicantarella.gymtracker.database.dao.cardio.CardioSessionDao
import com.tonicantarella.gymtracker.database.dao.cardio.CardioWorkoutDao
import com.tonicantarella.gymtracker.database.entity.cardio.CardioWorkoutEntity
import com.tonicantarella.gymtracker.ui.entity.WorkoutWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.cardio.CardioMetrics
import com.tonicantarella.gymtracker.ui.entity.cardio.WorkoutWithMetrics
import com.tonicantarella.gymtracker.utility.UnitUtil.convertDistanceFromDatabase
import java.time.Duration

class CardioWorkoutRepository(
    private val workoutDao: CardioWorkoutDao,
    private val sessionDao: CardioSessionDao
) {
    suspend fun getAllWorkouts(): List<WorkoutWithTimestamp> {
        val workouts = workoutDao.getAll()

        return workouts.map { workout ->
            val session = sessionDao.getLastSession(workout.id)

            WorkoutWithTimestamp(
                id = workout.id,
                name = workout.name,
                timestamp = session?.timestamp
            )
        }
    }

    suspend fun addWorkout(workoutName: String): Int {
        return workoutDao.insert(
            CardioWorkoutEntity(
                name = workoutName.trim()
            )
        ).toInt()
    }

    suspend fun updateWorkout(workoutId: Int, workoutName: String) {
        workoutDao.update(
            workoutDao.getById(workoutId)?.copy(
                name = workoutName.trim()
            ) ?: return
        )
    }

    suspend fun getLatestWorkoutWithMetrics(workoutId: Int): WorkoutWithMetrics? {
        val workout = workoutDao.getById(workoutId)
        val session = sessionDao.getLastSession(workoutId)

        val distance = session?.distance?.convertDistanceFromDatabase() ?: 0.0

        return WorkoutWithMetrics(
            id = workoutId,
            name = workout?.name ?: "",
            timestamp = session?.timestamp,
            metrics = CardioMetrics(
                steps = session?.steps ?: 0,
                distance = distance,
                duration = Duration.ofMillis(session?.durationMillis ?: 0)
            )
        )
    }

    suspend fun deleteWorkout(workoutId: Int) {
        workoutDao.deleteById(workoutId)
    }
}
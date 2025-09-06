package com.example.gymtracker.repository.cardio

import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.cardio.CardioWorkoutDao
import com.example.gymtracker.database.entity.cardio.CardioWorkoutEntity
import com.example.gymtracker.ui.entity.WorkoutWithTimestamp
import com.example.gymtracker.ui.entity.cardio.CardioMetrics
import com.example.gymtracker.ui.entity.cardio.WorkoutWithMetrics
import java.time.Duration

interface CardioWorkoutRepository {
    suspend fun getAllWorkouts(): List<WorkoutWithTimestamp>
    suspend fun addWorkout(workoutName: String)
    suspend fun updateWorkout(workoutId: Int, workoutName: String)
    suspend fun getLatestWorkoutWithMetrics(workoutId: Int): WorkoutWithMetrics?
    suspend fun deleteWorkout(workoutId: Int)
}

class CardioWorkoutRepositoryImpl(
    private val workoutDao: CardioWorkoutDao,
    private val sessionDao: CardioSessionDao
) : CardioWorkoutRepository {
    override suspend fun getAllWorkouts(): List<WorkoutWithTimestamp> {
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
        val session = sessionDao.getLastSession(workoutId)

        return WorkoutWithMetrics(
            id = workoutId,
            name = workout?.name ?: "",
            timestamp = session?.timestamp,
            metrics = CardioMetrics(
                steps = session?.steps ?: 0,
                distance = session?.distance ?: 0.0,
                duration = session?.duration ?: Duration.ZERO
            )
        )
    }

    override suspend fun deleteWorkout(workoutId: Int) {
        workoutDao.deleteById(workoutId)
    }
}
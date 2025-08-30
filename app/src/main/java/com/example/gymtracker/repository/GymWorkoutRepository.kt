package com.example.gymtracker.repository

import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.dao.gym.GymWorkoutDao

interface GymWorkoutRepository {
    suspend fun getGymWorkoutPlans(): List<WorkoutWithLatestTimestamp>
}

class GymWorkoutRepositoryImpl(
    private val workoutDao: GymWorkoutDao,
    private val sessionDao: GymSessionDao,
) : GymWorkoutRepository {
    override suspend fun getGymWorkoutPlans(): List<WorkoutWithLatestTimestamp> {
        val workouts = workoutDao.getAll()

        return workouts.map {
            val timestamp = sessionDao.getLastSession(it.id)?.timestamp

            WorkoutWithLatestTimestamp(
                id = it.id,
                name = it.name,
                latestTimestamp = timestamp
            )
        }
    }
}
package com.example.gymtracker.repository.gym

import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.dao.gym.GymWorkoutDao
import com.example.gymtracker.repository.WorkoutWithLatestTimestamp

interface GymWorkoutRepository {
    suspend fun getGymWorkoutPlans(): List<WorkoutWithLatestTimestamp>
    suspend fun deleteGymWorkoutPlan(splitId: Int)
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

    override suspend fun deleteGymWorkoutPlan(splitId: Int) = workoutDao.deleteById(splitId)
}
package com.example.gymtracker.database.repository

import com.example.gymtracker.database.dao.cardio.CardioDao
import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.cardio.CardioWorkoutPlanDao
import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.dao.gym.GymWorkoutPlanDao
import java.time.Instant

data class Workout(
    val id: Int,
    val name: String
)

data class WorkoutSession(
    val id: Int,
    val workoutId: Int,
    val timestamp: Instant,
)


interface WorkoutRepository {
    suspend fun getGymWorkouts(): List<Workout>
    suspend fun getCardioWorkouts(): List<Workout>
    suspend fun getGymSessions(): List<WorkoutSession>
    suspend fun getCardioSessions(): List<WorkoutSession>
    suspend fun getWorkoutSessionsBetween(start: Instant, end: Instant): List<WorkoutSession>
}

class WorkoutRepositoryImpl(
    private val gymWorkoutPlanDao : GymWorkoutPlanDao,
    private val cardioWorkoutPlanDao : CardioWorkoutPlanDao,
    private val gymSessionDao: GymSessionDao,
    private val cardioDao: CardioDao,
    private val cardioSessionDao: CardioSessionDao
) : WorkoutRepository {
    override suspend fun getGymWorkouts(): List<Workout> {
        val workouts = gymWorkoutPlanDao.getAll()
        return workouts.map { workout ->
            Workout(
                id = workout.id,
                name = workout.name
            )
        }
    }

    override suspend fun getCardioWorkouts(): List<Workout> {
        val workouts = cardioWorkoutPlanDao.getAll()
        return workouts.map { workout ->
            Workout(
                id = workout.id,
                name = workout.name
            )
        }
    }

    override suspend fun getGymSessions(): List<WorkoutSession> {
        val sessions = gymSessionDao.getAllSessions()
        return sessions.mapNotNull { session ->
            if (session != null) {
                WorkoutSession(
                    id = session.id,
                    workoutId = session.workoutId,
                    timestamp = session.timestamp
                )
            } else null
        }
    }

    override suspend fun getCardioSessions(): List<WorkoutSession> {
        val sessions = cardioSessionDao.getAllSessions()
        val cardioList = cardioDao.getAllCardio().associateBy { it.id }

        return sessions.mapNotNull { session ->
            val cardio = cardioList[session?.cardioId]
            if (cardio != null && session != null) {
                WorkoutSession(
                    id = session.id,
                    workoutId = cardio.workoutId,
                    timestamp = session.timestamp
                )
            } else null
        }
    }

    override suspend fun getWorkoutSessionsBetween(
        start: Instant,
        end: Instant
    ): List<WorkoutSession> {
        val gymSessions = getGymSessionsBetween(start, end)
        val cardioSessions = getCardioSessionsBetween(start, end)
        return gymSessions + cardioSessions
    }

    private suspend fun getGymSessionsBetween(
        start: Instant,
        end: Instant
    ): List<WorkoutSession> {
        val sessions = gymSessionDao.getSessionsForTimespan(start, end)
        return sessions.mapNotNull { session ->
            if (session != null) {
                WorkoutSession(
                    id = session.id,
                    workoutId = session.workoutId,
                    timestamp = session.timestamp
                )
            } else null
        }
    }

    private suspend fun getCardioSessionsBetween(
        start: Instant,
        end: Instant
    ): List<WorkoutSession> {
        val sessions = cardioSessionDao.getSessionsForTimespan(start, end)
        val cardioList = cardioDao.getAllCardio().associateBy { it.id }

        return sessions.mapNotNull { session ->
            val cardio = cardioList[session?.cardioId]
            if (cardio != null && session != null) {
                WorkoutSession(
                    id = session.id,
                    workoutId = cardio.workoutId,
                    timestamp = session.timestamp
                )
            } else null
        }
    }
}
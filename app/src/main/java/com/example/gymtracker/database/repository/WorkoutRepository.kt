package com.example.gymtracker.database.repository

import com.example.gymtracker.database.GymDatabase
import com.example.gymtracker.database.dao.WorkoutDao
import com.example.gymtracker.database.dao.cardio.CardioDao
import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.entity.WorkoutType
import com.example.gymtracker.database.entity.cardio.CardioSessionEntity
import com.example.gymtracker.database.entity.gym.GymSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

data class Workout(
    val id: Int,
    val name: String,
    val type: WorkoutType
)

data class WorkoutSession(
    val id: Int,
    val workout: Workout,
    val timestamp: Instant,
)

interface WorkoutRepository {
    suspend fun getAllWorkouts(): List<Workout>
    suspend fun getAllWorkoutSessions(): List<WorkoutSession>
    suspend fun getWorkoutSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession>

    suspend fun deleteAllData()
}

class WorkoutRepositoryImpl(
    private val db: GymDatabase,
    private val workoutDao: WorkoutDao,
    private val gymSessionDao: GymSessionDao,
    private val cardioDao: CardioDao,
    private val cardioSessionDao: CardioSessionDao
) : WorkoutRepository {

    override suspend fun getAllWorkouts(): List<Workout> {
        val workouts = workoutDao.getAllWorkouts()

        return workouts.map {
            Workout(
                id = it.id,
                name = it.name,
                type = it.type
            )
        }
    }

    override suspend fun getAllWorkoutSessions(): List<WorkoutSession> {
        val splitSessions = getAllGymWorkoutSessions()
        val cardioSessions = getAllCardioWorkoutSessions()
        return splitSessions + cardioSessions
    }

    override suspend fun getWorkoutSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession> {
        val gymSessions = getGymSessionsBetweenDates(startDate, endDate)
        val cardioSessions = getCardioSessionsBetweenDates(startDate, endDate)
        return gymSessions + cardioSessions
    }

    override suspend fun deleteAllData() {
        CoroutineScope(Dispatchers.IO).launch {
            db.clearAllTables()
        }
    }

    private suspend fun getAllGymWorkoutSessions(): List<WorkoutSession> {
        val sessions = gymSessionDao.getAllSessions().filterNotNull()
        return gymSessionsToWorkoutSessions(sessions)
    }

    private suspend fun getAllCardioWorkoutSessions(): List<WorkoutSession> {
        val sessions = cardioSessionDao.getAllSessions().filterNotNull()
        return cardioSessionsToWorkoutSessions(sessions)
    }

    private suspend fun getGymSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession> {
        val sessions = gymSessionDao.getSessionsForTimespan(startDate, endDate).filterNotNull()
        return gymSessionsToWorkoutSessions(sessions)
    }

    private suspend fun getCardioSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession> {
        val sessions = cardioSessionDao.getSessionsForTimespan(startDate, endDate).filterNotNull()
        return cardioSessionsToWorkoutSessions(sessions)
    }

    private suspend fun gymSessionsToWorkoutSessions(sessions: List<GymSessionEntity>): List<WorkoutSession> {
        if (sessions.isEmpty()) return emptyList()

        val workouts = workoutDao.getAllGymWorkouts().associateBy { it.id }

        return sessions.mapNotNull { session ->
            val split = workouts[session.workoutId]
            if (split != null) {
                WorkoutSession(
                    id = session.id,
                    workout = Workout(
                        id = split.id,
                        name = split.name,
                        type = split.type
                    ),
                    timestamp = session.timestamp,
                )
            } else null
        }
    }

    private suspend fun cardioSessionsToWorkoutSessions(sessions: List<CardioSessionEntity>): List<WorkoutSession> {
        if (sessions.isEmpty()) return emptyList()

        val workouts = workoutDao.getAllCardioWorkouts().associateBy { it.id }
        val cardioList = cardioDao.getAllCardio().associateBy { it.id }

        return sessions.mapNotNull { session ->
            val cardio = cardioList[session.cardioId]
            if (cardio != null) {
                val workout = workouts[cardio.workoutId]
                if (workout != null) {
                    WorkoutSession(
                        id = session.id,
                        workout = Workout(
                            id = workout.id,
                            name = workout.name,
                            type = workout.type
                        ),
                        timestamp = session.timestamp,
                    )
                } else null
            } else null
        }
    }
}
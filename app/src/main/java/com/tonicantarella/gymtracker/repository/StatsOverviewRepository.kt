package com.tonicantarella.gymtracker.repository

import com.tonicantarella.gymtracker.database.dao.cardio.CardioSessionDao
import com.tonicantarella.gymtracker.database.dao.cardio.CardioWorkoutDao
import com.tonicantarella.gymtracker.database.dao.gym.GymSessionDao
import com.tonicantarella.gymtracker.database.dao.gym.GymWorkoutDao
import com.tonicantarella.gymtracker.ui.entity.WorkoutWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutSession
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutType
import java.time.Instant

interface StatsOverviewRepository {
    suspend fun getAllGymWorkouts(): List<WorkoutWithTimestamp>
    suspend fun getAllCardioWorkouts(): List<WorkoutWithTimestamp>
    suspend fun getAllGymSessions(): List<WorkoutSession>
    suspend fun getAllCardioSessions(): List<WorkoutSession>
    suspend fun getWorkoutSessionsForTimespan(
        start: Instant,
        end: Instant
    ): List<WorkoutSession>
}

class StatsOverviewRepositoryImpl(
    private val gymWorkoutDao: GymWorkoutDao,
    private val cardioWorkoutDao: CardioWorkoutDao,
    private val gymSessionDao: GymSessionDao,
    private val cardioSessionDao: CardioSessionDao
) : StatsOverviewRepository {
    override suspend fun getAllGymWorkouts(): List<WorkoutWithTimestamp> {
        val workouts = gymWorkoutDao.getAll()

        return workouts.map { workout ->
            val lastSession = gymSessionDao.getLastSession(workout.id)

            WorkoutWithTimestamp(
                id = workout.id,
                name = workout.name,
                timestamp = lastSession?.timestamp
            )
        }
    }

    override suspend fun getAllCardioWorkouts(): List<WorkoutWithTimestamp> {
        return cardioWorkoutDao.getAll().map { workout ->
            val lastSession = cardioSessionDao.getLastSession(workout.id)

            WorkoutWithTimestamp(
                id = workout.id,
                name = workout.name,
                timestamp = lastSession?.timestamp
            )
        }
    }

    override suspend fun getAllGymSessions(): List<WorkoutSession> {
        return gymSessionDao.getAllSessions()?.mapNotNull { session ->
            gymWorkoutDao.getById(session.workoutId)?.let { workout ->
                WorkoutSession(
                    sessionId = session.id,
                    workoutId = session.workoutId,
                    workoutName = workout.name,
                    timestamp = session.timestamp,
                    type = WorkoutType.GYM
                )
            }
        }.orEmpty()
    }

    override suspend fun getAllCardioSessions(): List<WorkoutSession> {
        return cardioSessionDao.getAllSessions()?.mapNotNull { session ->
            cardioWorkoutDao.getById(session.workoutId)?.let { workout ->
                WorkoutSession(
                    sessionId = session.id,
                    workoutId = session.workoutId,
                    workoutName = workout.name,
                    timestamp = session.timestamp,
                    type = WorkoutType.CARDIO
                )
            }
        }.orEmpty()
    }

    override suspend fun getWorkoutSessionsForTimespan(
        start: Instant,
        end: Instant
    ): List<WorkoutSession> {
        val gymSessions = gymSessionDao.getSessionsForTimespan(start, end).orEmpty()
        val cardioSessions = cardioSessionDao.getSessionsForTimespan(start, end).orEmpty()

        val gymWorkoutSessions = gymSessions.mapNotNull { session ->
            gymWorkoutDao.getById(session.workoutId)?.let { workout ->
                WorkoutSession(
                    sessionId = session.id,
                    workoutId = session.workoutId,
                    workoutName = workout.name,
                    timestamp = session.timestamp,
                    type = WorkoutType.GYM
                )
            }
        }
        val cardioWorkoutSessions = cardioSessions.mapNotNull { session ->
            cardioWorkoutDao.getById(session.workoutId)?.let { workout ->
                WorkoutSession(
                    sessionId = session.id,
                    workoutId = session.workoutId,
                    workoutName = workout.name,
                    timestamp = session.timestamp,
                    type = WorkoutType.CARDIO
                )
            }
        }

        return gymWorkoutSessions + cardioWorkoutSessions
    }
}
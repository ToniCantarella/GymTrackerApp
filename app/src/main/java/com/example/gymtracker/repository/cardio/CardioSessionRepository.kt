package com.example.gymtracker.repository.cardio

import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.cardio.CardioWorkoutDao
import com.example.gymtracker.database.entity.cardio.CardioSessionEntity
import com.example.gymtracker.ui.entity.WorkoutSession
import com.example.gymtracker.ui.entity.cardio.CardioMetrics
import com.example.gymtracker.ui.entity.cardio.WorkoutWithMetrics
import java.time.Instant

interface CardioSessionRepository {
    suspend fun getAllSessions(): List<WorkoutSession>
    suspend fun getSessionsForTimespan(start: Instant, end: Instant): List<WorkoutSession>
    suspend fun getWorkoutForSession(sessionId: Int): WorkoutWithMetrics?
    suspend fun markSessionDone(workoutId: Int, metrics: CardioMetrics)
}

class CardioSessionRepositoryImpl(
    private val sessionDao: CardioSessionDao,
    private val workoutDao: CardioWorkoutDao
) : CardioSessionRepository {
    override suspend fun getAllSessions(): List<WorkoutSession> {
        val sessions = sessionDao.getAllSessions()
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

    override suspend fun getSessionsForTimespan(
        start: Instant,
        end: Instant
    ): List<WorkoutSession> {
        val sessions = sessionDao.getSessionsForTimespan(start, end)

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

    override suspend fun getWorkoutForSession(sessionId: Int): WorkoutWithMetrics? {
        val session = sessionDao.getById(sessionId)
        val workout = workoutDao.getById(session.workoutId)
        if (workout == null) {
            return null
        }

        return WorkoutWithMetrics(
            id = workout.id,
            name = workout.name,
            timestamp = session.timestamp,
            metrics = CardioMetrics(
                steps = session.steps,
                distance = session.distance,
                duration = session.duration
            )
        )
    }

    override suspend fun markSessionDone(
        workoutId: Int,
        metrics: CardioMetrics
    ) {
        val timestamp = Instant.now()

        sessionDao.insert(
            CardioSessionEntity(
                workoutId = workoutId,
                timestamp = timestamp,
                steps = metrics.steps,
                distance = metrics.distance,
                duration = metrics.duration
            )
        )
    }
}
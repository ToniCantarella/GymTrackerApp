package com.tonicantarella.gymtracker.repository.cardio

import com.tonicantarella.gymtracker.database.dao.cardio.CardioSessionDao
import com.tonicantarella.gymtracker.database.dao.cardio.CardioWorkoutDao
import com.tonicantarella.gymtracker.database.entity.cardio.CardioSessionEntity
import com.tonicantarella.gymtracker.ui.entity.cardio.CardioMetrics
import com.tonicantarella.gymtracker.ui.entity.cardio.WorkoutWithMetrics
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutSession
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutType
import com.tonicantarella.gymtracker.utility.UnitUtil.convertDistanceFromDatabase
import com.tonicantarella.gymtracker.utility.UnitUtil.convertDistanceToDatabase
import java.time.Duration
import java.time.Instant

interface CardioSessionRepository {
    suspend fun getAllSessions(): List<WorkoutSession>
    suspend fun getSessionsForTimespan(start: Instant, end: Instant): List<WorkoutSession>
    suspend fun getWorkoutForSession(sessionId: Int): WorkoutWithMetrics?
    suspend fun markSessionDone(
        workoutId: Int,
        metrics: CardioMetrics,
        timestamp: Instant? = null
    )
}

class CardioSessionRepositoryImpl(
    private val sessionDao: CardioSessionDao,
    private val workoutDao: CardioWorkoutDao
) : CardioSessionRepository {
    override suspend fun getAllSessions(): List<WorkoutSession> {
        return sessionDao.getAllSessions()?.mapNotNull { session ->
            workoutDao.getById(session.workoutId)?.let { workout ->
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

    override suspend fun getSessionsForTimespan(
        start: Instant,
        end: Instant
    ): List<WorkoutSession> {
        return sessionDao.getSessionsForTimespan(start, end)?.mapNotNull { session ->
            workoutDao.getById(session.workoutId)?.let { workout ->
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
                distance = session.distance?.convertDistanceFromDatabase(),
                duration = if (session.durationMillis != null)Duration.ofMillis(session.durationMillis) else null
            )
        )
    }

    override suspend fun markSessionDone(
        workoutId: Int,
        metrics: CardioMetrics,
        timestamp: Instant?
    ) {

        sessionDao.insert(
            CardioSessionEntity(
                workoutId = workoutId,
                timestamp = timestamp ?: Instant.now(),
                steps = metrics.steps,
                distance = metrics.distance?.convertDistanceToDatabase(),
                durationMillis = metrics.duration?.toMillis()
            )
        )
    }
}
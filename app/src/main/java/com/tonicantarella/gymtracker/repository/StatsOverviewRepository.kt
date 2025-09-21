package com.tonicantarella.gymtracker.repository

import com.tonicantarella.gymtracker.database.dao.cardio.CardioSessionDao
import com.tonicantarella.gymtracker.database.dao.cardio.CardioWorkoutDao
import com.tonicantarella.gymtracker.database.dao.gym.ExerciseDao
import com.tonicantarella.gymtracker.database.dao.gym.GymSessionDao
import com.tonicantarella.gymtracker.database.dao.gym.GymWorkoutDao
import com.tonicantarella.gymtracker.database.dao.gym.SetDao
import com.tonicantarella.gymtracker.database.dao.gym.SetSessionDao
import com.tonicantarella.gymtracker.ui.entity.WorkoutWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.statsoverview.CardioWorkoutWithGeneralStats
import com.tonicantarella.gymtracker.ui.entity.statsoverview.GymWorkoutWithGeneralStats
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutSession
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutType
import java.time.Duration
import java.time.Instant

interface StatsOverviewRepository {
    suspend fun getAllGymWorkouts(): List<WorkoutWithTimestamp>
    suspend fun getAllCardioWorkouts(): List<WorkoutWithTimestamp>
    suspend fun getAllGymWorkoutsWithGeneralStats(): List<GymWorkoutWithGeneralStats>
    suspend fun getAllCardioWorkoutsWithGeneralStats(): List<CardioWorkoutWithGeneralStats>
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
    private val cardioSessionDao: CardioSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao,
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

    override suspend fun getAllGymWorkoutsWithGeneralStats(): List<GymWorkoutWithGeneralStats> {
        return gymWorkoutDao.getAll().map { workout ->
            val exerciseCount = exerciseDao.countExercisesByWorkoutId(workout.id)

            val avgSetStats = setSessionDao.getAverageWeightAndRepsForWorkout(workout.id)
            val avgWeight = avgSetStats?.avgWeight ?: 0.0
            val avgReps = avgSetStats?.avgReps ?: 0.0

            val avgSets = setDao.getAverageSetsPerExerciseForWorkout(workout.id) ?: 0.0

            GymWorkoutWithGeneralStats(
                id = workout.id,
                name = workout.name,
                exerciseCount = exerciseCount,
                avgWeight = avgWeight,
                avgSets = avgSets.toInt(),
                avgReps = avgReps.toInt(),
                avgDuration = Duration.ZERO
            )
        }
    }

    override suspend fun getAllCardioWorkoutsWithGeneralStats(): List<CardioWorkoutWithGeneralStats> {
        return cardioWorkoutDao.getAll().map { workout ->
            val avgStats = cardioSessionDao.getAverageStatsForCardioWorkout(workout.id)

            val avgDistance = avgStats?.avgDistance ?: 0.0
            val avgSteps = avgStats?.avgSteps?.toInt() ?: 0
            val avgDuration = avgStats?.avgDurationMillis?.let { millis ->
                if (millis > 0) Duration.ofMillis(millis) else Duration.ZERO
            } ?: Duration.ZERO

            CardioWorkoutWithGeneralStats(
                id = workout.id,
                name = workout.name,
                avgSteps = avgSteps,
                avgDuration = avgDuration,
                avgDistance = avgDistance,
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
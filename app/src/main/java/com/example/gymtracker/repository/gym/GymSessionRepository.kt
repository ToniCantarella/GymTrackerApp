package com.example.gymtracker.repository.gym

import com.example.gymtracker.database.dao.gym.ExerciseDao
import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.dao.gym.GymWorkoutDao
import com.example.gymtracker.database.dao.gym.SetDao
import com.example.gymtracker.database.dao.gym.SetSessionDao
import com.example.gymtracker.database.entity.gym.GymSessionEntity
import com.example.gymtracker.database.entity.gym.SetSessionEntity
import com.example.gymtracker.repository.WorkoutSession
import com.example.gymtracker.ui.gym.entity.Exercise
import com.example.gymtracker.ui.gym.entity.WorkoutSet
import java.time.Instant

interface GymSessionRepository {
    suspend fun getAllSessions(): List<WorkoutSession>
    suspend fun getSessionsForTimespan(start: Instant, end: Instant): List<WorkoutSession>
    suspend fun getWorkoutForSession(sessionId: Int): WorkoutWithExercises?
    suspend fun markSessionDone(
        workoutId: Int,
        exercises: List<Exercise>
    )
}

class GymSessionRepositoryImpl(
    private val workoutDao: GymWorkoutDao,
    private val workoutSessionDao: GymSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao
) : GymSessionRepository {

    override suspend fun getAllSessions(): List<WorkoutSession> {
        val sessions = workoutSessionDao.getAllSessions()
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
        val sessions = workoutSessionDao.getSessionsForTimespan(start, end)
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

    override suspend fun getWorkoutForSession(sessionId: Int): WorkoutWithExercises? {
        val gymSession = workoutSessionDao.getById(sessionId)
        val workout = workoutDao.getById(gymSession.workoutId)
        if (workout == null) {
            return null
        }
        val exercises = exerciseDao.getExercisesByWorkoutId(workout.id)
        val setSession = setSessionDao.getSetsForSession(gymSession.id)

        return WorkoutWithExercises(
            id = workout.id,
            name = workout.name,
            timestamp = gymSession.timestamp,
            exercises = exercises.map { exercise ->
                val sets = setDao.getSetsForExercise(exercise.id)
                val setSessionsForExercise =
                    setSession.filter { sets.any { set -> it.setId == set.id } }

                Exercise(
                    uuid = exercise.uuid,
                    name = exercise.name,
                    description = exercise.description,
                    sets = setSessionsForExercise.map { set ->
                        WorkoutSet(
                            uuid = set.uuid,
                            weight = set.weight,
                            repetitions = set.repetitions
                        )
                    }
                )
            }
        )
    }

    override suspend fun markSessionDone(
        workoutId: Int,
        exercises: List<Exercise>
    ) {
        val timestamp = Instant.now()
        val sessionId = workoutSessionDao.insert(
            GymSessionEntity(
                workoutId = workoutId,
                timestamp = timestamp
            )
        ).toInt()

        val currentExercises = exerciseDao.getExercisesByWorkoutId(workoutId).associateBy { it.uuid }

        exercises.forEach { exercise ->
            val currentExercise = currentExercises[exercise.uuid] ?: return
            val currentSets = setDao.getSetsForExercise(currentExercise.id).associateBy { it.uuid }

            exercise.sets.forEach { set ->
                if (set.checked) {
                    val currentSet = currentSets[set.uuid] ?: return
                    setSessionDao.insert(
                        SetSessionEntity(
                            setId = currentSet.id,
                            sessionId = sessionId,
                            uuid = set.uuid,
                            weight = set.weight,
                            repetitions = set.repetitions
                        )
                    )
                }
            }
        }
    }
}
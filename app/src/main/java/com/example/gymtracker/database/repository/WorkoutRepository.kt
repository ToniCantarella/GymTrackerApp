package com.example.gymtracker.database.repository

import com.example.gymtracker.database.dao.ExerciseDao
import com.example.gymtracker.database.dao.SetDao
import com.example.gymtracker.database.dao.SetSessionDao
import com.example.gymtracker.database.dao.SplitDao
import com.example.gymtracker.database.dao.SplitSessionDao
import com.example.gymtracker.database.entity.workout.ExerciseEntity
import com.example.gymtracker.database.entity.workout.SetEntity
import com.example.gymtracker.database.entity.workout.SetSessionEntity
import com.example.gymtracker.database.entity.workout.SplitEntity
import com.example.gymtracker.database.entity.workout.SplitSessionEntity
import com.example.gymtracker.ui.common.WorkoutListItem
import com.example.gymtracker.ui.workouts.entity.Exercise
import com.example.gymtracker.ui.workouts.entity.WorkoutSet
import java.time.Instant

data class LatestSplitWithExercises(
    val id: Int,
    val name: String,
    val timestamp: Instant?,
    val exercises: List<Exercise>
)

data class Workout(
    val name: String,
    val type: WorkoutType
)

enum class WorkoutType {
    GYM,
    CARDIO
}

data class WorkoutSession(
    val name: String,
    val timestamp: Instant,
    val type: WorkoutType
)

class WorkoutRepository(
    private val splitDao: SplitDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao,
    private val sessionDao: SplitSessionDao
) {
    suspend fun getSplitsWithLatestTimestamp(): List<WorkoutListItem> {
        val splits = splitDao.getAllSplits()

        return splits.map {
            val timestamp = sessionDao.getLastSession(it.id)?.timestamp

            WorkoutListItem(
                id = it.id,
                name = it.name,
                latestTimestamp = timestamp
            )
        }.sortedBy { it.latestTimestamp }
    }

    suspend fun addSplitWithExercises(splitName: String, exercises: List<Exercise>) {
        val splitId = splitDao.insert(SplitEntity(name = splitName.trim())).toInt()

        exercises.forEach { exercise ->
            val exerciseId = exerciseDao.insert(
                ExerciseEntity(
                    splitId = splitId,
                    uuid = exercise.uuid,
                    name = exercise.name.trim(),
                    description = exercise.description?.trim()
                )
            ).toInt()

            exercise.sets.forEach { set ->
                setDao.insert(
                    SetEntity(
                        exerciseId = exerciseId,
                        uuid = set.uuid,
                        weight = set.weight,
                        repetitions = set.repetitions
                    )
                )
            }
        }
    }

    suspend fun deleteSplit(splitId: Int) = splitDao.deleteById(splitId)

    suspend fun markSessionDone(
        splitId: Int,
        splitName: String? = null,
        exercises: List<Exercise>
    ) {
        if (exercises.isEmpty()) return

        if (splitName?.isNotEmpty() == true) {
            val currentSplit = splitDao.getSplitById(splitId)
            if (currentSplit.name != splitName) {
                splitDao.updateSplit(
                    currentSplit.copy(
                        name = splitName.trim()
                    )
                )
            }
        }

        val performedSets = exercises.filter { it.sets.any { set -> set.checked } }

        val sessionId = if (performedSets.isNotEmpty()) {
            sessionDao.insert(
                SplitSessionEntity(
                    splitId = splitId,
                    timestamp = Instant.now()
                )
            ).toInt()
        } else null

        val currentExercises = exerciseDao.getExercisesBySplitId(splitId).associateBy { it.uuid }

        val deletedExercises = currentExercises.values.filter { current ->
            exercises.none {
                it.uuid == current.uuid
            }
        }

        if (deletedExercises.isNotEmpty()) {
            exerciseDao.deleteExercises(deletedExercises)
        }

        exercises.forEach { exercise ->
            val currentExercise = currentExercises[exercise.uuid]
            val exerciseId = currentExercise?.id ?: exerciseDao.insert(
                ExerciseEntity(
                    splitId = splitId,
                    uuid = exercise.uuid,
                    name = exercise.name.trim(),
                    description = exercise.description?.trim()
                )
            ).toInt()

            val exerciseInfoChanged =
                (currentExercise?.name != exercise.name || currentExercise.description != exercise.description)
            if (currentExercise != null && exerciseInfoChanged) {
                exerciseDao.updateExercise(
                    currentExercise.copy(
                        name = exercise.name.trim(),
                        description = exercise.description?.trim()
                    )
                )
            }

            val currentSets = setDao.getSetsForExercise(exerciseId).associateBy { it.uuid }

            val deletedSets = currentSets.values.filter { current ->
                exercise.sets.none { it.uuid == current.uuid }
            }

            if (deletedSets.isNotEmpty()) {
                setDao.deleteSets(deletedSets)
            }

            exercise.sets.forEach { set ->
                val currentSet = currentSets[set.uuid]
                val setId = currentSet?.id ?: setDao.insert(
                    SetEntity(
                        exerciseId = exerciseId,
                        uuid = set.uuid,
                        weight = set.weight,
                        repetitions = set.repetitions
                    )
                ).toInt()

                val setInfoChanged =
                    (currentSet?.weight != set.weight || currentSet.repetitions != set.repetitions)
                if (currentSet != null && setInfoChanged) {
                    setDao.updateSet(
                        currentSet.copy(
                            weight = set.weight,
                            repetitions = set.repetitions
                        )
                    )
                }

                if (sessionId != null && set.checked) {
                    setSessionDao.insert(
                        SetSessionEntity(
                            setId = setId,
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


    suspend fun getLatestSplitWithExercises(splitId: Int): LatestSplitWithExercises? {
        val timestamp = sessionDao.getLastSession(splitId)?.timestamp
        val split = splitDao.getSplitById(splitId)

        val exercises = exerciseDao.getExercisesBySplitId(splitId)
        val exercisesGrouped = exercises.map { exercise ->
            val sets = setDao.getSetsForExercise(exercise.id)

            Exercise(
                uuid = exercise.uuid,
                name = exercise.name,
                description = exercise.description,
                sets = sets.map { set ->
                    WorkoutSet(
                        uuid = set.uuid,
                        weight = set.weight,
                        repetitions = set.repetitions
                    )
                }
            )
        }

        return LatestSplitWithExercises(
            id = splitId,
            name = split.name,
            timestamp = timestamp,
            exercises = exercisesGrouped
        )
    }

    suspend fun getAllWorkouts(): List<Workout> {
        val splits = splitDao.getAllSplits()

        return splits.map {
            Workout(
                name = it.name,
                type = WorkoutType.GYM
            )
        }
    }

    suspend fun getAllSplitSessions(): List<WorkoutSession> {
        val sessions = sessionDao.getAllSessions().filterNotNull()
        return getWorkoutSessionsForSplitSessions(sessions)
    }

    suspend fun getSplitSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession> {
        val sessions = sessionDao.getSessionsForTimespan(startDate, endDate).filterNotNull()

        return getWorkoutSessionsForSplitSessions(sessions)
    }

    private suspend fun getWorkoutSessionsForSplitSessions(sessions: List<SplitSessionEntity>): List<WorkoutSession> {
        if (sessions.isEmpty()) return emptyList()

        val splits = splitDao.getAllSplits().associateBy { it.id }

        return sessions.mapNotNull { session ->
            val splitName = splits[session.splitId]?.name
            if (splitName != null) {
                WorkoutSession(
                    name = splitName,
                    timestamp = session.timestamp,
                    type = WorkoutType.GYM
                )
            } else null
        }
    }
}
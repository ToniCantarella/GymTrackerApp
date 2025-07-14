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
import com.example.gymtracker.ui.workouts.entity.Exercise
import com.example.gymtracker.ui.workouts.entity.WorkoutSet
import com.example.gymtracker.ui.workouts.splitslist.SplitListItem
import java.time.Instant

data class LastPerformedSplit(
    val id: Int,
    val name: String,
    val timestamp: Instant,
    val exercises: List<Exercise>
)

class WorkoutRepository(
    private val splitDao: SplitDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao,
    private val sessionDao: SplitSessionDao
) {
    suspend fun getSplitsWithLatestTimestamp(): List<SplitListItem> {
        val splits = splitDao.getAllSplits()

        return splits.map {
            val timestamp = sessionDao.getLastSession(it.id)?.timestamp

            SplitListItem(
                id = it.id,
                name = it.name,
                latestTimestamp = timestamp ?: Instant.now()
            )
        }
    }

    suspend fun addSplitWithExercises(splitName: String, exercises: List<Exercise>) {
        val splitId = splitDao.insert(SplitEntity(name = splitName)).toInt()
        val sessionId = sessionDao.insert(
            SplitSessionEntity(
                splitId = splitId,
                timestamp = Instant.now()
            )
        ).toInt()

        exercises.forEach { exercise ->
            val exerciseId = exerciseDao.insert(
                ExerciseEntity(
                    splitId = splitId,
                    uuid = exercise.uuid,
                    name = exercise.name,
                    description = exercise.description
                )
            ).toInt()

            exercise.sets.forEach { set ->
                val setId = setDao.insert(
                    SetEntity(
                        exerciseId = exerciseId,
                        uuid = set.uuid,
                        weight = set.weight,
                        repetitions = set.repetitions
                    )
                ).toInt()

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

    suspend fun deleteSplit(splitId: Int) {
        splitDao.deleteById(splitId)
    }

    suspend fun markSessionDone(splitId: Int, exercisesPerformed: List<Exercise>): Long {
        val sessionId = sessionDao.insert(
            SplitSessionEntity(
                splitId = splitId,
                timestamp = Instant.now()
            )
        ).toInt()

        val exercisesForSplit = exerciseDao.getBySplitId(splitId).associateBy { it.uuid }

        exercisesPerformed.forEach { performedExercise ->
            val exerciseId = exercisesForSplit[performedExercise.uuid]?.id ?: exerciseDao.insert(
                ExerciseEntity(
                    splitId = splitId,
                    uuid = performedExercise.uuid,
                    name = performedExercise.name,
                    description = performedExercise.description
                )
            ).toInt()

            val existingSets = setDao.getSetsForExercise(exerciseId).associateBy { it.uuid }

            performedExercise.sets.forEach { performedSet ->
                val existingSet = existingSets[performedSet.uuid]
                val setId = existingSet?.id ?: setDao.insert(
                    SetEntity(
                        exerciseId = exerciseId,
                        uuid = performedSet.uuid,
                        weight = performedSet.weight,
                        repetitions = performedSet.repetitions
                    )
                ).toInt()

                val setInfoChanged = (existingSet?.weight != performedSet.weight || existingSet.repetitions != performedSet.repetitions)
                if (existingSet != null && setInfoChanged) {
                    setDao.updateSet(
                        existingSet.copy(
                            weight = performedSet.weight,
                            repetitions = performedSet.repetitions
                        )
                    )
                }

                setSessionDao.insert(
                    SetSessionEntity(
                        setId = setId,
                        sessionId = sessionId,
                        uuid = performedSet.uuid,
                        weight = performedSet.weight,
                        repetitions = performedSet.repetitions
                    )
                )
            }
        }

        return sessionId.toLong()
    }


    suspend fun getLastPerformedSplit(splitId: Int): LastPerformedSplit? {
        val lastSession = sessionDao.getLastSession(splitId) ?: return null
        val split = splitDao.getSplitById(lastSession.splitId)

        val exercises = exerciseDao.getBySplitId(splitId)
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

        return LastPerformedSplit(
            id = splitId,
            name = split.name,
            timestamp = lastSession.timestamp,
            exercises = exercisesGrouped
        )
    }
}
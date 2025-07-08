package com.example.gymtracker.database.repository

import com.example.gymtracker.database.dao.ExerciseDao
import com.example.gymtracker.database.dao.SetDao
import com.example.gymtracker.database.dao.SplitDao
import com.example.gymtracker.database.dao.SplitSessionDao
import com.example.gymtracker.database.entity.workout.ExerciseEntity
import com.example.gymtracker.database.entity.workout.SetEntity
import com.example.gymtracker.database.entity.workout.SplitEntity
import com.example.gymtracker.database.entity.workout.SplitSessionEntity
import com.example.gymtracker.ui.workouts.split.Exercise
import com.example.gymtracker.ui.workouts.split.WorkoutSet
import com.example.gymtracker.ui.workouts.splitslist.SplitListItem
import java.time.Instant

data class LastPerformedSplit(
    val splitName: String,
    val timestamp: Instant,
    val exercises: List<Exercise>
)

class WorkoutRepository(
    private val splitDao: SplitDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val sessionDao: SplitSessionDao
) {
    suspend fun getSplitsWithLatestTimestamp(): List<SplitListItem> {
        return splitDao.getSplitsWithLatestSession()
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
                setDao.insert(
                    SetEntity(
                        exerciseId = exerciseId,
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
        val exercises = exerciseDao.getBySplitId(splitId)

        val uuidToIdMap = exercises.associateBy { it.uuid }

        exercisesPerformed.forEach { exercise ->
            val exerciseId = uuidToIdMap[exercise.uuid]?.id
            if (exerciseId != null) {
                exercise.sets.forEach { set ->
                    setDao.insert(
                        SetEntity(
                            exerciseId = exerciseId,
                            sessionId = sessionId,
                            uuid = set.uuid,
                            weight = set.weight,
                            repetitions = set.repetitions
                        )
                    )
                }
            }
        }

        return sessionId.toLong()
    }

    suspend fun getLastPerformedSplit(splitId: Int): LastPerformedSplit? {
        val lastSession = sessionDao.getLastSession(splitId) ?: return null
        val split = splitDao.getSplitById(lastSession.splitId)
        val performedSets = setDao.getSetsForSession(lastSession.id)

        val exercisesGrouped = performedSets.groupBy { it.exerciseId }.map { (exerciseId, sets) ->
            val exercise = exerciseDao.getById(exerciseId)
            Exercise(
                uuid = exercise.uuid,
                name = exercise.name,
                description = exercise.description,
                sets = sets.map {
                    WorkoutSet(
                        uuid = it.uuid,
                        weight = it.weight,
                        repetitions = it.repetitions
                    )
                }
            )
        }

        return LastPerformedSplit(
            splitName = split.name,
            timestamp = lastSession.timestamp,
            exercises = exercisesGrouped
        )
    }
}
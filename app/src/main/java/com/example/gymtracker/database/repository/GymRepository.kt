package com.example.gymtracker.database.repository

import com.example.gymtracker.database.dao.WorkoutDao
import com.example.gymtracker.database.dao.gym.ExerciseDao
import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.dao.gym.SetDao
import com.example.gymtracker.database.dao.gym.SetSessionDao
import com.example.gymtracker.database.entity.WorkoutEntity
import com.example.gymtracker.database.entity.WorkoutType
import com.example.gymtracker.database.entity.gym.ExerciseEntity
import com.example.gymtracker.database.entity.gym.GymSessionEntity
import com.example.gymtracker.database.entity.gym.SetEntity
import com.example.gymtracker.database.entity.gym.SetSessionEntity
import com.example.gymtracker.ui.gym.entity.Exercise
import com.example.gymtracker.ui.gym.entity.WorkoutSet
import com.example.gymtracker.utility.UnitUtil
import com.example.gymtracker.utility.UnitUtil.roundToDisplay
import com.example.gymtracker.utility.WeightUnit
import java.time.Instant

data class SplitWithExercises(
    val id: Int,
    val name: String,
    val timestamp: Instant?,
    val exercises: List<Exercise>
)

data class WorkoutWithLatestTimestamp(
    val id: Int,
    val name: String,
    val latestTimestamp: Instant?,
    val selected: Boolean = false
)

interface GymRepository {
    suspend fun addSplitWithExercises(splitName: String, exercises: List<Exercise>)
    suspend fun getSplitsWithLatestTimestamp(): List<WorkoutWithLatestTimestamp>
    suspend fun getLatestSplitWithExercises(id: Int): SplitWithExercises?
    suspend fun getSplitBySession(sessionId: Int): SplitWithExercises
    suspend fun deleteSplit(splitId: Int)
    suspend fun markSplitSessionDone(
        splitId: Int,
        splitName: String,
        exercises: List<Exercise>,
        timestamp: Instant? = null
    )
}

class GymRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val gymSessionDao: GymSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao
): GymRepository {

    override suspend fun addSplitWithExercises(splitName: String, exercises: List<Exercise>) {
        val workoutId = workoutDao.insert(
            WorkoutEntity(
                name = splitName.trim(),
                type = WorkoutType.GYM
            )
        ).toInt()

        exercises.forEach { exercise ->
            val exerciseId = exerciseDao.insert(
                ExerciseEntity(
                    workoutId = workoutId,
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
                        weight = convertWeightToDatabase(set.weight),
                        repetitions = set.repetitions
                    )
                )
            }
        }
    }

    override suspend fun getSplitsWithLatestTimestamp(): List<WorkoutWithLatestTimestamp> {
        val workouts = workoutDao.getAllGymWorkouts()

        return workouts.map {
            val timestamp = gymSessionDao.getLastSession(it.id)?.timestamp

            WorkoutWithLatestTimestamp(
                id = it.id,
                name = it.name,
                latestTimestamp = timestamp
            )
        }
    }

    override suspend fun getLatestSplitWithExercises(id: Int): SplitWithExercises? {
        val workout = workoutDao.getById(id)
        val timestamp = gymSessionDao.getLastSession(id)?.timestamp

        val exercises = exerciseDao.getExercisesByWorkoutId(id)
        val exercisesGrouped = exercises.map { exercise ->
            val sets = setDao.getSetsForExercise(exercise.id)

            Exercise(
                uuid = exercise.uuid,
                name = exercise.name,
                description = exercise.description,
                sets = sets.map { set ->
                    WorkoutSet(
                        uuid = set.uuid,
                        weight = convertWeightFromDatabase(set.weight),
                        repetitions = set.repetitions
                    )
                }
            )
        }

        return SplitWithExercises(
            id = id,
            name = workout.name,
            timestamp = timestamp,
            exercises = exercisesGrouped
        )
    }

    override suspend fun getSplitBySession(
        sessionId: Int
    ): SplitWithExercises {
        val gymSession = gymSessionDao.getById(sessionId)
        val split = workoutDao.getById(gymSession.workoutId)
        val exercises = exerciseDao.getExercisesByWorkoutId(split.id)
        val setSession = setSessionDao.getSetsForSession(gymSession.id)

        return SplitWithExercises(
            id = split.id,
            name = split.name,
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

    override suspend fun deleteSplit(splitId: Int) = workoutDao.deleteById(splitId)

    override suspend fun markSplitSessionDone(
        splitId: Int,
        splitName: String,
        exercises: List<Exercise>,
        timestamp: Instant?
    ) {
        if (exercises.isEmpty()) return

        val currentSplit = workoutDao.getById(splitId)
        val newName = splitName.trim()

        if (newName.isNotEmpty() && currentSplit.name != newName) {
            workoutDao.updateWorkout(
                currentSplit.copy(
                    name = newName
                )
            )
        }

        val performedSets = exercises.filter { it.sets.any { set -> set.checked } }

        val sessionId = if (performedSets.isNotEmpty()) {
            gymSessionDao.insert(
                GymSessionEntity(
                    workoutId = splitId,
                    timestamp = timestamp ?: Instant.now()
                )
            ).toInt()
        } else null

        val currentExercises = exerciseDao.getExercisesByWorkoutId(splitId).associateBy { it.uuid }

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
                    workoutId = splitId,
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
                        weight = convertWeightToDatabase(set.weight),
                        repetitions = set.repetitions
                    )
                ).toInt()

                val weight = convertWeightToDatabase(set.weight)
                val setInfoChanged = currentSet?.weight != weight ||
                        currentSet.repetitions != set.repetitions

                if (currentSet != null && setInfoChanged) {
                    setDao.updateSet(
                        currentSet.copy(
                            weight = weight,
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
                            weight = convertWeightToDatabase(set.weight),
                            repetitions = set.repetitions
                        )
                    )
                }
            }
        }
    }

    private fun convertWeightToDatabase(weight: Double): Double =
        if (UnitUtil.weightUnit == WeightUnit.KILOGRAM)
            weight
        else
            UnitUtil.lbToKg(weight)

    private fun convertWeightFromDatabase(weight: Double): Double =
        if (UnitUtil.weightUnit == WeightUnit.KILOGRAM)
            weight
        else
            UnitUtil.kgToLb(weight).roundToDisplay()
}
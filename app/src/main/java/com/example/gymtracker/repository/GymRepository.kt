package com.example.gymtracker.repository

import com.example.gymtracker.database.dao.gym.ExerciseDao
import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.dao.gym.GymWorkoutDao
import com.example.gymtracker.database.dao.gym.SetDao
import com.example.gymtracker.database.dao.gym.SetSessionDao
import com.example.gymtracker.database.entity.gym.ExerciseEntity
import com.example.gymtracker.database.entity.gym.GymSessionEntity
import com.example.gymtracker.database.entity.gym.GymWorkoutEntity
import com.example.gymtracker.database.entity.gym.SetEntity
import com.example.gymtracker.database.entity.gym.SetSessionEntity
import com.example.gymtracker.repository.gym.WorkoutWithExercises
import com.example.gymtracker.repository.gym.WorkoutWithLatestTimestamp
import com.example.gymtracker.ui.entity.gym.Exercise
import com.example.gymtracker.ui.entity.gym.WorkoutSet
import com.example.gymtracker.utility.UnitUtil
import com.example.gymtracker.utility.UnitUtil.roundToDisplay
import com.example.gymtracker.utility.WeightUnit
import java.time.Instant



interface GymRepository {
    suspend fun addGymWorkoutWithExercises(workoutName: String, exercises: List<Exercise>)
    suspend fun getGymWorkoutPlans(): List<WorkoutWithLatestTimestamp>
    suspend fun getLatestGymWorkoutWithExercises(id: Int): WorkoutWithExercises?
    suspend fun getSplitBySession(sessionId: Int): WorkoutWithExercises?
    suspend fun deleteGymWorkoutPlan(splitId: Int)
    suspend fun markGymSessionDone(
        workoutId: Int,
        workoutName: String,
        exercises: List<Exercise>,
        timestamp: Instant? = null
    )
}

class GymRepositoryImpl(
    private val workoutDao: GymWorkoutDao,
    private val gymSessionDao: GymSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao
): GymRepository {

    override suspend fun addGymWorkoutWithExercises(workoutName: String, exercises: List<Exercise>) {
        val workoutId = workoutDao.insert(
            GymWorkoutEntity(
                name = workoutName.trim()
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

    override suspend fun getGymWorkoutPlans(): List<WorkoutWithLatestTimestamp> {
        val workouts = workoutDao.getAll()

        return workouts.map {
            val timestamp = gymSessionDao.getLastSession(it.id)?.timestamp

            WorkoutWithLatestTimestamp(
                id = it.id,
                name = it.name,
                latestTimestamp = timestamp
            )
        }
    }

    override suspend fun getLatestGymWorkoutWithExercises(id: Int): WorkoutWithExercises? {
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

        return WorkoutWithExercises(
            id = id,
            name = workout?.name ?: "",
            timestamp = timestamp,
            exercises = exercisesGrouped
        )
    }

    override suspend fun getSplitBySession(
        sessionId: Int
    ): WorkoutWithExercises? {
        val gymSession = gymSessionDao.getById(sessionId)
        val split = workoutDao.getById(gymSession.workoutId)
        if (split == null) {
            return null
        }
        val exercises = exerciseDao.getExercisesByWorkoutId(split.id)
        val setSession = setSessionDao.getSetsForSession(gymSession.id)

        return WorkoutWithExercises(
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

    override suspend fun deleteGymWorkoutPlan(splitId: Int) = workoutDao.deleteById(splitId)

    override suspend fun markGymSessionDone(
        workoutId: Int,
        workoutName: String,
        exercises: List<Exercise>,
        timestamp: Instant?
    ) {
        if (exercises.isEmpty()) return

        val currentSplit = workoutDao.getById(workoutId)
        val newName = workoutName.trim()
        if (currentSplit == null) {
            return
        }

        if (newName.isNotEmpty() && currentSplit.name != newName) {
            workoutDao.update(
                currentSplit.copy(
                    name = newName
                )
            )
        }

        val performedSets = exercises.filter { it.sets.any { set -> set.checked } }

        val sessionId = if (performedSets.isNotEmpty()) {
            gymSessionDao.insert(
                GymSessionEntity(
                    workoutId = workoutId,
                    timestamp = timestamp ?: Instant.now()
                )
            ).toInt()
        } else null

        val currentExercises = exerciseDao.getExercisesByWorkoutId(workoutId).associateBy { it.uuid }

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
                    workoutId = workoutId,
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
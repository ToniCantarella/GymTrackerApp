package com.tonicantarella.gymtracker.repository.gym

import com.tonicantarella.gymtracker.database.dao.gym.ExerciseDao
import com.tonicantarella.gymtracker.database.dao.gym.GymSessionDao
import com.tonicantarella.gymtracker.database.dao.gym.GymWorkoutDao
import com.tonicantarella.gymtracker.database.dao.gym.SetDao
import com.tonicantarella.gymtracker.database.entity.gym.ExerciseEntity
import com.tonicantarella.gymtracker.database.entity.gym.GymWorkoutEntity
import com.tonicantarella.gymtracker.database.entity.gym.SetEntity
import com.tonicantarella.gymtracker.ui.entity.WorkoutWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.gym.Exercise
import com.tonicantarella.gymtracker.ui.entity.gym.WorkoutSet
import com.tonicantarella.gymtracker.ui.entity.gym.WorkoutWithExercises
import com.tonicantarella.gymtracker.utility.UnitUtil.convertWeightFromDatabase
import com.tonicantarella.gymtracker.utility.UnitUtil.convertWeightToDatabase

class GymWorkoutRepository(
    private val workoutDao: GymWorkoutDao,
    private val sessionDao: GymSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
) {

    suspend fun getAllWorkouts(): List<WorkoutWithTimestamp> {
        val workouts = workoutDao.getAll()

        return workouts.map {
            val timestamp = sessionDao.getLastSession(it.id)?.timestamp

            WorkoutWithTimestamp(
                id = it.id,
                name = it.name,
                timestamp = timestamp
            )
        }
    }

    suspend fun addWorkout(workoutName: String, exercises: List<Exercise>): Int {
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
                        weight = set.weight.convertWeightToDatabase(),
                        repetitions = set.repetitions
                    )
                )
            }
        }

        return workoutId
    }

    suspend fun updateWorkout(
        workoutId: Int,
        workoutName: String,
        exercises: List<Exercise>
    ) {
        val currentWorkout = workoutDao.getById(workoutId) ?: return

        if (workoutName.trim() != currentWorkout.name) {
            workoutDao.update(
                currentWorkout.copy(
                    name = workoutName.trim()
                )
            )
        }

        val currentExercises =
            exerciseDao.getExercisesByWorkoutId(workoutId)

        val deletedExercises = currentExercises.filter { current ->
            exercises.none {
                it.uuid == current.uuid
            }
        }

        if (deletedExercises.isNotEmpty()) {
            exerciseDao.deleteExercises(deletedExercises)
        }

        val currentExercisesByUuid = currentExercises.associateBy { it.uuid }

        exercises.forEach { exercise ->
            val currentExercise = currentExercisesByUuid[exercise.uuid]

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
                val weight = set.weight.convertWeightToDatabase()
                val setInfoChanged =
                    currentSet?.weight != weight || currentSet.repetitions != set.repetitions

                if (currentSet?.id == null) {
                    setDao.insert(
                        SetEntity(
                            exerciseId = exerciseId,
                            uuid = set.uuid,
                            weight = weight,
                            repetitions = set.repetitions
                        )
                    ).toInt()
                } else if (setInfoChanged) {
                    setDao.updateSet(
                        currentSet.copy(
                            weight = weight,
                            repetitions = set.repetitions
                        )
                    )
                }
            }
        }
    }

    suspend fun getLatestWorkoutWithExercises(workoutId: Int): WorkoutWithExercises? {
        val workout = workoutDao.getById(workoutId)
        val timestamp = sessionDao.getLastSession(workoutId)?.timestamp

        val exercises = exerciseDao.getExercisesByWorkoutId(workoutId).map { exercise ->
            val sets = setDao.getSetsForExercise(exercise.id)

            Exercise(
                uuid = exercise.uuid,
                name = exercise.name,
                description = exercise.description,
                sets = sets.map { set ->
                    WorkoutSet(
                        uuid = set.uuid,
                        weight = set.weight.convertWeightFromDatabase(),
                        repetitions = set.repetitions
                    )
                }
            )
        }

        return WorkoutWithExercises(
            id = workoutId,
            name = workout?.name ?: "",
            timestamp = timestamp,
            exercises = exercises
        )
    }

    suspend fun deleteWorkout(splitId: Int) {
        workoutDao.deleteById(splitId)
    }
}
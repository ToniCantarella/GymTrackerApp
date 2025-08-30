package com.example.gymtracker.repository.gym

import com.example.gymtracker.database.dao.gym.ExerciseDao
import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.dao.gym.GymWorkoutDao
import com.example.gymtracker.database.dao.gym.SetDao
import com.example.gymtracker.database.entity.gym.ExerciseEntity
import com.example.gymtracker.database.entity.gym.GymWorkoutEntity
import com.example.gymtracker.database.entity.gym.SetEntity
import com.example.gymtracker.ui.gym.entity.Exercise
import com.example.gymtracker.ui.gym.entity.WorkoutSet
import java.time.Instant

data class WorkoutWithExercises(
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

interface GymWorkoutRepository {
    suspend fun getGymWorkouts(): List<WorkoutWithLatestTimestamp>
    suspend fun addGymWorkout(workoutName: String, exercises: List<Exercise>)
    suspend fun updateGymWorkout(workoutId: Int, workoutName: String, exercises: List<Exercise>)
    suspend fun getLatestWorkoutWithExercises(id: Int): WorkoutWithExercises?
    suspend fun deleteGymWorkout(splitId: Int)
}

class GymWorkoutRepositoryImpl(
    private val workoutDao: GymWorkoutDao,
    private val sessionDao: GymSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
) : GymWorkoutRepository {

    override suspend fun getGymWorkouts(): List<WorkoutWithLatestTimestamp> {
        val workouts = workoutDao.getAll()

        return workouts.map {
            val timestamp = sessionDao.getLastSession(it.id)?.timestamp

            WorkoutWithLatestTimestamp(
                id = it.id,
                name = it.name,
                latestTimestamp = timestamp
            )
        }
    }

    override suspend fun getLatestWorkoutWithExercises(id: Int): WorkoutWithExercises? {
        val workout = workoutDao.getById(id)
        val timestamp = sessionDao.getLastSession(id)?.timestamp

        val exercises = exerciseDao.getExercisesByWorkoutId(id).map { exercise ->
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
            id = id,
            name = workout?.name ?: "",
            timestamp = timestamp,
            exercises = exercises
        )
    }

    override suspend fun addGymWorkout(workoutName: String, exercises: List<Exercise>) {
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
    }

    // TODO this tie to the save button
    override suspend fun updateGymWorkout(
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

    override suspend fun deleteGymWorkout(splitId: Int) = workoutDao.deleteById(splitId)
}
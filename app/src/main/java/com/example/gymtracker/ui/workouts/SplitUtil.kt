package com.example.gymtracker.ui.workouts

import com.example.gymtracker.ui.workouts.entity.Exercise
import com.example.gymtracker.ui.workouts.entity.WorkoutSet
import java.util.UUID

const val MAX_SPLITS = 7
const val MAX_EXERCISES = 10
const val MAX_SETS = 6

class SplitUtil {

    fun updateExerciseName(
        exercises: List<Exercise>,
        id: UUID,
        name: String
    ): List<Exercise> = exercises.map { exercise ->
        if (exercise.uuid == id) {
            exercise.copy(name = name)
        } else {
            exercise
        }
    }

    fun updateExerciseDescription(
        exercises: List<Exercise>,
        id: UUID,
        description: String
    ): List<Exercise> = exercises.map { exercise ->
        if (exercise.uuid == id) {
            exercise.copy(description = description)
        } else {
            exercise
        }
    }

    fun addSet(
        exercises: List<Exercise>,
        id: UUID
    ): List<Exercise> = exercises.map { exercise ->
        if (exercise.uuid == id) {
            exercise.copy(
                sets = exercise.sets + WorkoutSet(
                    uuid = UUID.randomUUID(),
                    weight = exercise.sets.last().weight,
                    repetitions = exercise.sets.last().repetitions
                )
            )
        } else {
            exercise
        }
    }

    fun removeSet(
        exercises: List<Exercise>,
        exerciseId: UUID,
        setId: UUID
    ): List<Exercise> = exercises.map { exercise ->
        if (exercise.uuid == exerciseId) {
            exercise.copy(
                sets = exercise.sets.filter { set ->
                    set.uuid != setId
                }
            )
        } else {
            exercise
        }
    }

    fun updateWeight(
        exercises: List<Exercise>,
        exerciseId: UUID,
        setId: UUID,
        weight: Double
    ): List<Exercise> = exercises.map { exercise ->
        if (exercise.uuid == exerciseId) {
            exercise.copy(
                sets = exercise.sets.map { set ->
                    if (set.uuid == setId) {
                        set.copy(
                            weight = weight
                        )
                    } else set
                }
            )
        } else {
            exercise
        }
    }

    fun updateRepetitions(
        exercises: List<Exercise>,
        exerciseId: UUID,
        setId: UUID,
        repetitions: Int
    ): List<Exercise> = exercises.map { exercise ->
        if (exercise.uuid == exerciseId) {
            exercise.copy(
                sets = exercise.sets.map { set ->
                    if (set.uuid == setId) {
                        set.copy(
                            repetitions = repetitions
                        )
                    } else set
                }
            )
        } else {
            exercise
        }
    }

    fun checkSet(
        exercises: List<Exercise>,
        exerciseId: UUID,
        setId: UUID,
        checked: Boolean
    ): List<Exercise> = exercises.map { exercise ->
        if (exercise.uuid == exerciseId) {
            exercise.copy(
                sets = exercise.sets.map { set ->
                    if (set.uuid == setId) {
                        set.copy(
                            checked = checked
                        )
                    } else set
                }
            )
        } else {
            exercise
        }
    }
}
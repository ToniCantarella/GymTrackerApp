package com.example.gymtracker.ui.workouts.createsplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.database.repository.WorkoutRepository
import com.example.gymtracker.ui.workouts.split.Exercise
import com.example.gymtracker.ui.workouts.split.WorkoutSet
import com.example.gymtracker.ui.workouts.split.emptyExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CreateSplitUiState(
    val splitName: String = "",
    val exercises: List<Exercise> = listOf(emptyExercise())
)

class CreateSplitViewModel (
    private val workoutRepository: WorkoutRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(CreateSplitUiState())
    val uiState = _uiState.asStateFlow()

    fun onCreateSplitPressed(onCreateDone: () -> Unit) {
        viewModelScope.launch {
            workoutRepository.addSplitWithExercises(
                splitName = uiState.value.splitName,
                exercises = uiState.value.exercises
            )
            onCreateDone()
        }
    }

    fun onSplitNameChange(name: String) {
        _uiState.update {
            it.copy(
                splitName = name
            )
        }
    }

    fun addExercise() {
        _uiState.update {
            it.copy(
                exercises = it.exercises + listOf(emptyExercise())
            )
        }
    }

    fun onExerciseNameChange(id: UUID, name: String) {
        _uiState.update {
            it.copy(
                exercises = it.exercises.map { exercise ->
                    if (exercise.uuid == id) {
                        exercise.copy(name = name)
                    } else {
                        exercise
                    }
                }
            )
        }
    }

    fun onDescriptionChange(id: UUID, description: String) {
        _uiState.update {
            it.copy(
                exercises = it.exercises.map { exercise ->
                    if (exercise.uuid == id) {
                        exercise.copy(description = description)
                    } else {
                        exercise
                    }
                }
            )
        }
    }

    fun addSet(exerciseId: UUID) {
        _uiState.update {
            it.copy(
                exercises = it.exercises.map { exercise ->
                    if (exercise.uuid == exerciseId) {
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
            )
        }
    }

    fun onRemoveSet(exerciseId: UUID, setId: UUID) {
        _uiState.update {
            it.copy(
                exercises = it.exercises.map { exercise ->
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
            )
        }
    }

    fun onChangeWeight(exerciseId: UUID, setId: UUID, weight: Double) {
        _uiState.update {
            it.copy(
                exercises = it.exercises.map { exercise ->
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
            )
        }
    }

    fun onChangeRepetitions(exerciseId: UUID, setId: UUID, repetitions: Int) {
        _uiState.update {
            it.copy(
                exercises = it.exercises.map { exercise ->
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
            )
        }
    }
}
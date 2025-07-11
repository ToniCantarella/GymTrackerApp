package com.example.gymtracker.ui.workouts.split

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.database.repository.WorkoutRepository
import com.example.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class Exercise(
    val uuid: UUID,
    val name: String,
    val description: String?,
    val sets: List<WorkoutSet>
)

data class WorkoutSet(
    val uuid: UUID,
    val weight: Double,
    val repetitions: Int
)

data class SplitUiState(
    val loading: Boolean = true,
    val splitName: String = "",
    val adding: Boolean = false,
    val exercises: List<Exercise> = listOf(
        Exercise(
            uuid = UUID.randomUUID(),
            name = "",
            description = null,
            sets = listOf(
                WorkoutSet(
                    uuid = UUID.randomUUID(),
                    weight = 0.0,
                    repetitions = 0
                )
            )
        )
    ),
    val setsPerformed: List<WorkoutSet> = emptyList()
)

class SplitViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.Split>()

    private val _uiState =
        MutableStateFlow(SplitUiState(adding = navParams.adding))
    val uiState = _uiState.asStateFlow()

    init {
        if (navParams.id != null) {
            viewModelScope.launch {
                val lastPerformedSplit = workoutRepository.getLastPerformedSplit(navParams.id)
                _uiState.update {
                    it.copy(
                        splitName = lastPerformedSplit?.name ?: "",
                        exercises = lastPerformedSplit?.exercises ?: emptyList(),
                        loading = false
                    )
                }
            }
        }
    }

    fun onCreateSplitPressed() {
        viewModelScope.launch {
            workoutRepository.addSplitWithExercises(
                splitName = uiState.value.splitName,
                exercises = uiState.value.exercises
            )
        }
    }

    // TODO RENAME
    private fun findAndCollectPerformedExercises(): List<Exercise> {
        val exercises = uiState.value.exercises
        val performedSets = uiState.value.setsPerformed

        return exercises.map { exercise ->
            val sets =
                performedSets.filter { performedSet -> exercise.sets.any { set -> performedSet.uuid == set.uuid } }
            Exercise(
                uuid = exercise.uuid,
                name = exercise.name,
                description = exercise.description,
                sets = sets
            )
        }
    }

    fun onFinishWorkoutPressed() {
        if (navParams.id != null) {
            viewModelScope.launch {
                val exercisesPerformed = findAndCollectPerformedExercises()

                workoutRepository.markSessionDone(
                    splitId = navParams.id,
                    exercisesPerformed = exercisesPerformed
                )
            }
        }
    }

    fun onCheckSet(workoutSet: WorkoutSet, checked: Boolean) {
        Log.d("toni", "$workoutSet")
        if (checked) {
            _uiState.update {
                it.copy(
                    setsPerformed = it.setsPerformed + listOf(workoutSet)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    setsPerformed = it.setsPerformed.filter { set -> set.uuid != workoutSet.uuid }
                )
            }
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
                exercises = it.exercises + listOf(
                    Exercise(
                        uuid = UUID.randomUUID(),
                        name = "",
                        description = null,
                        sets = listOf(
                            WorkoutSet(
                                uuid = UUID.randomUUID(),
                                weight = 0.0,
                                repetitions = 0
                            )
                        )
                    )
                )
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

    fun addSet(id: UUID) {
        _uiState.update {
            it.copy(
                exercises = it.exercises.map { exercise ->
                    if (exercise.uuid == id) {
                        exercise.copy(
                            sets = exercise.sets + WorkoutSet(
                                uuid = UUID.randomUUID(),
                                weight = 0.0,
                                repetitions = 0
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
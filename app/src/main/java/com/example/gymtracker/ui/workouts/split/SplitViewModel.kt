package com.example.gymtracker.ui.workouts.split

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.example.gymtracker.ui.navigation.Route
import com.example.gymtracker.ui.theme.GymTrackerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class Exercise(
    val exerciseId: UUID,
    val name: String,
    val description: String?,
    val sets: List<Set>
)

data class Set(
    val setId: UUID,
    val weight: Double,
    val repetitions: Int
)

data class SplitUiState(
    val splitName: String = "",
    val adding: Boolean = false,
    val exercises: List<Exercise> = listOf(
        Exercise(
            exerciseId = UUID.randomUUID(),
            name = "",
            description = null,
            sets = listOf(
                Set(
                    setId = UUID.randomUUID(),
                    weight = 0.0,
                    repetitions = 0
                )
            )
        )
    )
)

class SplitViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val split = savedStateHandle.toRoute<Route.Split>()

    private val _uiState = MutableStateFlow(SplitUiState(splitName = split.name ?: "", adding = split.adding))
    val uiState = _uiState.asStateFlow()

    fun onDonePressed() {

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
                        exerciseId = UUID.randomUUID(),
                        name = "",
                        description = null,
                        sets = listOf(
                            Set(
                                setId = UUID.randomUUID(),
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
                    if (exercise.exerciseId == id) {
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
                    if (exercise.exerciseId == id) {
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
                    if (exercise.exerciseId == id) {
                        exercise.copy(
                            sets = exercise.sets + Set(
                                setId = UUID.randomUUID(),
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
                    if (exercise.exerciseId == exerciseId) {
                        exercise.copy(
                            sets = exercise.sets.filter { set ->
                                set.setId != setId
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
                    if (exercise.exerciseId == exerciseId) {
                        exercise.copy(
                            sets = exercise.sets.map { set ->
                                if (set.setId == setId) {
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
                    if (exercise.exerciseId == exerciseId) {
                        exercise.copy(
                            sets = exercise.sets.map { set ->
                                if (set.setId == setId) {
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

@Composable
private fun ScreenForPreview() {
    SplitScreen(
        exercises = listOf(
            Exercise(
                exerciseId = UUID.randomUUID(),
                name = "",
                description = null,
                sets = listOf(
                    Set(
                        setId = UUID.randomUUID(),
                        weight = 0.0,
                        repetitions = 0
                    ),
                    Set(
                        setId = UUID.randomUUID(),
                        weight = 0.0,
                        repetitions = 0
                    )
                )
            )
        ),
        addingSplit = false,
        addExercise = {},
        onExerciseNameChange = { _, _ -> },
        onDescriptionChange = { _, _ -> },
        addSet = { },
        onChangeWeight = { _, _, _ -> },
        onChangeRepetitions = { _, _, _ -> },
        onRemoveSet = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
private fun AddSplitPreview() {
    GymTrackerTheme {
        ScreenForPreview()
    }
}

@Preview(showBackground = true, locale = "fi")
@Composable
private fun AddSplitPreviewFi() {
    GymTrackerTheme {
        ScreenForPreview()
    }
}
package com.example.gymtracker.ui.workouts.split

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.database.repository.WorkoutRepository
import com.example.gymtracker.ui.navigation.Route
import com.example.gymtracker.ui.workouts.SplitUtil
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
    val checked: Boolean = false,
    val weight: Double,
    val repetitions: Int
)

fun emptyExercise() = Exercise(
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

data class SplitUiState(
    val loading: Boolean = false,
    val splitName: String = "",
    val exercises: List<Exercise> = listOf(emptyExercise())
)

const val MAX_EXERCISES = 10
const val MAX_SETS = 6

class SplitViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val splitUtil = SplitUtil()
    private val navParams = savedStateHandle.toRoute<Route.Split>()

    private val _uiState = MutableStateFlow(SplitUiState())
    val uiState = _uiState.asStateFlow()

    init {
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

    fun onSplitNameChange(name: String) {
        _uiState.update {
            it.copy(
                splitName = name
            )
        }
    }

    fun onExerciseNameChange(id: UUID, name: String) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.updateExerciseName(it.exercises, id, name)
            )
        }
    }

    fun onDescriptionChange(id: UUID, description: String) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.updateExerciseDescription(it.exercises, id, description)
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

    fun onCheckSet(exerciseId: UUID, setId: UUID, checked: Boolean) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.checkSet(it.exercises, exerciseId, setId, checked)
            )
        }
    }

    fun addSet(exerciseId: UUID) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.addSet(it.exercises, exerciseId)
            )
        }
    }

    fun onRemoveSet(exerciseId: UUID, setId: UUID) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.removeSet(it.exercises, exerciseId, setId)
            )
        }
    }

    fun onChangeWeight(exerciseId: UUID, setId: UUID, weight: Double) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.updateWeight(it.exercises, exerciseId, setId, weight)
            )
        }
    }

    fun onChangeRepetitions(exerciseId: UUID, setId: UUID, repetitions: Int) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.updateRepetitions(
                    it.exercises,
                    exerciseId,
                    setId,
                    repetitions
                )
            )
        }
    }

    fun onFinishWorkoutPressed() {
        viewModelScope.launch {
            val exercisesPerformed = findAndCollectPerformedExercises()

            workoutRepository.markSessionDone(
                splitId = navParams.id,
                exercisesPerformed = exercisesPerformed
            )
        }
    }

    private fun findAndCollectPerformedExercises(): List<Exercise> {
        val exercises = uiState.value.exercises

        return exercises.map { exercise ->
            Exercise(
                uuid = exercise.uuid,
                name = exercise.name,
                description = exercise.description,
                sets = exercise.sets.filter { set -> set.checked }
            )
        }
    }
}

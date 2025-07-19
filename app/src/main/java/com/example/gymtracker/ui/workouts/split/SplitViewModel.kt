package com.example.gymtracker.ui.workouts.split

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.database.repository.WorkoutRepository
import com.example.gymtracker.ui.navigation.Route
import com.example.gymtracker.ui.workouts.SplitUtil
import com.example.gymtracker.ui.workouts.entity.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

data class SplitUiState(
    val loading: Boolean = false,
    val splitName: String = "",
    val latestTimestamp: Instant? = null,
    val exercises: List<Exercise> = emptyList()
)

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
            val latestSplit = workoutRepository.getLatestSplitWithExercises(navParams.id)
            _uiState.update {
                it.copy(
                    splitName = latestSplit?.name ?: "",
                    latestTimestamp = latestSplit?.timestamp,
                    exercises = latestSplit?.exercises ?: emptyList(),
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
                exercises = it.exercises + listOf(Exercise.emptyExercise())
            )
        }
    }

    fun onRemoveExercise(exerciseId: UUID) {
        _uiState.update {
            it.copy(
                exercises = it.exercises.filter { exercise -> exercise.uuid != exerciseId }
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

    fun onFinishWorkoutPressed(onFinish: () -> Unit) {
        viewModelScope.launch {
            workoutRepository.markSessionDone(
                splitId = navParams.id,
                splitName = uiState.value.splitName,
                exercises = uiState.value.exercises
            )
            onFinish()
        }
    }
}

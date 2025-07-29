package com.example.gymtracker.ui.workouts.createsplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.database.repository.GymRepository
import com.example.gymtracker.ui.workouts.entity.Exercise
import com.example.gymtracker.utility.SplitUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CreateSplitUiState(
    val splitName: String = "",
    val exercises: List<Exercise> = listOf(Exercise.emptyExercise())
)

class CreateSplitViewModel(
    private val gymRepository: GymRepository
) : ViewModel() {
    private val splitUtil = SplitUtil()
    private val _uiState = MutableStateFlow(CreateSplitUiState())
    val uiState = _uiState.asStateFlow()

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

    fun onCreateSplitPressed(onCreateDone: () -> Unit) {
        viewModelScope.launch {
            gymRepository.addSplitWithExercises(
                splitName = uiState.value.splitName,
                exercises = uiState.value.exercises
            )
            onCreateDone()
        }
    }
}

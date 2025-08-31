package com.example.gymtracker.ui.gym.creategymworkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.repository.gym.GymWorkoutRepository
import com.example.gymtracker.ui.entity.gym.Exercise
import com.example.gymtracker.utility.GymWorkoutUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID


data class CreateGymWorkoutUiState(
    val workoutName: String = "",
    val exercises: List<Exercise> = defaultExercises,
    val initialExercises: List<Exercise> = defaultExercises
){
    companion object {
        val defaultExercises = listOf(Exercise.emptyExercise())
    }
}

class CreateGymWorkoutViewModel(
    private val workoutRepository: GymWorkoutRepository
) : ViewModel() {
    private val gymWorkoutUtil = GymWorkoutUtil()
    private val _uiState = MutableStateFlow(CreateGymWorkoutUiState())
    val uiState = _uiState.asStateFlow()

    fun onWorkoutNameChange(name: String) {
        _uiState.update {
            it.copy(
                workoutName = name
            )
        }
    }

    fun onExerciseNameChange(id: UUID, name: String) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.updateExerciseName(it.exercises, id, name)
            )
        }
    }

    fun onDescriptionChange(id: UUID, description: String) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.updateExerciseDescription(it.exercises, id, description)
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
                exercises = gymWorkoutUtil.addSet(it.exercises, exerciseId)
            )
        }
    }

    fun onRemoveSet(exerciseId: UUID, setId: UUID) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.removeSet(it.exercises, exerciseId, setId)
            )
        }
    }

    fun onChangeWeight(exerciseId: UUID, setId: UUID, weight: Double) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.updateWeight(it.exercises, exerciseId, setId, weight)
            )
        }
    }

    fun onChangeRepetitions(exerciseId: UUID, setId: UUID, repetitions: Int) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.updateRepetitions(
                    it.exercises,
                    exerciseId,
                    setId,
                    repetitions
                )
            )
        }
    }

    fun onCreateWorkoutPressed(onCreateDone: () -> Unit) {
        viewModelScope.launch {
            workoutRepository.addWorkout(
                workoutName = uiState.value.workoutName,
                exercises = uiState.value.exercises
            )
            onCreateDone()
        }
    }
}

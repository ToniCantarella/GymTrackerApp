package com.tonicantarella.gymtracker.ui.gym.creategymworkout

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonicantarella.gymtracker.preferences.GymPreferences
import com.tonicantarella.gymtracker.repository.gym.GymWorkoutRepository
import com.tonicantarella.gymtracker.ui.entity.gym.Exercise
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import com.tonicantarella.gymtracker.utility.GymWorkoutUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID


data class CreateGymWorkoutUiState(
    val workoutName: String = "",
    val exercises: List<Exercise> = defaultExercises,
    val initialExercises: List<Exercise> = defaultExercises,
    val unSavedChangesDialogOpen: Boolean = false,
    val confirmUnsavedChanges: Boolean = true
){
    companion object {
        val defaultExercises = listOf(Exercise.emptyExercise())
    }
    val hasUnsavedChanges = workoutName.isNotEmpty() || exercises != initialExercises
}

class CreateGymWorkoutViewModel(
    private val workoutRepository: GymWorkoutRepository,
    private val dataStore: DataStore<Preferences>,
    private val navigator: Navigator
) : ViewModel() {
    private val gymWorkoutUtil = GymWorkoutUtil()
    private val _uiState = MutableStateFlow(CreateGymWorkoutUiState())
    val uiState = _uiState.asStateFlow()

    init {
        registerNavigationGuard()
        registerNavigationAttempts()
        getPreferences()
    }

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

    fun getPreferences() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                preferences[GymPreferences.SHOW_UNSAVED_CHANGES_DIALOG] ?: true
            }.distinctUntilChanged().collect { showDialog ->
                _uiState.update {
                    it.copy(
                        confirmUnsavedChanges = showDialog
                    )
                }
            }
        }
    }

    fun onCreateWorkoutPressed() {
        viewModelScope.launch {
            workoutRepository.addWorkout(
                workoutName = uiState.value.workoutName,
                exercises = uiState.value.exercises
            )
        }
        navigator.releaseGuard()
        navigator.popBackStack()
    }

    fun onNavigateBack() {
        navigator.popBackStack()
    }

    fun dismissUnsavedChangesDialog() {
        _uiState.update {
            it.copy(
                unSavedChangesDialogOpen = false
            )
        }
    }

    fun onConfirmUnsavedChangesDialog(doNotAskAgain: Boolean) {
        _uiState.update {
            it.copy(
                unSavedChangesDialogOpen = false
            )
        }
        if (doNotAskAgain) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[GymPreferences.SHOW_UNSAVED_CHANGES_DIALOG] = false
                }
            }
        }
        navigator.releaseGuard()
    }

    private fun registerNavigationGuard() {
        viewModelScope.launch {
            uiState
                .map { it.hasUnsavedChanges }
                .distinctUntilChanged()
                .collect { hasChanges ->
                    navigator.guard(hasChanges)
                }
        }
    }

    private fun registerNavigationAttempts() {
        viewModelScope.launch {
            navigator.navigationAttempts.collect { direction ->
                if (navigator.isGuarded && uiState.value.confirmUnsavedChanges) {
                    _uiState.update {
                        it.copy(
                            unSavedChangesDialogOpen = true
                        )
                    }
                }
            }
        }
    }
}

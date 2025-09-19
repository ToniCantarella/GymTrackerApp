package com.tonicantarella.gymtracker.ui.gym.gymworkout

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tonicantarella.gymtracker.preferences.GymPreferences
import com.tonicantarella.gymtracker.repository.gym.GymSessionRepository
import com.tonicantarella.gymtracker.repository.gym.GymStatsRepository
import com.tonicantarella.gymtracker.repository.gym.GymWorkoutRepository
import com.tonicantarella.gymtracker.ui.entity.gym.Exercise
import com.tonicantarella.gymtracker.ui.entity.gym.GymWorkoutStats
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import com.tonicantarella.gymtracker.ui.navigation.Route
import com.tonicantarella.gymtracker.utility.GymWorkoutUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

data class GymWorkoutUiState(
    val loading: Boolean = false,
    val workoutId: Int = 0,
    val workoutName: String = "",
    val initialWorkoutName: String = "",
    val latestTimestamp: Instant? = null,
    val exercises: List<Exercise> = emptyList(),
    val initialExercises: List<Exercise> = emptyList(),
    val showFinishWorkoutDialog: Boolean = true,
    val sessionTimestamp: Instant? = null,
    val stats: GymWorkoutStats? = null,
    val finishWorkoutDialogOpen: Boolean = false,
    val unSavedChangesDialogOpen: Boolean = false
) {
    val hasUnsavedChanges = initialWorkoutName != workoutName || initialExercises != exercises
    val hasPerformedSets = exercises.any { it.sets.any { set -> set.checked } }
    val hasChanges = hasUnsavedChanges || hasPerformedSets
}

class GymWorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: GymWorkoutRepository,
    private val statsRepository: GymStatsRepository,
    private val sessionRepository: GymSessionRepository,
    private val dataStore: DataStore<Preferences>,
    private val navigator: Navigator
) : ViewModel() {
    private val gymWorkoutUtil = GymWorkoutUtil()
    private val navParams = savedStateHandle.toRoute<Route.GymWorkout>()

    private val _uiState = MutableStateFlow(GymWorkoutUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            initScreen()
        }
    }

    fun navigateBack() {
        navigator.popBackStack()
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

    fun onCheckSet(exerciseId: UUID, setId: UUID, checked: Boolean) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.checkSet(it.exercises, exerciseId, setId, checked)
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
                // TODO what should set this to true? Certainly not the navigator, right?
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

    fun saveChanges() {
        viewModelScope.launch {
            workoutRepository.updateWorkout(
                workoutId = navParams.id,
                workoutName = uiState.value.workoutName,
                exercises = uiState.value.exercises
            )
        }
        navigator.releaseGuard()
        navigateBack()
    }

    fun stopAskingFinishConfirm() {
        viewModelScope.launch {
            if (uiState.value.showFinishWorkoutDialog) {
                dataStore.edit { preferences ->
                    preferences[GymPreferences.SHOW_FINISH_WORKOUT_CONFIRM_DIALOG] = false
                }
            }
        }
    }

    fun dismissFinishWorkoutDialog() {
        _uiState.update {
            it.copy(
                finishWorkoutDialogOpen = false
            )
        }
    }

    fun finishWorkoutCheck() {
        if (uiState.value.hasPerformedSets) {
            _uiState.update {
             it.copy(
                 finishWorkoutDialogOpen = true
             )
            }
        } else {
            onFinishWorkoutPressed()
        }
    }

    fun onFinishWorkoutPressed() {
        viewModelScope.launch {
            saveChanges()

            sessionRepository.markSessionDone(
                workoutId = navParams.id,
                exercises = uiState.value.exercises,
            )
        }
        navigator.releaseGuard()
        navigateBack()
    }

    private suspend fun initScreen() {
        getWorkoutInfo()
        getPreferences()
        getStats()
        startNavigationGuardFoo()
    }

    private suspend fun getWorkoutInfo() {
        val latestWorkout = workoutRepository.getLatestWorkoutWithExercises(navParams.id)
        val workoutName = latestWorkout?.name ?: ""
        val exercises = latestWorkout?.exercises ?: emptyList()
        val sessionTimestamp = navParams.timestampString?.let { Instant.parse(it) }

        _uiState.update {
            it.copy(
                workoutId = navParams.id,
                workoutName = workoutName,
                initialWorkoutName = workoutName,
                latestTimestamp = latestWorkout?.timestamp,
                exercises = exercises,
                initialExercises = exercises,
                sessionTimestamp = sessionTimestamp,
                loading = false
            )
        }
    }

    private suspend fun getPreferences() {
        val showFinishWorkoutDialog = dataStore.data
            .map { it[GymPreferences.SHOW_FINISH_WORKOUT_CONFIRM_DIALOG] ?: true }
            .first()

        _uiState.update {
            it.copy(
                showFinishWorkoutDialog = showFinishWorkoutDialog
            )
        }
    }

    private suspend fun getStats() {
        val workoutStats = statsRepository.getWorkoutStats(navParams.id)

        _uiState.update {
            it.copy(
                stats = workoutStats
            )
        }
    }

    // TODO rename
    private suspend fun startNavigationGuardFoo() {
        uiState
            .map { it.hasChanges }
            .distinctUntilChanged()
            .collect { hasChanges ->
                navigator.guard(hasChanges)
            }
    }
}

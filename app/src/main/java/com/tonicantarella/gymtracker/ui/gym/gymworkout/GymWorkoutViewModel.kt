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
    val loading: Boolean = true,
    val workoutId: Int = 0,
    val workoutName: String = "",
    val initialWorkoutName: String = "",
    val latestTimestamp: Instant? = null,
    val exercises: List<Exercise> = emptyList(),
    val initialExercises: List<Exercise> = emptyList(),
    val sessionTimestamp: Instant? = null,
    val stats: GymWorkoutStats? = null,
    val confirmFinishWorkout: Boolean = true,
    val finishWorkoutDialogOpen: Boolean = false,
    val confirmUnsavedChanges: Boolean = true,
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
            getWorkoutInfo()
            getPreferences()
            getStats()
        }
        registerNavigationGuard()
        registerNavigationAttempts()
    }

    fun onNavigateBack() {
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

    fun onSavePressed() {
        viewModelScope.launch {
            if (navParams.id != null) {
                saveChanges(navParams.id)
            } else {
                createWorkout()
            }
        }
        navigator.releaseGuard()
        onNavigateBack()
    }

    fun dismissFinishWorkoutDialog() {
        _uiState.update {
            it.copy(
                finishWorkoutDialogOpen = false
            )
        }
    }

    fun onFinishPressed() {
        if (uiState.value.hasPerformedSets && uiState.value.confirmFinishWorkout) {
            _uiState.update {
                it.copy(
                    finishWorkoutDialogOpen = true
                )
            }
        } else {
            finishWorkout()
        }
    }

    fun onConfirmFinishWorkoutDialog(doNotAskAgain: Boolean) {
        dismissFinishWorkoutDialog()
        if (doNotAskAgain) {
            stopAskingFinishConfirm()
        }
        finishWorkout()
    }

    private fun finishWorkout() {
        viewModelScope.launch {
            val workoutId = if (navParams.id != null) {
                saveChanges(navParams.id)
                navParams.id
            } else {
                createWorkout()
            }

            sessionRepository.markSessionDone(
                workoutId = workoutId,
                exercises = uiState.value.exercises,
                timestamp = uiState.value.sessionTimestamp
            )
        }
        navigator.releaseGuard()
        onNavigateBack()
    }

    private suspend fun getWorkoutInfo() {
        if (navParams.id != null) {
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
        } else {
            _uiState.update {
                it.copy(
                    exercises = listOf(
                        Exercise.emptyExercise()
                    ),
                    loading = false
                )
            }
        }
    }

    private suspend fun getPreferences() {
        val showFinishWorkoutDialog = dataStore.data
            .map { it[GymPreferences.SHOW_FINISH_WORKOUT_CONFIRM_DIALOG] ?: true }
            .first()

        val showUnsavedChangesDialog = dataStore.data
            .map { it[GymPreferences.SHOW_UNSAVED_CHANGES_DIALOG] ?: true }
            .first()

        _uiState.update {
            it.copy(
                confirmFinishWorkout = showFinishWorkoutDialog,
                confirmUnsavedChanges = showUnsavedChangesDialog
            )
        }
    }

    private suspend fun getStats() {
        if (navParams.id != null) {
            val workoutStats = statsRepository.getWorkoutStats(navParams.id)

            _uiState.update {
                it.copy(
                    stats = workoutStats
                )
            }
        }
    }

    private fun stopAskingFinishConfirm() {
        viewModelScope.launch {
            if (uiState.value.confirmFinishWorkout) {
                dataStore.edit { preferences ->
                    preferences[GymPreferences.SHOW_FINISH_WORKOUT_CONFIRM_DIALOG] = false
                }
            }
        }
    }

    private suspend fun createWorkout(): Int {
        return workoutRepository.addWorkout(
            workoutName = uiState.value.workoutName,
            exercises = uiState.value.exercises
        )
    }

    private suspend fun saveChanges(workoutId: Int) {
        workoutRepository.updateWorkout(
            workoutId = workoutId,
            workoutName = uiState.value.workoutName,
            exercises = uiState.value.exercises
        )
    }

    private fun registerNavigationGuard() {
        viewModelScope.launch {
            uiState
                .map { it.hasChanges }
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

package com.tonicantarella.gymtracker.ui.cardio.cardioworkout

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tonicantarella.gymtracker.preferences.GymPreferences
import com.tonicantarella.gymtracker.repository.cardio.CardioSessionRepository
import com.tonicantarella.gymtracker.repository.cardio.CardioStatsRepository
import com.tonicantarella.gymtracker.repository.cardio.CardioWorkoutRepository
import com.tonicantarella.gymtracker.ui.entity.cardio.CardioMetrics
import com.tonicantarella.gymtracker.ui.entity.cardio.CardioWorkoutStats
import com.tonicantarella.gymtracker.ui.entity.cardio.WorkoutWithMetrics
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import com.tonicantarella.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

data class CardioWorkoutUiState(
    val loading: Boolean = true,
    val workout: WorkoutWithMetrics = emptyWorkoutWithMetrics,
    val initialWorkout: WorkoutWithMetrics = emptyWorkoutWithMetrics,
    val previousWorkout: WorkoutWithMetrics? = null,
    val sessionTimestamp: Instant? = null,
    val confirmUnsavedChanges: Boolean = true,
    val unSavedChangesDialogOpen: Boolean = false,
    val confirmFinishWorkout: Boolean = true,
    val finishWorkoutDialogOpen: Boolean = false,
    val stats: CardioWorkoutStats? = null
) {
    val hasUnsavedChanges: Boolean = workout.name != initialWorkout.name
    val hasMarkedMetrics: Boolean = workout.metrics != initialWorkout.metrics
    val hasChanges = hasUnsavedChanges || hasMarkedMetrics

    companion object {
        val emptyMetrics = CardioMetrics(
            steps = 0,
            distance = 0.0,
            duration = Duration.ZERO
        )
        val emptyWorkoutWithMetrics = WorkoutWithMetrics(
            id = 0,
            name = "",
            timestamp = null,
            metrics = emptyMetrics
        )
    }
}

class CardioWorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: CardioWorkoutRepository,
    private val statsRepository: CardioStatsRepository,
    private val sessionRepository: CardioSessionRepository,
    private val dataStore: DataStore<Preferences>,
    private val navigator: Navigator
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.CardioWorkout>()

    private val _uiState = MutableStateFlow(CardioWorkoutUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getWorkout()
        registerNavigationGuard()
        registerNavigationAttempts()
    }

    fun onNavigateBack() {
        navigator.popBackStack()
    }

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                workout = it.workout.copy(name = name)
            )
        }
    }

    fun onStepsChange(steps: Int) {
        _uiState.update {
            it.copy(
                workout = it.workout.copy(
                    metrics = it.workout.metrics.copy(
                        steps = steps
                    )
                )
            )
        }
    }

    fun onDistanceChange(distance: Double) {
        _uiState.update {
            it.copy(
                workout = it.workout.copy(
                    metrics = it.workout.metrics.copy(
                        distance = distance
                    )
                )
            )
        }
    }

    fun onDurationChange(duration: Duration) {
        _uiState.update {
            it.copy(
                workout = it.workout.copy(
                    metrics = it.workout.metrics.copy(
                        duration = duration
                    )
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
            saveChanges()
        }
        navigator.releaseGuard()
        navigator.popBackStack()
    }

    fun dismissFinishWorkoutDialog() {
        _uiState.update {
            it.copy(
                finishWorkoutDialogOpen = false
            )
        }
    }

    fun onFinishWorkoutPressed() {
        if (uiState.value.hasMarkedMetrics && uiState.value.confirmFinishWorkout) {
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

    private fun getWorkout() {
        viewModelScope.launch {
            val latestWorkoutWithMetrics =
                workoutRepository.getLatestWorkoutWithMetrics(navParams.id)
            val sessionTimestamp = navParams.timestampString?.let { Instant.parse(it) }

            val currentWorkout = uiState.value.workout.copy(
                id = latestWorkoutWithMetrics?.id ?: 0,
                name = latestWorkoutWithMetrics?.name ?: "",
                timestamp = latestWorkoutWithMetrics?.timestamp
            )

            val showFinishWorkoutDialog = dataStore.data
                .map { it[GymPreferences.SHOW_FINISH_WORKOUT_CONFIRM_DIALOG] ?: true }
                .first()

            _uiState.update {
                it.copy(
                    workout = currentWorkout,
                    initialWorkout = currentWorkout,
                    previousWorkout = latestWorkoutWithMetrics,
                    sessionTimestamp = sessionTimestamp,
                    confirmFinishWorkout = showFinishWorkoutDialog,
                    loading = false
                )
            }

            val workoutStats = statsRepository.getWorkoutStats(navParams.id)

            _uiState.update {
                it.copy(
                    stats = workoutStats
                )
            }
        }
    }

    private fun finishWorkout() {
        viewModelScope.launch {
            saveChanges()
            sessionRepository.markSessionDone(
                workoutId = navParams.id,
                metrics = uiState.value.workout.metrics
            )
        }
        navigator.releaseGuard()
        navigator.popBackStack()
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

    private suspend fun saveChanges() {
        if (uiState.value.workout.name != uiState.value.initialWorkout.name){
            workoutRepository.updateWorkout(
                workoutId = navParams.id,
                workoutName = uiState.value.workout.name
            )
        }
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
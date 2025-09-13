package com.tonicantarella.gymtracker.ui.cardio.cardioworkout

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tonicantarella.gymtracker.GymPreferences
import com.tonicantarella.gymtracker.repository.cardio.CardioSessionRepository
import com.tonicantarella.gymtracker.repository.cardio.CardioWorkoutRepository
import com.tonicantarella.gymtracker.ui.entity.cardio.CardioMetrics
import com.tonicantarella.gymtracker.ui.entity.cardio.WorkoutWithMetrics
import com.tonicantarella.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val showFinishWorkoutDialog: Boolean = true,
) {
    val hasUnsavedChanges: Boolean = workout.name != initialWorkout.name
    val hasMarkedMetrics: Boolean = workout.metrics != initialWorkout.metrics

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
    private val sessionRepository: CardioSessionRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.CardioWorkout>()

    private val _uiState = MutableStateFlow(CardioWorkoutUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getWorkout()
    }

    fun getWorkout() {
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
                    showFinishWorkoutDialog = showFinishWorkoutDialog,
                    loading = false
                )
            }
        }
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

    fun onSaveChanges() {
        viewModelScope.launch {
            workoutRepository.updateWorkout(
                workoutId = navParams.id,
                workoutName = uiState.value.workout.name
            )
        }
    }

    fun onFinishPressed(onFinish: () -> Unit) {
        viewModelScope.launch {
            sessionRepository.markSessionDone(
                workoutId = navParams.id,
                metrics = uiState.value.workout.metrics
            )
            onFinish()
        }
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
}
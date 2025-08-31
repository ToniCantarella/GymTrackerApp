package com.example.gymtracker.ui.cardio.cardioitem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.repository.cardio.CardioSessionRepository
import com.example.gymtracker.repository.cardio.CardioWorkoutRepository
import com.example.gymtracker.ui.entity.cardio.CardioMetrics
import com.example.gymtracker.ui.entity.cardio.DistanceWithTimestamp
import com.example.gymtracker.ui.entity.cardio.DurationWithTimestamp
import com.example.gymtracker.ui.entity.cardio.StepsWithTimestamp
import com.example.gymtracker.ui.entity.cardio.WorkoutWithMetrics
import com.example.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

data class CardioItemUiState(
    val loading: Boolean = true,
    val previousCardio: WorkoutWithMetrics? = null,
    val cardioId: Int = 0,
    val cardio: WorkoutWithMetrics? = null,
    val initialCardio: WorkoutWithMetrics? = null,
    val selectedTimestamp: Instant? = null
)

class CardioItemViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: CardioWorkoutRepository,
    private val sessionRepository: CardioSessionRepository
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.CardioWorkout>()

    private val _uiState = MutableStateFlow(CardioItemUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val previousCardio = workoutRepository.getLatestWorkoutWithMetrics(navParams.id)
            val cardio = uiState.value.cardio?.copy(name = previousCardio?.name ?: "")
            val selectedTimestamp = navParams.timestampString?.let { Instant.parse(it) }

            _uiState.update {
                it.copy(
                    cardioId = navParams.id,
                    previousCardio = previousCardio,
                    cardio = cardio,
                    initialCardio = cardio,
                    selectedTimestamp = selectedTimestamp,
                    loading = false
                )
            }
        }
    }

    fun onChangeName(name: String) {
        _uiState.update {
            it.copy(
                cardio = it.cardio?.copy(name = name)
            )
        }
    }

    fun onStepsChange(steps: Int) {
        _uiState.update {
            it.copy(
                cardio = it.cardio?.copy(
                    steps = StepsWithTimestamp(
                        value = steps,
                        timestamp = uiState.value.selectedTimestamp
                    )
                )
            )
        }
    }

    fun onDistanceChange(distance: Double) {
        _uiState.update {
            it.copy(
                cardio = it.cardio?.copy(
                    distance = DistanceWithTimestamp(
                        value = distance,
                        timestamp = uiState.value.selectedTimestamp
                    )
                )
            )
        }
    }

    fun onDurationChange(duration: Duration) {
        _uiState.update {
            it.copy(
                cardio = it.cardio?.copy(
                    duration = DurationWithTimestamp(
                        value = duration,
                        timestamp = uiState.value.selectedTimestamp
                    )
                )
            )
        }
    }

    fun onFinishPressed(onFinish: () -> Unit) {
        viewModelScope.launch {
            val cardio = uiState.value.cardio
            sessionRepository.markSessionDone(
                workoutId = navParams.id,
                metrics = CardioMetrics(
                    steps = if (cardio?.steps?.value == 0) null else cardio?.steps?.value,
                    distance = if (cardio?.distance?.value == 0.0) null else cardio?.distance?.value,
                    duration = if (cardio?.duration?.value == Duration.ZERO) null else cardio?.duration?.value
                )
            )
            onFinish()
        }
    }
}
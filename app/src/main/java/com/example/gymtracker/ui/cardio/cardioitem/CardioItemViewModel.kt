package com.example.gymtracker.ui.cardio.cardioitem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.database.repository.WorkoutRepository
import com.example.gymtracker.ui.cardio.entity.Cardio
import com.example.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration

data class CardioItemUiState(
    val loading: Boolean = true,
    val previousCardio: Cardio? = null,
    val cardio: Cardio = Cardio.emptyCardio()
)

class CardioItemViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.CardioItem>()

    private val _uiState = MutableStateFlow(CardioItemUiState())
    val uiState = _uiState.asStateFlow()

    fun getCardio() {
        viewModelScope.launch {
            val previousCardio = workoutRepository.getLatestCardio(navParams.id)
            _uiState.update {
                it.copy(
                    previousCardio = previousCardio,
                    cardio = it.cardio.copy(name = previousCardio.name),
                    loading = false
                )
            }
        }
    }

    fun onChangeName(name: String) {
        _uiState.update {
            it.copy(
                cardio = it.cardio.copy(name = name)
            )
        }
    }

    fun onStepsChange(steps: Int) {
        _uiState.update {
            it.copy(
                cardio = it.cardio.copy(steps = steps)
            )
        }
    }

    fun onDistanceChange(distance: Double) {
        _uiState.update {
            it.copy(
                cardio = it.cardio.copy(distance = distance)
            )
        }
    }

    fun onDurationChange(duration: Duration) {
        _uiState.update {
            it.copy(
                cardio = it.cardio.copy(duration = duration)
            )
        }
    }

    fun onFinishPressed(onFinish: () -> Unit) {
        viewModelScope.launch {
            val cardio = uiState.value.cardio
            workoutRepository.markCardioSessionDone(
                navParams.id,
                cardio.copy(
                    steps = if (cardio.steps == 0) null else cardio.steps,
                    distance = if (cardio.distance == 0.0) null else cardio.distance,
                    duration = if (cardio.duration == Duration.ZERO) null else cardio.duration
                )
            )
            onFinish()
        }
    }
}
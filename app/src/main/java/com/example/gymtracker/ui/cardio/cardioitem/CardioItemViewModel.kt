package com.example.gymtracker.ui.cardio.cardioitem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.database.repository.CardioRepository
import com.example.gymtracker.ui.cardio.entity.Cardio
import com.example.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

data class CardioItemUiState(
    val loading: Boolean = true,
    val previousCardio: Cardio? = null,
    val cardioId: Int = 0,
    val cardio: Cardio = Cardio.emptyCardio(),
    val initialCardio: Cardio = Cardio.emptyCardio(),
    val selectedTimestamp: Instant? = null
)

class CardioItemViewModel(
    savedStateHandle: SavedStateHandle,
    private val cardioRepository: CardioRepository
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.CardioItem>()

    private val _uiState = MutableStateFlow(CardioItemUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val previousCardio = cardioRepository.getLatestCardio(navParams.id)
            val cardio = uiState.value.cardio.copy(name = previousCardio?.name ?: "")
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
            cardioRepository.markCardioSessionDone(
                id = navParams.id,
                cardio = cardio.copy(
                    steps = if (cardio.steps == 0) null else cardio.steps,
                    distance = if (cardio.distance == 0.0) null else cardio.distance,
                    duration = if (cardio.duration == Duration.ZERO) null else cardio.duration
                ),
                timestamp = uiState.value.selectedTimestamp
            )
            onFinish()
        }
    }
}
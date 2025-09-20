package com.tonicantarella.gymtracker.ui.stats.cardio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tonicantarella.gymtracker.repository.cardio.CardioSessionRepository
import com.tonicantarella.gymtracker.ui.entity.cardio.WorkoutWithMetrics
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import com.tonicantarella.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardioSessionStatsUiState(
    val loading: Boolean = true,
    val cardio: WorkoutWithMetrics? = null
)

class CardioSessionStatsViewModel(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: CardioSessionRepository,
    private val navigator: Navigator
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.CardioWorkoutSession>()

    private val _uiState = MutableStateFlow(CardioSessionStatsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val cardio = sessionRepository.getWorkoutForSession(navParams.id)
            _uiState.update {
                it.copy(
                    cardio = cardio,
                    loading = false
                )
            }
        }
    }

    fun onNavigateBack() {
        navigator.popBackStack()
    }
}
package com.tonicantarella.gymtracker.ui.stats.cardio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tonicantarella.gymtracker.repository.StatsRepository
import com.tonicantarella.gymtracker.ui.entity.cardio.CardioWorkoutStats
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import com.tonicantarella.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardioWorkoutStatsUiState(
    val loading: Boolean = true,
    val stats: CardioWorkoutStats? = null
)

class CardioWorkoutStatsViewModel(
    savedStateHandle: SavedStateHandle,
    private val statRepository: StatsRepository,
    private val navigator: Navigator
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.CardioWorkoutStats>()

    private val _uiState = MutableStateFlow(CardioWorkoutStatsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val stats = statRepository.getCardioWorkoutStats(navParams.id)
            _uiState.update {
                it.copy(
                    stats = stats,
                    loading = false
                )
            }
        }
    }

    fun onNavigateBack() {
        navigator.popBackStack()
    }
}
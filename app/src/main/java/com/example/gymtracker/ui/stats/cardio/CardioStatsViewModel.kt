package com.example.gymtracker.ui.stats.cardio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.database.repository.CardioStats
import com.example.gymtracker.database.repository.WorkoutRepository
import com.example.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardioStatsUiState(
    val loading: Boolean = true,
    val stats: CardioStats? = null
)

class CardioStatsViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
): ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.CardioStats>()

    private val _uiState = MutableStateFlow(CardioStatsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val stats = workoutRepository.getCardioStats(navParams.id)
            _uiState.update {
                it.copy(
                    stats = stats,
                    loading = false
                )
            }
        }
    }
}
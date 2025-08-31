package com.example.gymtracker.ui.stats.gym

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.repository.gym.GymStatsRepository
import com.example.gymtracker.ui.entity.gym.GymWorkoutStats
import com.example.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplitStatsUiState(
    val loading: Boolean = true,
    val splitName: String = "",
    val stats: GymWorkoutStats? = null
)

class SplitStatsViewModel(
    savedStateHandle: SavedStateHandle,
    private val statRepository: GymStatsRepository
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.SplitStats>()

    private val _uiState = MutableStateFlow(SplitStatsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val stats = statRepository.getWorkoutStats(navParams.id)
            _uiState.update {
                it.copy(
                    splitName = stats?.name ?: "",
                    stats = stats,
                    loading = false
                )
            }
        }
    }
}
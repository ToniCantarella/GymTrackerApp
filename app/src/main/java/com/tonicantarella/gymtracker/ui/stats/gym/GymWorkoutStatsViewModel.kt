package com.tonicantarella.gymtracker.ui.stats.gym

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tonicantarella.gymtracker.repository.gym.GymStatsRepository
import com.tonicantarella.gymtracker.ui.entity.gym.GymWorkoutStats
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import com.tonicantarella.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GymWorkoutStatsUiState(
    val loading: Boolean = true,
    val splitName: String = "",
    val stats: GymWorkoutStats? = null
)

class GymWorkoutStatsViewModel(
    savedStateHandle: SavedStateHandle,
    private val statRepository: GymStatsRepository,
    private val navigator: Navigator
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.GymWorkoutStats>()

    private val _uiState = MutableStateFlow(GymWorkoutStatsUiState())
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

    fun onNavigateBack() {
        navigator.popBackStack()
    }
}
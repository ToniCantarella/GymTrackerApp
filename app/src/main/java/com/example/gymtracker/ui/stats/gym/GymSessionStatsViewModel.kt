package com.example.gymtracker.ui.stats.gym

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.repository.gym.GymSessionRepository
import com.example.gymtracker.ui.entity.gym.WorkoutWithExercises
import com.example.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GymSessionUiState(
    val loading: Boolean = true,
    val workout: WorkoutWithExercises? = null
)

class GymSessionStatsViewModel(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: GymSessionRepository
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.GymWorkoutSession>()

    init {
        viewModelScope.launch {
            val workout = sessionRepository.getWorkoutForSession(navParams.id)
            _uiState.update {
                it.copy(
                    workout = workout,
                    loading = false
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(GymSessionUiState())
    val uiState = _uiState.asStateFlow()
}
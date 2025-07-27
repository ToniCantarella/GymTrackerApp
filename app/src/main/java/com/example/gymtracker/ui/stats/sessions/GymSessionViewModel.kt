package com.example.gymtracker.ui.stats.sessions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.database.repository.SplitWithExercises
import com.example.gymtracker.database.repository.WorkoutRepository
import com.example.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GymSessionUiState(
    val loading: Boolean = true,
    val split: SplitWithExercises? = null
)

class GymSessionViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.GymSession>()

    init {
        viewModelScope.launch {
            val split = workoutRepository.getSplitBySession(navParams.id)
            _uiState.update {
                it.copy(
                    split = split,
                    loading = false
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(GymSessionUiState())
    val uiState = _uiState.asStateFlow()
}
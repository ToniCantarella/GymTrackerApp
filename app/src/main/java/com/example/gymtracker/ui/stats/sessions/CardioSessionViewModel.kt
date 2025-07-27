package com.example.gymtracker.ui.stats.sessions

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

data class CardioSessionUiState(
    val loading: Boolean = true,
    val cardio: Cardio? = null
)

class CardioSessionViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val navParams = savedStateHandle.toRoute<Route.CardioSession>()

    private val _uiState = MutableStateFlow(CardioSessionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val cardio = workoutRepository.getCardioBySession(navParams.id)
            _uiState.update {
                it.copy(
                    cardio = cardio,
                    loading = false
                )
            }
        }
    }
}
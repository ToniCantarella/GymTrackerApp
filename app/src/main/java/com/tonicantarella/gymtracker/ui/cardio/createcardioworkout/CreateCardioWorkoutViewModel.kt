package com.tonicantarella.gymtracker.ui.cardio.createcardioworkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonicantarella.gymtracker.repository.cardio.CardioWorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateCardioWorkoutUiState(
    val name: String = ""
)

class CreateCardioWorkoutViewModel(
    private val workoutRepository: CardioWorkoutRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateCardioWorkoutUiState())
    val uiState = _uiState.asStateFlow()

    fun onChangeName(name: String) {
        _uiState.update {
            it.copy(
                name = name
            )
        }
    }

    fun onSavePressed(onSave: () -> Unit) {
        viewModelScope.launch {
            val cardioName = uiState.value.name
            workoutRepository.addWorkout(cardioName)
            onSave()
        }
    }
}
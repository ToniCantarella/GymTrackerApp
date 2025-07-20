package com.example.gymtracker.ui.cardio.createcario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.database.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateCardioUiState(
    val name: String = ""
)

class CreateCardioViewModel(
    private val workoutRepository: WorkoutRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(CreateCardioUiState())
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
            workoutRepository.addCardio(cardioName)
            onSave()
        }
    }
}
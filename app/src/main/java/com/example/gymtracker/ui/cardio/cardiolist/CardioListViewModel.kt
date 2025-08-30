package com.example.gymtracker.ui.cardio.cardiolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.repository.cardio.CardioWorkoutRepository
import com.example.gymtracker.ui.entity.WorkoutWithLatestTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardioListUiState(
    val loading: Boolean = true,
    val cardioList: List<WorkoutWithLatestTimestamp> = emptyList(),
    val selectingItems: Boolean = false
)

class CardioListViewModel(
    private val workoutRepository: CardioWorkoutRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CardioListUiState())
    val uiState = _uiState.asStateFlow()

    fun getCardioList() {
        viewModelScope.launch {
            val cardioList = workoutRepository.getAllWorkouts()
            _uiState.update {
                it.copy(
                    cardioList = cardioList,
                    loading = false
                )
            }
        }
    }

    fun startSelectingItems(id: Int? = null) {
        _uiState.update {
            it.copy(
                selectingItems = true,
                cardioList = it.cardioList.map { cardio ->
                    if (cardio.id == id) {
                        cardio.copy(
                            selected = true
                        )
                    } else cardio
                }
            )
        }
    }

    fun stopSelectingItems() {
        _uiState.update {
            it.copy(
                selectingItems = false,
                cardioList = it.cardioList.map { cardio ->
                    cardio.copy(
                        selected = false
                    )
                }
            )
        }
    }

    fun onSelectItem(id: Int, selected: Boolean) {
        _uiState.update {
            it.copy(
                cardioList = it.cardioList.map { cardio ->
                    if (cardio.id == id) {
                        cardio.copy(
                            selected = selected
                        )
                    } else cardio
                }
            )
        }
    }

    fun onDeleteCardioList(onDeletionDone: () -> Unit) {
        val itemsToDelete = uiState.value.cardioList.filter { it.selected }

        viewModelScope.launch {
            itemsToDelete.forEach {
                workoutRepository.deleteWorkout(it.id)
            }
            stopSelectingItems()
            getCardioList()
            onDeletionDone()
        }
    }
}
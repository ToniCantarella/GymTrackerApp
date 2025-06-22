package com.example.gymtracker.ui.workouts.splitslist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SplitListUiState(
    val splits: List<ExerciseListItem> = emptyList(),
    val selectingItemsToDelete: Boolean = false,
    val itemsToDelete: List<Long> = emptyList()
)

class SplitListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SplitListUiState())
    val uiState = _uiState.asStateFlow()

    fun startSelectingForDeletion() {
        _uiState.update {
            it.copy(
                selectingItemsToDelete = true
            )
        }
    }

    fun stopSelectingForDeletion() {
        _uiState.update {
            it.copy(
                selectingItemsToDelete = false,
                itemsToDelete = emptyList()
            )
        }
    }

    fun onSelectForDeletion(id: Long) {
        _uiState.update {
            it.copy(
                itemsToDelete =
                    if (it.itemsToDelete.contains(id)) it.itemsToDelete.filter { item -> id != item }
                    else it.itemsToDelete + listOf(id)
            )
        }
    }
}
package com.example.gymtracker.ui.workouts.splitslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.database.repository.GymRepository
import com.example.gymtracker.database.repository.WorkoutListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplitListUiState(
    val loading: Boolean = true,
    val splits: List<WorkoutListItem> = emptyList(),
    val selectingItems: Boolean = false,
    val selectedItems: List<Int> = emptyList()
)

class SplitListViewModel(
    private val gymRepository: GymRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplitListUiState())
    val uiState = _uiState.asStateFlow()

    fun getSplits() {
        viewModelScope.launch {
            val splits = gymRepository.getSplitsWithLatestTimestamp()
            _uiState.update {
                it.copy(
                    splits = splits,
                    loading = false
                )
            }
        }
    }

    fun startSelectingItems(id: Int? = null) {
        _uiState.update {
            it.copy(
                selectingItems = true,
                selectedItems = if (id != null) listOf(id) else emptyList()
            )
        }
    }

    fun stopSelectingItems() {
        _uiState.update {
            it.copy(
                selectingItems = false,
                selectedItems = emptyList()
            )
        }
    }

    fun onSelectItem(id: Int) {
        _uiState.update {
            it.copy(
                selectedItems =
                    if (it.selectedItems.contains(id)) it.selectedItems.filter { item -> id != item }
                    else it.selectedItems + listOf(id)
            )
        }
    }

    fun onDeleteSplits(onDeletionDone: () -> Unit) {
        val itemsToDelete = uiState.value.selectedItems

        viewModelScope.launch {
            itemsToDelete.forEach {
                gymRepository.deleteSplit(it)
            }
            stopSelectingItems()
            getSplits()
            onDeletionDone()
        }
    }
}
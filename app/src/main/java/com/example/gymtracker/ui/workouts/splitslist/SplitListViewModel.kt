package com.example.gymtracker.ui.workouts.splitslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.database.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class SplitListItem(
    val id: Int,
    val name: String,
    val latestTimestamp: Instant
)

data class SplitListUiState(
    val loading: Boolean = true,
    val splits: List<SplitListItem> = emptyList(),
    val selectingItems: Boolean = false,
    val selectedItems: List<Int> = emptyList()
)

const val MAX_SPLITS = 7

class SplitListViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplitListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getSplits()
    }

    fun getSplits() {
        viewModelScope.launch {
            val splits = workoutRepository.getSplitsWithLatestTimestamp()
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

    fun onDeleteSplits() {
        val itemsToDelete = uiState.value.selectedItems

        viewModelScope.launch {
            itemsToDelete.forEach {
                workoutRepository.deleteSplit(it)
            }

            getSplits()
        }
    }
}
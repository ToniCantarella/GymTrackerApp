package com.example.gymtracker.ui.gym.splitslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.repository.GymRepository
import com.example.gymtracker.repository.WorkoutWithLatestTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplitListUiState(
    val loading: Boolean = true,
    val splits: List<WorkoutWithLatestTimestamp> = emptyList(),
    val selectingItems: Boolean = false
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
                splits = it.splits.map { split ->
                    if (split.id == id) {
                        split.copy(
                            selected = true
                        )
                    } else split
                }
            )
        }
    }

    fun stopSelectingItems() {
        _uiState.update {
            it.copy(
                selectingItems = false,
                splits = it.splits.map { split ->
                    split.copy(
                        selected = false
                    )
                }
            )
        }
    }

    fun onSelectItem(id: Int, selected: Boolean) {
        _uiState.update {
            it.copy(
                splits = it.splits.map { split ->
                    if (split.id == id) {
                        split.copy(
                            selected = selected
                        )
                    } else split
                }
            )
        }
    }

    fun onDeleteSplits(onDeletionDone: () -> Unit) {
        val itemsToDelete = uiState.value.splits.filter { it.selected }

        viewModelScope.launch {
            itemsToDelete.forEach {
                gymRepository.deleteSplit(it.id)
            }
            stopSelectingItems()
            getSplits()
            onDeletionDone()
        }
    }
}
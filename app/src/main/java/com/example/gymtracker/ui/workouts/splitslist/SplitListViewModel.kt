package com.example.gymtracker.ui.workouts.splitslist

import android.util.Log
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
    val selectingItemsToDelete: Boolean = false,
    val itemsToDelete: List<Int> = emptyList()
)

class SplitListViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplitListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val splits = workoutRepository.getSplitsWithLatestTimestamp()
            Log.d("toni", "splits : ${splits}")
            _uiState.update {
                it.copy(
                    splits = splits,
                    loading = false
                )
            }
        }
    }

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

    fun onSelectForDeletion(id: Int) {
        _uiState.update {
            it.copy(
                itemsToDelete =
                    if (it.itemsToDelete.contains(id)) it.itemsToDelete.filter { item -> id != item }
                    else it.itemsToDelete + listOf(id)
            )
        }
    }
}
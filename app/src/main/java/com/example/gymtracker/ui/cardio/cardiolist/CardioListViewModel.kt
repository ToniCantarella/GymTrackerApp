package com.example.gymtracker.ui.cardio.cardiolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.database.repository.CardioRepository
import com.example.gymtracker.database.repository.WorkoutListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardioListUiState(
    val loading: Boolean = true,
    val cardioList: List<WorkoutListItem> = emptyList(),
    val selectingItems: Boolean = false,
    val selectedItems: List<Int> = emptyList()
)

class CardioListViewModel(
    private val cardioRepository: CardioRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CardioListUiState())
    val uiState = _uiState.asStateFlow()

    fun getCardioList() {
        viewModelScope.launch {
            val cardioList = cardioRepository.getCardioListWithLatestTimestamp()
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

    fun onDeleteCardioList(onDeletionDone: () -> Unit) {
        val itemsToDelete = uiState.value.selectedItems

        viewModelScope.launch {
            itemsToDelete.forEach {
                cardioRepository.deleteCardio(it)
            }
            stopSelectingItems()
            getCardioList()
            onDeletionDone()
        }
    }
}
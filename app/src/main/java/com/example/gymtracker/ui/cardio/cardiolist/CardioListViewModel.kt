package com.example.gymtracker.ui.cardio.cardiolist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CardioListUiState(
    val cardios: List<String> = emptyList()
)

class CardioListViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(CardioListUiState())
    val uiState = _uiState.asStateFlow()
}